package com.qburst.training.personalfinancetracker.controller;

import com.qburst.training.personalfinancetracker.dto.BankAccountDto;
import com.qburst.training.personalfinancetracker.dto.ReportDto;
import com.qburst.training.personalfinancetracker.service.report.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Financial reporting and analytics")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/bank-balances")
    @Operation(summary = "Bank balance summary")
    public ResponseEntity<List<BankAccountDto.BalanceSummary>> getBankBalances(
            @RequestParam Long userId) {
        return ResponseEntity.ok(reportService.getBankBalanceSummary(userId));
    }

    @GetMapping("/monthly-expenses")
    @Operation(summary = "Monthly expense summary")
    public ResponseEntity<List<ReportDto.MonthlyExpense>> getMonthlyExpenses(
            @RequestParam Long userId) {
        return ResponseEntity.ok(reportService.getMonthlyExpenses(userId));
    }

    @GetMapping("/expense-by-category")
    @Operation(summary = "Expense breakdown by category")
    public ResponseEntity<List<ReportDto.CategoryExpense>> getExpenseByCategory(
            @RequestParam Long userId) {
        return ResponseEntity.ok(reportService.getExpenseByCategory(userId));
    }

    @GetMapping("/income-expense-summary")
    @Operation(summary = "Income vs expense summary")
    public ResponseEntity<ReportDto.IncomeExpenseSummary> getIncomeExpenseSummary(
            @RequestParam Long userId) {
        return ResponseEntity.ok(reportService.getIncomeExpenseSummary(userId));
    }
}