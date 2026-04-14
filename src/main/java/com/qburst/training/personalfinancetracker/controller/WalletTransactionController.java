package com.qburst.training.personalfinancetracker.controller;

import com.qburst.training.personalfinancetracker.dto.WalletTransactionDto;
import com.qburst.training.personalfinancetracker.service.wallet.WalletTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Wallet Transactions", description = "Deposit, withdraw and transfer between wallets")
public class WalletTransactionController {

    private final WalletTransactionService walletTransactionService;

    public WalletTransactionController(WalletTransactionService walletTransactionService) {
        this.walletTransactionService = walletTransactionService;
    }

    @PostMapping("/deposit")
    @Operation(summary = "Deposit money into wallet")
    @ApiResponse(responseCode = "201", description = "Deposit recorded")
    public ResponseEntity<WalletTransactionDto.Response> deposit(
            @Valid @RequestBody WalletTransactionDto.DepositWithdrawRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(walletTransactionService.deposit(request));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw money from wallet")
    @ApiResponse(responseCode = "201", description = "Withdrawal recorded")
    public ResponseEntity<WalletTransactionDto.Response> withdraw(
            @Valid @RequestBody WalletTransactionDto.DepositWithdrawRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(walletTransactionService.withdraw(request));
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money between wallets")
    @ApiResponse(responseCode = "201", description = "Transfer recorded")
    public ResponseEntity<WalletTransactionDto.TransferResponse> transfer(
            @Valid @RequestBody WalletTransactionDto.TransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(walletTransactionService.transfer(request));
    }
}
