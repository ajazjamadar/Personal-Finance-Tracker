package com.qburst.training.personalfinancetracker.controller;

import com.qburst.training.personalfinancetracker.service.TransactionService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    //Income
    @PostMapping("/income")
    @Operation(
            summary = "Record income",
            description = "Adds income (salary, freelance, refunds) to bank account or wallets"
    )
    @ApiResponse(responseCode = "201", description = "Income recorded")
    public ResponseEntity<String> recordIncome(@RequestBody TransactionRequest request){
        return ResponseEntity.status(201).body(transactionService.recordIncome(request));
    }

    //Wallet
    @PostMapping("/expense")
    @Operation(
            summary = "Record wallet expense",
            description = "Records an expense paid from a wallet. Accepts: walletId, amount, category, description"
    )
    @ApiResponse(responseCode = "201", description = "Expense recorded")
    public ResponseEntity<String> recordExpense(@RequestBody TransactionRequest request){
        return ResponseEntity.status(201).body(transactionService.recordExpense(request));
    }

    //ATM Withdrawal
    @PostMapping("/atm-withdrawal")
    @Operation(
            summary = "ATM withdrawal",
            description = "Withdrwal cash from a bank account. Accepts: bankAccountId, amount"
    )
    @ApiResponse(responseCode = "201", description = "Withdrawal recorded")
    public ResponseEntity<String> atmWithdrawal(@RequestBody TransactionRequest request){
        return ResponseEntity.status(201).body(transactionService.recordAtmWithdrawal(request));
    }

    //Bank Expense Payment
    @PostMapping("/bank-expense")
    @Operation(
            summary = "Bank expense payment",
            description = "Pays an expense directly form a bank account. Accepts: bankAccountId, amount, category, description"
    )

    public ResponseEntity<String> recordBankExpense(@RequestBody TransactionRequest request){
        return ResponseEntity.status(201).body(transactionService.recordBankExpense(request));
    }

    //History
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get transaction history for a user")

    public ResponseEntity<String> getTransactionHistory(@PathVariable Long userId){
        return ResponseEntity.ok(transactionService.getTransactionsByUserId(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transactions by ID")

    public ResponseEntity<String> getTransactionById(@PathVariable Long id){
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }
}
