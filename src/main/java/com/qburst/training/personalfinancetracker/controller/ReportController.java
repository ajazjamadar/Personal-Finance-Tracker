package com.qburst.training.personalfinancetracker.controller;

import com.qburst.training.personalfinancetracker.dto.BankAccountDto;
import com.qburst.training.personalfinancetracker.dto.ReportDto;
import com.qburst.training.personalfinancetracker.service.report.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

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
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(reportService.getBankBalanceSummary(userId));
    }

    @GetMapping("/monthly-expenses")
    @Operation(summary = "Monthly expense summary")
    public ResponseEntity<List<ReportDto.MonthlyExpense>> getMonthlyExpenses(
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(reportService.getMonthlyExpenses(userId));
    }

    @GetMapping("/expense-by-category")
    @Operation(summary = "Expense breakdown by category")
    public ResponseEntity<List<ReportDto.CategoryExpense>> getExpenseByCategory(
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(reportService.getExpenseByCategory(userId));
    }

    @GetMapping("/income-expense-summary")
    @Operation(summary = "Income vs expense summary")
    public ResponseEntity<ReportDto.IncomeExpenseSummary> getIncomeExpenseSummary(
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(reportService.getIncomeExpenseSummary(userId));
    }

    @GetMapping("/overview")
    @Operation(summary = "Full financial report overview")
    public ResponseEntity<ReportDto.Overview> getOverview(
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(reportService.getOverview(userId));
    }

    @GetMapping("/bank-statement/export")
    @Operation(summary = "Export bank statement")
    public ResponseEntity<byte[]> exportBankStatement(
            @RequestParam(required = false) Long userId,
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate,
            @RequestParam(defaultValue = "csv") String format) {
        String normalizedFormat = normalizeFormat(format);
        byte[] payload = "pdf".equals(normalizedFormat)
                ? reportService.exportBankStatementPdf(userId, fromDate, toDate)
                : reportService.exportBankStatementCsv(userId, fromDate, toDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition("bank-statement", normalizedFormat))
                .contentType("pdf".equals(normalizedFormat) ? MediaType.APPLICATION_PDF : MediaType.valueOf("text/csv"))
                .body(payload);
    }

    @GetMapping("/overview/export")
    @Operation(summary = "Export overall financial overview")
    public ResponseEntity<byte[]> exportOverview(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "csv") String format) {
        String normalizedFormat = normalizeFormat(format);
        byte[] payload = "pdf".equals(normalizedFormat)
                ? reportService.exportOverviewPdf(userId)
                : reportService.exportOverviewCsv(userId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition("overall-report", normalizedFormat))
                .contentType("pdf".equals(normalizedFormat) ? MediaType.APPLICATION_PDF : MediaType.valueOf("text/csv"))
                .body(payload);
    }

    private String normalizeFormat(String format) {
        String normalized = format == null ? "csv" : format.trim().toLowerCase(Locale.ROOT);
        if (!"csv".equals(normalized) && !"pdf".equals(normalized)) {
            throw new IllegalArgumentException("Unsupported format. Use csv or pdf");
        }
        return normalized;
    }

    private String contentDisposition(String prefix, String extension) {
        return ContentDisposition.attachment()
                .filename(prefix + "." + extension)
                .build()
                .toString();
    }
}
