package com.qburst.training.personalfinancetracker.service.auth;

import com.qburst.training.personalfinancetracker.dto.AuthDto;

public interface AuthService {
    AuthDto.UserSession registerUser(AuthDto.RegisterRequest request);

    AuthDto.AuthResponse loginUser(AuthDto.LoginRequest request);

    AuthDto.AuthResponse loginAdmin(AuthDto.LoginRequest request);

    AuthDto.OtpDispatchResponse requestUserLoginOtp(AuthDto.OtpRequest request);

    AuthDto.OtpDispatchResponse requestAdminLoginOtp(AuthDto.OtpRequest request);

    AuthDto.AuthResponse verifyUserLoginOtp(AuthDto.OtpVerifyRequest request);

    AuthDto.AuthResponse verifyAdminLoginOtp(AuthDto.OtpVerifyRequest request);

    AuthDto.UserSession me();
}
