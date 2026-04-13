package com.qburst.training.personalfinancetracker.controller;

import com.qburst.training.personalfinancetracker.dto.TransactionDto;
import com.qburst.training.personalfinancetracker.service.transaction.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Record account income, expenses, and ATM withdrawals")
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
    @Operation(summary = "Record account expense")
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

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get transaction history for a user")
    @ApiResponse(responseCode = "200", description = "Transactions retrieved")
    public ResponseEntity<List<TransactionDto.Response>> getTransactionHistory(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) TransactionDto.HistoryTransactionType transactionType,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) com.qburst.training.personalfinancetracker.entity.Transaction.TransactionStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) com.qburst.training.personalfinancetracker.entity.Transaction.PaymentMethod paymentMethod,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) String receiver) {
        TransactionDto.HistoryFilter filter = new TransactionDto.HistoryFilter(
                fromDate, toDate, transactionType, minAmount, maxAmount,
                status, category, paymentMethod, accountId, receiver);
        return ResponseEntity.ok(transactionService.getTransactionsByUserId(userId, filter));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    @ApiResponse(responseCode = "200", description = "Transaction found")
    public ResponseEntity<TransactionDto.Response> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }
}
