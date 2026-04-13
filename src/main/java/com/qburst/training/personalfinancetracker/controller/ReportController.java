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

    @GetMapping({"/bank-balances", "/users/{userId}/bank-balances"})
    @Operation(summary = "Bank balance summary")
    public ResponseEntity<List<BankAccountDto.BalanceSummary>> getBankBalances(
            @RequestParam(required = false) Long userId,
            @PathVariable(required = false) Long pathUserId) {
        Long effectiveUserId = pathUserId != null ? pathUserId : userId;
        return ResponseEntity.ok(reportService.getBankBalanceSummary(effectiveUserId));
    }

    @GetMapping({"/monthly-expenses", "/expenses/monthly", "/users/{userId}/expenses/monthly"})
    @Operation(summary = "Monthly expense summary")
    public ResponseEntity<List<ReportDto.MonthlyExpense>> getMonthlyExpenses(
            @RequestParam(required = false) Long userId,
            @PathVariable(required = false) Long pathUserId) {
        Long effectiveUserId = pathUserId != null ? pathUserId : userId;
        return ResponseEntity.ok(reportService.getMonthlyExpenses(effectiveUserId));
    }

    @GetMapping({"/expense-by-category", "/expenses/by-category", "/users/{userId}/expenses/by-category"})
    @Operation(summary = "Expense breakdown by category")
    public ResponseEntity<List<ReportDto.CategoryExpense>> getExpenseByCategory(
            @RequestParam(required = false) Long userId,
            @PathVariable(required = false) Long pathUserId) {
        Long effectiveUserId = pathUserId != null ? pathUserId : userId;
        return ResponseEntity.ok(reportService.getExpenseByCategory(effectiveUserId));
    }

    @GetMapping({"/income-expense-summary", "/summary", "/users/{userId}/summary"})
    @Operation(summary = "Income vs expense summary")
    public ResponseEntity<ReportDto.IncomeExpenseSummary> getIncomeExpenseSummary(
            @RequestParam(required = false) Long userId,
            @PathVariable(required = false) Long pathUserId) {
        Long effectiveUserId = pathUserId != null ? pathUserId : userId;
        return ResponseEntity.ok(reportService.getIncomeExpenseSummary(effectiveUserId));
    }

    @GetMapping({"/overview", "/users/{userId}/overview"})
    @Operation(summary = "Full financial report overview")
    public ResponseEntity<ReportDto.Overview> getOverview(
            @RequestParam(required = false) Long userId,
            @PathVariable(required = false) Long pathUserId) {
        Long effectiveUserId = pathUserId != null ? pathUserId : userId;
        return ResponseEntity.ok(reportService.getOverview(effectiveUserId));
    }
}
