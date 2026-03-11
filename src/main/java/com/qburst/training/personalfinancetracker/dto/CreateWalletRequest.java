package com.qburst.training.personalfinancetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for creating a digital wallet")
public class CreateWalletRequest {

    @Schema(description = "ID of the user who owns this wallet", example = "Cash Wallet")
    private Long userId;

    @Schema(description = "Wallet name", example = "Cash Wallet")
    private String walletName;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }
}
