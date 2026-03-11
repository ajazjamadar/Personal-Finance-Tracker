package com.qburst.training.personalfinancetracker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/reports")
@Tag(name= "Report & Analytics", description = "Financial reporting and analytics APIs")
public class ReportController {

    @GetMapping("/bank-balances")
    @Operation(
            summary = "Bank balance summary",
            description = "Returns current balance of all bank accounts. Response: [{ bankName, balance }]"
    )
    public ResponseEntity<List<Map<String, Object>>> getBankBalances() {
        // TODO: Call ReportService.getBankBalanceSummary()
        // TODO: Use JPQL aggregation query across bank_accounts table
        return ResponseEntity.ok(List.of(
                Map.of("bankName", "HDFC Bank", "balance", 50000),
                Map.of("bankName", "SBI Bank", "balance", 25000)
        ));
    }

    @GetMapping("/monthly-expenses")
    @Operation(
            summary = "Monthly expense summary",
            description = "Returns total expenses grouped by month for a user"
    )
    public ResponseEntity<String> getMonthlyExpenses(@RequestParam Long userId) {
        // TODO: Call ReportService.getMonthlyExpenses(userId)
        // TODO: Use monthly_financial_report VIEW or JPQL GROUP BY DATE
        return ResponseEntity.ok("Monthly expense summary for user ID: " + userId);
    }

    @GetMapping("/expense-by-category")
    @Operation(
            summary = "Expense by category",
            description = "Returns expenses grouped by category (Food, Transport, Shopping, etc.)"
    )
    public ResponseEntity<String> getExpenseByCategory(@RequestParam Long userId) {
        // TODO: Call ReportService.getExpenseByCategory(userId)
        // TODO: GROUP BY category_id JOIN categories table
        return ResponseEntity.ok("Expense by category for user ID: " + userId);
    }

    @GetMapping("/income-expense-summary")
    @Operation(
            summary = "Income vs Expense summary",
            description = "Returns total income, total expense, and net savings for a user"
    )
    public ResponseEntity<String> getIncomeExpenseSummary(@RequestParam Long userId) {
        // TODO: Call ReportService.getIncomeExpenseSummary(userId)
        // TODO: Return { totalIncome, totalExpense, netSavings }
        return ResponseEntity.ok("Income vs Expense summary for user ID: " + userId);
    }
}

