package com.qburst.training.personalfinancetracker.controller;

import com.qburst.training.personalfinancetracker.service.BankAccountService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qburst.training.personalfinancetracker.dto.CreateBankAccountRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/bank-accounts")
@Tag(name = "Bank Account Management", description = "Create and manage user bank accounts")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @PostMapping
    @Operation(
            summary = "Create a bank account",
            description = "LInk a new bank account to a user. Accept: userId, bankName, accountNumber, initialBalance "
    )
    @ApiResponse(responseCode = "201", description = "Bank account created successfully")
    public ResponseEntity<String> createBankAccount(@RequestBody CreateBankAccountRequest request){
        return ResponseEntity.status(201).body(bankAccountService.createAccount(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bank account by ID")
    @ApiResponse(responseCode = "200", description = "Bank account found")
    public ResponseEntity<String> getBankAccountById(@PathVariable Long id){
        return ResponseEntity.ok(bankAccountService.getAccountById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get bank accounts for a user")
    @ApiResponse(responseCode = "200", description = "Accounts retrieved")
    public ResponseEntity<String> getAccountsByUser(@PathVariable Long userId){
        return ResponseEntity.ok(bankAccountService.getAccountByUserId(userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a bank account")
    @ApiResponse(responseCode = "200", description = "Bank account deleted")
    public ResponseEntity<String> deleteBankAccount(@PathVariable Long id){
        return ResponseEntity.ok(bankAccountService.deleteAccount(id));
    }
}
