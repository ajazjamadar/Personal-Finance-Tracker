package com.qburst.training.personalfinancetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Request body for bank to wallet transfer")
public class TransferRequest {

    @Schema(description = "Source bank account ID", example = "1")
    private Long bankAccountId;

    @Schema(description = "Destination wallet ID", example = "101")
    private Long walletId;

    @Schema(description = "Amount to transfer", example = "1000.00")
    private BigDecimal amount;

    public Long getBankAccountId() {
        return bankAccountId;
    }

    public void setBankAccountId(Long bankAccountId) {
        this.bankAccountId = bankAccountId;
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
