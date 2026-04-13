package com.qburst.training.personalfinancetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BankAccountDto {

        @Schema(name = "BankAccountRequest")
    public record Request(
            @NotNull(message = "User ID is required")
            Long userId,

            @NotBlank(message = "Bank name is required")
            String bankName,

            @NotBlank(message = "Account number is required")
            String accountNumber,

            @NotNull(message = "Initial balance is required")
            @PositiveOrZero(message = "Balance cannot be negative")
            BigDecimal initialBalance
    ) {}

    @Schema(name = "BankAccountResponse")
    public record Response(
            Long id,
            Long userId,
            String bankName,
            String accountNumber,
            BigDecimal balance,
            LocalDateTime createdAt
    ) {}

    @Schema(name = "BankAccountBalanceSummary")
    public record BalanceSummary(
            String bankName,
            String accountNumber,
            BigDecimal balance
    ) {}

    @Schema(name = "AdminBankAccountUpdateRequest")
    public record AdminUpdateRequest(
            @NotNull(message = "User ID is required")
            Long userId,

            @NotBlank(message = "Bank name is required")
            String bankName,

            @NotBlank(message = "Account number is required")
            String accountNumber,

            @NotNull(message = "Balance is required")
            @PositiveOrZero(message = "Balance cannot be negative")
            BigDecimal balance
    ) {}
}
