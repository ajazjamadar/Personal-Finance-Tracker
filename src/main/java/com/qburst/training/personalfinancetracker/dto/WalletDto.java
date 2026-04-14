package com.qburst.training.personalfinancetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WalletDto {

    @Schema(name = "WalletRequest")
    public record Request(
            @NotBlank(message = "Wallet name is required")
            String name,

            @PositiveOrZero(message = "Initial balance cannot be negative")
            BigDecimal initialBalance,

            @Pattern(regexp = "^[A-Za-z]{3}$", message = "Currency must be a 3-letter code")
            String currency
    ) {}

    @Schema(name = "WalletResponse")
    public record Response(
            Long id,
            Long userId,
            String name,
            BigDecimal balance,
            String currency,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}
