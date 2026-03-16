package com.qburst.training.personalfinancetracker.dto;

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
            String fullName
    ) {}

    @Schema(name = "UserResponse")
    public record Response(
            Long id,
            String username,
            String email,
            String fullName,
            LocalDateTime createdAt
    ) {}
}