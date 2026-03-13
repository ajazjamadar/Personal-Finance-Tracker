package com.qburst.training.personalfinancetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Schema(description = "Request body for recording a transaction (income or expense)")
@Getter
@Setter
public class TransactionRequest {

    @Schema(description = "Wallet ID (for wallet transaction)", example = "101")
    private Long walletId;

    @Schema(description = "Bank account ID (for bank transactions)", example = "1")
    private Long bankAccountId;

    @Schema(description = "Transaction Amount - must be positive", example = "500.00")
    private BigDecimal amount;

    @Schema(description = "Expense category", example = "Food")
    private String category;

    @Schema(description = "Transaction note or description", example = "Dinner at Restaurent")
    private String description;

}
