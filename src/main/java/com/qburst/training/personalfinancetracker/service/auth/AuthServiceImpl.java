package com.qburst.training.personalfinancetracker.service.auth;

import com.qburst.training.personalfinancetracker.dto.AuthDto;
import com.qburst.training.personalfinancetracker.dto.UserDto;
import com.qburst.training.personalfinancetracker.entity.LoginOtp;
import com.qburst.training.personalfinancetracker.entity.User;
import com.qburst.training.personalfinancetracker.entity.UserRole;
import com.qburst.training.personalfinancetracker.exception.ResourceNotFoundException;
import com.qburst.training.personalfinancetracker.repository.LoginOtpRepository;
import com.qburst.training.personalfinancetracker.repository.UserRepository;
import com.qburst.training.personalfinancetracker.security.AuthContextService;
import com.qburst.training.personalfinancetracker.security.JwtService;
import com.qburst.training.personalfinancetracker.service.mail.MailService;
import com.qburst.training.personalfinancetracker.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final LoginOtpRepository loginOtpRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MailService mailService;
    private final AuthContextService authContextService;

    @Value("${app.auth.otp-expiration-minutes:5}")
    private long otpExpiryMinutes;

    @Override
    @Transactional
    public AuthDto.UserSession registerUser(AuthDto.RegisterRequest request) {
        UserDto.Request userRequest = new UserDto.Request(
                request.username(),
                request.email(),
                request.password(),
                request.fullName(),
                UserRole.USER
        );
        UserDto.Response created = userService.createUser(userRequest, UserRole.USER);
        return toSession(created);
    }

    @Override
    @Transactional
    public AuthDto.AuthResponse loginUser(AuthDto.LoginRequest request) {
        return loginWithPassword(request, UserRole.USER);
    }

    @Override
    @Transactional
    public AuthDto.AuthResponse loginAdmin(AuthDto.LoginRequest request) {
        return loginWithPassword(request, UserRole.ADMIN);
    }

    @Override
    @Transactional
    public AuthDto.OtpDispatchResponse requestUserLoginOtp(AuthDto.OtpRequest request) {
        return issueOtp(request, UserRole.USER);
    }

    @Override
    @Transactional
    public AuthDto.OtpDispatchResponse requestAdminLoginOtp(AuthDto.OtpRequest request) {
        return issueOtp(request, UserRole.ADMIN);
    }

    @Override
    @Transactional
    public AuthDto.AuthResponse verifyUserLoginOtp(AuthDto.OtpVerifyRequest request) {
        return verifyOtp(request, UserRole.USER);
    }

    @Override
    @Transactional
    public AuthDto.AuthResponse verifyAdminLoginOtp(AuthDto.OtpVerifyRequest request) {
        return verifyOtp(request, UserRole.ADMIN);
    }

    @Override
    public AuthDto.UserSession me() {
        User user = userRepository.findById(authContextService.currentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
        return toSession(user);
    }

    private AuthDto.OtpDispatchResponse issueOtp(AuthDto.OtpRequest request, UserRole expectedRole) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + request.email()));

        if (user.getRole() != expectedRole) {
            throw new AccessDeniedException("This account cannot use this login flow");
        }

        String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(otpExpiryMinutes);

        LoginOtp loginOtp = LoginOtp.builder()
                .user(user)
                .otpHash(passwordEncoder.encode(otp))
                .purpose(LoginOtp.OtpPurpose.LOGIN)
                .expiresAt(expiry)
                .build();
        loginOtpRepository.save(loginOtp);

        mailService.sendLoginOtp(user.getEmail(), user.getFullName(), otp);
        return new AuthDto.OtpDispatchResponse("OTP sent successfully", expiry);
    }

    private AuthDto.AuthResponse loginWithPassword(AuthDto.LoginRequest request, UserRole expectedRole) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + request.email()));

        if (user.getRole() != expectedRole) {
            throw new AccessDeniedException("This account cannot use this login flow");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AccessDeniedException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);
        return new AuthDto.AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirySeconds(),
                toSession(user)
        );
    }

    private AuthDto.AuthResponse verifyOtp(AuthDto.OtpVerifyRequest request, UserRole expectedRole) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + request.email()));

        if (user.getRole() != expectedRole) {
            throw new AccessDeniedException("This account cannot use this login flow");
        }

        LoginOtp latestOtp = loginOtpRepository.findTopByUserIdAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(
                        user.getId(),
                        LoginOtp.OtpPurpose.LOGIN
                )
                .orElseThrow(() -> new AccessDeniedException("OTP not found. Request a new OTP."));

        if (latestOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AccessDeniedException("OTP expired. Request a new OTP.");
        }

        if (!passwordEncoder.matches(request.otp(), latestOtp.getOtpHash())) {
            throw new AccessDeniedException("Invalid OTP");
        }

        latestOtp.setUsedAt(LocalDateTime.now());
        loginOtpRepository.save(latestOtp);

        String token = jwtService.generateToken(user);
        return new AuthDto.AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirySeconds(),
                toSession(user)
        );
    }

    private AuthDto.UserSession toSession(UserDto.Response user) {
        return new AuthDto.UserSession(
                user.id(),
                user.username(),
                user.email(),
                user.fullName(),
                user.role(),
                user.createdAt()
        );
    }

    private AuthDto.UserSession toSession(User user) {
        return new AuthDto.UserSession(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
