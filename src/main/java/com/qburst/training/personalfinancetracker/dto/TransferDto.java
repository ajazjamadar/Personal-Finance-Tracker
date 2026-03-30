package com.qburst.training.personalfinancetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferDto {

    @Schema(name = "TransferRequest")
    public record Request(
            @NotNull(message = "Source ID is required")
            Long sourceId,

            @NotNull(message = "Destination ID is required")
            Long destinationId,

            @NotNull(message = "Amount is required")
            @Positive(message = "Amount must be positive")
            BigDecimal amount
    ) {}

    @Schema(name = "TransferResponse")
    public record Response(
            Long id,
            String transactionType,
            BigDecimal amount,
            String description,
            LocalDateTime createdAt
    ) {}
}
