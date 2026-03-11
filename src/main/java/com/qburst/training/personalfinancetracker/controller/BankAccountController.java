package com.qburst.training.personalfinancetracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qburst.training.personalfinancetracker.dto.CreatingBankAccountRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/bank-accounts")
@Tag(name = "Bank Account Management", description = "Create and manage user bank accounts")
public class BankAccountController {

    @PostMapping
    @Operation(
            summary = "Create a bank account",
            description = "LInk a new bank account to a user. Accept: userId, bankName, accountNumber, initialBalance "
    )

    public ResponseEntity<String> createBankAccount(@RequestBody CreatingBankAccountRequest request){
        // TODO: Call BankAccountService.createBankAccount(request)
        return ResponseEntity.status(201).body("Bank account created successfully.");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bank account by ID")

    public ResponseEntity<String> getBankAccountById(@PathVariable Long id){
        return ResponseEntity.ok("Bank account details for ID:" + id);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get bank accounts for a user")

    public ResponseEntity<String> getAccountsByUser(@PathVariable Long userId){
        return ResponseEntity.ok("All bank accounts for user ID: " + userId);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a bank account")

    public ResponseEntity<String> deleteBankAccount(@PathVariable Long id){
        return ResponseEntity.ok("Bank account ID " + id + "deleted.");
    }
}
