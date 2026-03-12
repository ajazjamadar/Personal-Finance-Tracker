package com.qburst.training.personalfinancetracker.controller;

import com.qburst.training.personalfinancetracker.service.WalletService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qburst.training.personalfinancetracker.dto.CreateWalletRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/wallets")
@Tag(name = "Wallet Management", description = "Create and manage digital wallets")
public class WalletController {

    public final WalletService walletService;

    public WalletController(WalletService walletService){
        this.walletService = walletService;
    }

    @PostMapping
    @Operation(
            summary = "Create a wallet",
            description = "Create a new degital wallet for a user. Accets: userId, walletName. Initial balance starts at 0.00"
    )

    public ResponseEntity<String> createWallet(@RequestBody CreateWalletRequest request){
        walletService.createWallet(request);
        return ResponseEntity.status(201).body("Wallet created successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get wallet by ID")

    public ResponseEntity<String> getWalletById(@PathVariable Long id){
        return ResponseEntity.ok(walletService.getWalletById(id));

    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all wallets for a user")

    public ResponseEntity<String> getWalletsByUser(@PathVariable Long userId){
        return ResponseEntity.ok(walletService.getWalletsByUser(userId));
    }


}
