package com.qburst.training.personalfinancetracker.dto;

import com.qburst.training.personalfinancetracker.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class UserDto {

        @Schema(name = "UserRequest")
    public record Request(
            @NotBlank(message = "Username is required")
            @Size(min = 3, max = 100)
            String username,

            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 6)
            String password,

            @NotBlank(message = "Full name is required")
            String fullName,

            UserRole role
    ) {}

    @Schema(name = "UserResponse")
    public record Response(
            Long id,
            String username,
            String email,
            String fullName,
                        UserRole role,
            LocalDateTime createdAt
    ) {}

    @Schema(name = "UserUpdateRequest")
    public record UpdateRequest(
            @NotBlank(message = "Username is required")
            @Size(min = 3, max = 100)
            String username,

            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            String email,

            @NotBlank(message = "Full name is required")
            String fullName,

            @Size(min = 6, message = "Password must contain at least 6 characters")
            String password
    ) {}
}