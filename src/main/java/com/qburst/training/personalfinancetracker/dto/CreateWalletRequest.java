package com.qburst.training.personalfinancetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Request body for creating a digital wallet")
@Getter
@Setter
public class CreateWalletRequest {

    @Schema(description = "ID of the user who owns this wallet", example = "Cash Wallet")
    private Long userId;

    @Schema(description = "Wallet name", example = "Cash Wallet")
    private String walletName;

}
