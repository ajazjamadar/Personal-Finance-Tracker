package com.qburst.training.personalfinancetracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qburst.training.personalfinancetracker.dto.TransferRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/transfers")
@Tag(name = "Transfers", description = "Fund transfers between bank accounts and wallets")
public class TransferController {

    @PostMapping("/bank-to-wallet")
    @Operation(
            summary = "Transfer: Bank to Wallet",
            description = "Transfers funds from a bank account to a wallet. Bank balance decreases, wallet balance increases. Accepts: bankAccountId, walletId, amount"
    )

    public ResponseEntity<String> bankToWallet(@RequestBody TransferRequest request){
        // TODO: Call TransferService.bankToWallet(request)
        return ResponseEntity.status(201).body("Bank to wallet transfer successful.");
    }

    @PostMapping("/wallet-to-bank")
    @Operation(
            summary = "Transfer: Wallet",
            description = "Transfers funds from a wallet back to a bank account. Wallet balance decreases, bank balance increases."
    )

    public ResponseEntity<String> walletToBank(@RequestBody TransferRequest request){
        // TODO: Call TransferService.walletToBank(request)
        return ResponseEntity.status(201).body("Wallet to bank transfer successful.");
    }

}
