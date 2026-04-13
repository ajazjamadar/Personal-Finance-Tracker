package com.qburst.training.personalfinancetracker.dto;

import com.qburst.training.personalfinancetracker.entity.Transaction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

public class TransactionDto {
        @Schema(name = "TransactionRequest")
    public record Request(
            @NotNull(message = "Bank account ID is required")
            Long bankAccountId,

            @NotNull(message = "Amount is required")
            @Positive(message = "Amount must be greater than zero")
            BigDecimal amount,

            String category,

            @Size(max = 255)
            String description,

            String receiverName,

            String paymentMethod
    ) {}

    @Schema(name = "TransactionResponse")
    public record Response(
            Long id,
            Long userId,
            String transactionType,
            String transferType,
            Boolean selfTransfer,
            Long sourceAccountId,
            Long destinationAccountId,
            String destinationValue,
            BigDecimal amount,
            String description,
            String category,
            String status,
            String paymentMethod,
            String receiverName,
            LocalDateTime createdAt
    ) {}

    @Schema(name = "TransactionHistoryFilter")
    public record HistoryFilter(
            LocalDate fromDate,
            LocalDate toDate,
            HistoryTransactionType transactionType,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Transaction.TransactionStatus status,
            String category,
            Transaction.PaymentMethod paymentMethod,
            Long accountId,
            String receiver
    ) {
        public HistoryFilter normalized() {
            return new HistoryFilter(
                    fromDate,
                    toDate,
                    transactionType,
                    minAmount,
                    maxAmount,
                    status,
                    normalizeText(category),
                    paymentMethod,
                    accountId,
                    normalizeText(receiver)
            );
        }

        private static String normalizeText(String value) {
            if (value == null) {
                return null;
            }
            String trimmed = value.trim();
            return trimmed.isEmpty() ? null : trimmed.toLowerCase(Locale.ROOT);
        }
    }

    public enum HistoryTransactionType {
        CREDIT,
        DEBIT
    }

    @Schema(name = "BulkRowError")
    public record BulkRowError(
            int rowNumber,
            String error
    ) {}

    @Schema(name = "BulkResponse")
    public record BulkResponse(
            int acceptedCount,
            int rejectedCount,
            java.util.List<BulkRowError> errors,
            java.util.List<Response> transactions
    ) {}
}
