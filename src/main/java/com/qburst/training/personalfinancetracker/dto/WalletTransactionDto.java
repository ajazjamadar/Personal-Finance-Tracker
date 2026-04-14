package com.qburst.training.personalfinancetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WalletTransactionDto {

    @Schema(name = "WalletDepositWithdrawRequest")
    public record DepositWithdrawRequest(
            @NotNull(message = "Wallet ID is required")
            Long walletId,

            @NotNull(message = "Amount is required")
            @Positive(message = "Amount must be greater than zero")
            BigDecimal amount,

            @Size(max = 100)
            String category,

            @Size(max = 255)
            String description
    ) {}

    @Schema(name = "WalletTransferRequest")
    public record TransferRequest(
            @NotNull(message = "From wallet ID is required")
            Long fromWalletId,

            @NotNull(message = "To wallet ID is required")
            Long toWalletId,

            @NotNull(message = "Amount is required")
            @Positive(message = "Amount must be greater than zero")
            BigDecimal amount,

            @Size(max = 100)
            String category,

            @Size(max = 255)
            String description
    ) {}

    @Schema(name = "WalletTransactionResponse")
    public record Response(
            Long id,
            Long walletId,
            String type,
            BigDecimal amount,
            String category,
            String description,
            LocalDateTime createdAt
    ) {}

    @Schema(name = "WalletTransferResponse")
    public record TransferResponse(
            Long fromWalletId,
            Long toWalletId,
            BigDecimal amount,
            Response debitTransaction,
            Response creditTransaction,
            BigDecimal fromWalletBalance,
            BigDecimal toWalletBalance,
            LocalDateTime transferredAt
    ) {}
}
