package com.qburst.training.personalfinancetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Request body for recording a transaction (income or expense)")
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

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public Long getBankAccountId() {
        return bankAccountId;
    }

    public void setBankAccountId(Long bankAccountId) {
        this.bankAccountId = bankAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
