package com.qburst.training.personalfinancetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDto {

        @Schema(name = "TransactionRequest")
    public record Request(
            Long walletId,
            Long bankAccountId,

            @NotNull(message = "Amount is required")
            @Positive(message = "Amount must be greater than zero")
            BigDecimal amount,

            String category,

            @Size(max = 255)
            String description
    ) {}

    @Schema(name = "TransactionResponse")
    public record Response(
            Long id,
            String transactionType,
            BigDecimal amount,
            String description,
            LocalDateTime createdAt
    ) {}

    @Schema(name = "TransactionTransferRequest")
    public record TransferRequest(
            @NotNull(message = "Source ID is required")
            Long sourceId,

            @NotNull(message = "Destination ID is required")
            Long destinationId,

            @NotNull(message = "Amount is required")
            @Positive(message = "Amount must be greater than zero")
            BigDecimal amount
    ) {}
}