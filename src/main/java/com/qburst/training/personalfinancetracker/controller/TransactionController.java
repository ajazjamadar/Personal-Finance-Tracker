package com.qburst.training.personalfinancetracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qburst.training.personalfinancetracker.dto.TransactionRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/transactions")
@Tag(name="Transactions", description = "Record income, expenses, ATM withdrawals, and bank payments")
public class TransactionController {

    //Income
    @PostMapping("/income")
    @Operation(
            summary = "Record income",
            description = "Adds income (salary, freelance, refunds) to bank account or wallets"
    )

    public ResponseEntity<String> recordIncome(@RequestBody TransactionRequest request){
        // TODO: Call TransactionService.recordIncome(request)
        return ResponseEntity.status(201).body("Income recorded successfully");
    }

    //Wallet
    @PostMapping("/expense")
    @Operation(
            summary = "Record wallet expense",
            description = "Records an expense paid from a wallet. Accepts: walletId, amount, category, description"
    )

    public ResponseEntity<String> recordExpense(@RequestBody TransactionRequest request){
        // TODO: Call TransactionService.recordExpense(request)
        return ResponseEntity.status(201).body("Expense recorded successfully.");
    }

    //ATM Withdrawal
    @PostMapping("/atm-withdrawal")
    @Operation(
            summary = "ATM withdrawal",
            description = "Withdrwal cash from a bank account. Accepts: bankAccountId, amount"
    )

    public ResponseEntity<String> atmWithdrawal(@RequestBody TransactionRequest request){
        // TODO: Call TransactionService.atmWithdrawal(request)
        return ResponseEntity.status(201).body("ATM withdrawal recorded successfully");
    }

    //Bank Expense Payment
    @PostMapping("/bank-expense")
    @Operation(
            summary = "Bank expense payment",
            description = "Pays an expense directly form a bank account. Accepts: bankAccountId, amount, category, description"
    )

    public ResponseEntity<String> recordBankExpense(@RequestBody TransactionRequest request){
        // TODO: Call TransactionService.recordBankExpense(request)
        return ResponseEntity.status(201).body("Bank expense recorded successfully.");
    }

    //History
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get transaction history for a user")

    public ResponseEntity<String> getTransactionHistory(@PathVariable Long userId){
        return ResponseEntity.ok("Transaction history details for ID: " + userId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transactions by ID")

    public ResponseEntity<String> getTransactionById(@PathVariable Long id){
        return ResponseEntity.ok("TRansaction details for ID:" + id);
    }
}
