package com.qburst.training.personalfinancetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Schema(description = "Request body for bank to wallet transfer")
@Getter
@Setter
public class TransferRequest {

    @Schema(description = "Source bank account ID", example = "1")
    private Long bankAccountId;

    @Schema(description = "Destination wallet ID", example = "101")
    private Long walletId;

    @Schema(description = "Amount to transfer", example = "1000.00")
    private BigDecimal amount;

}
