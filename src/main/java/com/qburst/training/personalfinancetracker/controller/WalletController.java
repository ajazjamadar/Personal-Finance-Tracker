package com.qburst.training.personalfinancetracker.controller;

import com.qburst.training.personalfinancetracker.dto.WalletDto;
import com.qburst.training.personalfinancetracker.service.wallet.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@Tag(name = "Wallet Management", description = "Create and manage digital wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    @Operation(summary = "Create a wallet")
    @ApiResponse(responseCode = "201", description = "Wallet created successfully")
    public ResponseEntity<WalletDto.Response> createWallet(
            @Valid @RequestBody WalletDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(walletService.createWallet(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get wallet by ID")
    @ApiResponse(responseCode = "200", description = "Wallet found")
    public ResponseEntity<WalletDto.Response> getWalletById(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.getWalletById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all wallets for a user")
    @ApiResponse(responseCode = "200", description = "Wallets retrieved")
    public ResponseEntity<List<WalletDto.Response>> getWalletsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getWalletsByUserId(userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a wallet")
    @ApiResponse(responseCode = "204", description = "Wallet deleted")
    public ResponseEntity<Void> deleteWallet(@PathVariable Long id) {
        walletService.deleteWallet(id);
        return ResponseEntity.noContent().build();
    }
}