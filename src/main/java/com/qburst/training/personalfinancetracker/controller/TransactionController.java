package com.qburst.training.personalfinancetracker.controller;

import com.qburst.training.personalfinancetracker.dto.TransactionDto;
import com.qburst.training.personalfinancetracker.service.transaction.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Record income, expenses, ATM withdrawals and bank payments")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/income")
    @Operation(summary = "Record income")
    @ApiResponse(responseCode = "201", description = "Income recorded")
    public ResponseEntity<TransactionDto.Response> recordIncome(
            @Valid @RequestBody TransactionDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.recordIncome(request));
    }

    @PostMapping("/expense")
    @Operation(summary = "Record wallet expense")
    @ApiResponse(responseCode = "201", description = "Expense recorded")
    public ResponseEntity<TransactionDto.Response> recordExpense(
            @Valid @RequestBody TransactionDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.recordExpense(request));
    }

    @PostMapping("/atm-withdrawal")
    @Operation(summary = "ATM withdrawal")
    @ApiResponse(responseCode = "201", description = "Withdrawal recorded")
    public ResponseEntity<TransactionDto.Response> recordAtmWithdrawal(
            @Valid @RequestBody TransactionDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.recordAtmWithdrawal(request));
    }

    @PostMapping("/bank-expense")
    @Operation(summary = "Bank expense payment")
    @ApiResponse(responseCode = "201", description = "Bank expense recorded")
    public ResponseEntity<TransactionDto.Response> recordBankExpense(
            @Valid @RequestBody TransactionDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.recordBankExpense(request));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get transaction history for a user")
    @ApiResponse(responseCode = "200", description = "Transactions retrieved")
    public ResponseEntity<List<TransactionDto.Response>> getTransactionHistory(
            @PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getTransactionsByUserId(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    @ApiResponse(responseCode = "200", description = "Transaction found")
    public ResponseEntity<TransactionDto.Response> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }
}