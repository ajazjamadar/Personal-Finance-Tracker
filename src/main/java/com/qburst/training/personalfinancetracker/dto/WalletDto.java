package com.qburst.training.personalfinancetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WalletDto {

        @Schema(name = "WalletRequest")
    public record Request(
            @NotNull(message = "User ID is required")
            Long userId,

            @NotBlank(message = "Wallet name is required")
            String walletName,

            @NotNull(message = "Initial balance is required")
            @PositiveOrZero(message = "Balance cannot be negative")
            BigDecimal initialBalance
    ) {}

    @Schema(name = "WalletResponse")
    public record Response(
            Long id,
            Long userId,
            String walletName,
            BigDecimal balance,
            LocalDateTime createdAt
    ) {}
}