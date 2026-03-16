package com.qburst.training.personalfinancetracker.controller;

import com.qburst.training.personalfinancetracker.dto.TransferRequest;
import com.qburst.training.personalfinancetracker.dto.TransactionResponse;
import com.qburst.training.personalfinancetracker.service.transfer.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@Tag(name = "Transfers", description = "Fund transfers between bank accounts and wallets")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/bank-to-wallet")
    @Operation(summary = "Transfer from bank account to wallet")
    public ResponseEntity<TransactionResponse> bankToWallet(
            @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transferService.bankToWallet(request));
    }

    @PostMapping("/wallet-to-bank")
    @Operation(summary = "Transfer from wallet to bank account")
    public ResponseEntity<TransactionResponse> walletToBank(
            @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transferService.walletToBank(request));
    }
}