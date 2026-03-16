package com.qburst.training.personalfinancetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String transactionType,
        BigDecimal amount,
        String description,
        LocalDateTime createdAt
) {}
