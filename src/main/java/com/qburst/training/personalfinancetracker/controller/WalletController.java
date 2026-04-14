package com.qburst.training.personalfinancetracker.controller;

import com.qburst.training.personalfinancetracker.dto.WalletDto;
import com.qburst.training.personalfinancetracker.dto.WalletTransactionDto;
import com.qburst.training.personalfinancetracker.service.wallet.WalletService;
import com.qburst.training.personalfinancetracker.service.wallet.WalletTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@Tag(name = "Wallets", description = "Create and manage personal wallets")
public class WalletController {

    private final WalletService walletService;
    private final WalletTransactionService walletTransactionService;

    public WalletController(WalletService walletService,
                            WalletTransactionService walletTransactionService) {
        this.walletService = walletService;
        this.walletTransactionService = walletTransactionService;
    }

    @PostMapping
    @Operation(summary = "Create wallet")
    @ApiResponse(responseCode = "201", description = "Wallet created")
    public ResponseEntity<WalletDto.Response> createWallet(@Valid @RequestBody WalletDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.createWallet(request));
    }

    @GetMapping
    @Operation(summary = "List wallets for current user")
    @ApiResponse(responseCode = "200", description = "Wallets retrieved")
    public ResponseEntity<List<WalletDto.Response>> listWallets() {
        return ResponseEntity.ok(walletService.listWalletsForCurrentUser());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get wallet details")
    @ApiResponse(responseCode = "200", description = "Wallet retrieved")
    public ResponseEntity<WalletDto.Response> getWallet(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.getWalletById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete wallet")
    @ApiResponse(responseCode = "204", description = "Wallet deleted")
    public ResponseEntity<Void> deleteWallet(@PathVariable Long id) {
        walletService.deleteWallet(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "List wallet transactions")
    @ApiResponse(responseCode = "200", description = "Wallet transactions retrieved")
    public ResponseEntity<Page<WalletTransactionDto.Response>> listWalletTransactions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(walletTransactionService.listWalletTransactions(id, page, size));
    }
}
