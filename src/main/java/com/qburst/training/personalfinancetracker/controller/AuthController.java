package com.qburst.training.personalfinancetracker.controller;

import com.qburst.training.personalfinancetracker.dto.AuthDto;
import com.qburst.training.personalfinancetracker.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "OTP-based authentication and session APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Create a new user account")
    public ResponseEntity<AuthDto.UserSession> register(@Valid @RequestBody AuthDto.RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(request));
    }

    @PostMapping("/user/request-otp")
    @Operation(summary = "Request login OTP for user role")
    public ResponseEntity<AuthDto.OtpDispatchResponse> requestUserOtp(@Valid @RequestBody AuthDto.OtpRequest request) {
        return ResponseEntity.ok(authService.requestUserLoginOtp(request));
    }

    @PostMapping("/user/login")
    @Operation(summary = "Direct login for user with email and password")
    public ResponseEntity<AuthDto.AuthResponse> loginUser(@Valid @RequestBody AuthDto.LoginRequest request) {
        return ResponseEntity.ok(authService.loginUser(request));
    }

    @PostMapping("/user/verify-otp")
    @Operation(summary = "Verify user OTP and get access token")
    public ResponseEntity<AuthDto.AuthResponse> verifyUserOtp(@Valid @RequestBody AuthDto.OtpVerifyRequest request) {
        return ResponseEntity.ok(authService.verifyUserLoginOtp(request));
    }

    @PostMapping("/admin/request-otp")
    @Operation(summary = "Request login OTP for admin role")
    public ResponseEntity<AuthDto.OtpDispatchResponse> requestAdminOtp(@Valid @RequestBody AuthDto.OtpRequest request) {
        return ResponseEntity.ok(authService.requestAdminLoginOtp(request));
    }

    @PostMapping("/admin/login")
    @Operation(summary = "Direct login for admin with email and password")
    public ResponseEntity<AuthDto.AuthResponse> loginAdmin(@Valid @RequestBody AuthDto.LoginRequest request) {
        return ResponseEntity.ok(authService.loginAdmin(request));
    }

    @PostMapping("/admin/verify-otp")
    @Operation(summary = "Verify admin OTP and get access token")
    public ResponseEntity<AuthDto.AuthResponse> verifyAdminOtp(@Valid @RequestBody AuthDto.OtpVerifyRequest request) {
        return ResponseEntity.ok(authService.verifyAdminLoginOtp(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    public ResponseEntity<AuthDto.UserSession> me() {
        return ResponseEntity.ok(authService.me());
    }
}
