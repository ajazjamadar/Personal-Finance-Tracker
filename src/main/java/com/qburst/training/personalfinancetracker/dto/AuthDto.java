package com.qburst.training.personalfinancetracker.dto;

import com.qburst.training.personalfinancetracker.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class AuthDto {

    public record RegisterRequest(
            @NotBlank(message = "Username is required")
            @Size(min = 3, max = 100)
            String username,

            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 6, message = "Password must contain at least 6 characters")
            String password,

            @NotBlank(message = "Full name is required")
            String fullName
    ) {
    }

    public record OtpRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            String email
    ) {
    }

    public record LoginRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            String email,

            @NotBlank(message = "Password is required")
            String password
    ) {
    }

    public record OtpVerifyRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            String email,

            @NotBlank(message = "OTP is required")
            @Size(min = 6, max = 6, message = "OTP must be 6 digits")
            String otp
    ) {
    }

    public record UserSession(
            Long id,
            String username,
            String email,
            String fullName,
            UserRole role,
            LocalDateTime createdAt
    ) {
    }

    public record OtpDispatchResponse(
            String message,
            LocalDateTime expiresAt
    ) {
    }

    public record AuthResponse(
            String accessToken,
            String tokenType,
            long expiresInSeconds,
            UserSession user
    ) {
    }
}
