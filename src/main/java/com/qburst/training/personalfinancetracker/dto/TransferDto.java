package com.qburst.training.personalfinancetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferDto {

    @Schema(name = "TransferRequest")
    public record Request(
            @NotNull(message = "Source account ID is required")
            Long sourceAccountId,

            Long destinationAccountId,

            Long destinationWalletId,

            @NotNull(message = "Transfer type is required")
            TransferType transferType,

            Boolean selfTransfer,

            String mobileNumber,

            String upiId,

            String receiverName,

            String paymentMethod,

            String transferStatus,

            @NotNull(message = "Amount is required")
            @Positive(message = "Amount must be positive")
            BigDecimal amount,

            String description
    ) {}

    public enum TransferType {
        ACCOUNT,
        WALLET,
        MOBILE,
        UPI
    }

    @Schema(name = "TransferResponse")
    public record Response(
            Long id,
            String transactionType,
            String transferType,
            Boolean selfTransfer,
            Long sourceAccountId,
            Long destinationAccountId,
            Long destinationWalletId,
            String destinationValue,
            String receiverName,
            String paymentMethod,
            String transferStatus,
            BigDecimal amount,
            String description,
            LocalDateTime createdAt
    ) {}
}
