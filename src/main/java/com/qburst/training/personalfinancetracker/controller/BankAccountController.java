package com.qburst.training.personalfinancetracker.controller;

import com.qburst.training.personalfinancetracker.dto.BankAccountDto;
import com.qburst.training.personalfinancetracker.service.account.BankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bank-accounts")
@Tag(name = "Bank Account", description = "Create and manage user bank accounts")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @PostMapping
    @Operation(summary = "Create a bank account")
    @ApiResponse(responseCode = "201", description = "Bank account created successfully")
    public ResponseEntity<BankAccountDto.Response> createBankAccount(
            @Valid @RequestBody BankAccountDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bankAccountService.createBankAccount(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bank account by ID")
    @ApiResponse(responseCode = "200", description = "Bank account found")
    public ResponseEntity<BankAccountDto.Response> getBankAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(bankAccountService.getBankAccountById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all bank accounts for a user")
    @ApiResponse(responseCode = "200", description = "Accounts retrieved")
    public ResponseEntity<List<BankAccountDto.Response>> getAccountsByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(bankAccountService.getBankAccountsByUserId(userId));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search accounts by bank name or user full name")
    @ApiResponse(responseCode = "200", description = "Matching accounts retrieved")
    public ResponseEntity<List<BankAccountDto.Response>> searchAccounts(
            @RequestParam(required = false) String bankName,
            @RequestParam(required = false) String fullName) {
        return ResponseEntity.ok(bankAccountService.searchAccounts(bankName, fullName));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a bank account")
    @ApiResponse(responseCode = "204", description = "Bank account deleted")
    public ResponseEntity<Void> deleteBankAccount(@PathVariable Long id) {
        bankAccountService.deleteBankAccount(id);
        return ResponseEntity.noContent().build();
    }
}