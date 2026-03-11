package com.qburst.training.personalfinancetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Request body for creating a bank account")
public class CreatingBankAccountRequest {

    @Schema(description = "ID of the user who owns the account", example = "1")
    private Long usedId;

    @Schema(description = "Name of the Bank", example = "SBI bank")
    private String bankName;

    @Schema(description = "Unique bank account number", example = "123456789")
    private String accountNumber;

    @Schema(description = "Initial deposit amount", example = "50000.00")
    private BigDecimal initialBalance;

    public Long getUsedId() {
        return usedId;
    }

    public void setUsedId(Long usedId) {
        this.usedId = usedId;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }
}
