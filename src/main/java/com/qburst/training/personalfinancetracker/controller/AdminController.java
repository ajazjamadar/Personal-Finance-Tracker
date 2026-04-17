package com.qburst.training.personalfinancetracker.controller;

import com.qburst.training.personalfinancetracker.dto.AdminDashboardDto;
import com.qburst.training.personalfinancetracker.dto.BankAccountDto;
import com.qburst.training.personalfinancetracker.dto.TransactionDto;
import com.qburst.training.personalfinancetracker.dto.UserDto;
import com.qburst.training.personalfinancetracker.service.admin.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administrative access APIs")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard summary")
    public ResponseEntity<AdminDashboardDto.Response> dashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

        @GetMapping("/performance/monthly")
        @Operation(summary = "Get admin monthly performance for a specific month")
        public ResponseEntity<AdminDashboardDto.MonthlyPerformance> monthlyPerformance(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer year,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(adminService.getMonthlyPerformance(year, month));
        }

        @GetMapping("/performance/monthly/export")
        @Operation(summary = "Export admin monthly performance")
        public ResponseEntity<byte[]> exportMonthlyPerformance(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "csv") String format) {
        String normalizedFormat = normalizeFormat(format);
        byte[] payload = "pdf".equals(normalizedFormat)
            ? adminService.exportMonthlyPerformancePdf(year, month)
            : adminService.exportMonthlyPerformanceCsv(year, month);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename("admin-monthly-performance." + normalizedFormat)
                .build()
                .toString())
            .contentType("pdf".equals(normalizedFormat) ? MediaType.APPLICATION_PDF : MediaType.valueOf("text/csv"))
            .body(payload);
        }

    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserDto.Response>> listUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PostMapping("/users")
    @Operation(summary = "Create a user from admin workspace")
    public ResponseEntity<UserDto.Response> createUser(@Valid @RequestBody UserDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(request));
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Update a user from admin workspace")
    public ResponseEntity<UserDto.Response> updateUser(@PathVariable Long id,
                                                       @Valid @RequestBody UserDto.AdminUpdateRequest request) {
        return ResponseEntity.ok(adminService.updateUser(id, request));
    }

    @GetMapping("/accounts")
    @Operation(summary = "Get all bank accounts")
    public ResponseEntity<List<BankAccountDto.Response>> listAccounts() {
        return ResponseEntity.ok(adminService.getAllAccounts());
    }

    @PutMapping("/accounts/{id}")
    @Operation(summary = "Update a bank account from admin workspace")
    public ResponseEntity<BankAccountDto.Response> updateAccount(@PathVariable Long id,
                                                                 @Valid @RequestBody BankAccountDto.AdminUpdateRequest request) {
        return ResponseEntity.ok(adminService.updateAccount(id, request));
    }

    @GetMapping("/activities")
    @Operation(summary = "Get recent activities")
    public ResponseEntity<List<TransactionDto.Response>> listActivities() {
        return ResponseEntity.ok(adminService.getRecentActivities());
    }

    private String normalizeFormat(String format) {
        String normalized = format == null ? "csv" : format.trim().toLowerCase(Locale.ROOT);
        if (!"csv".equals(normalized) && !"pdf".equals(normalized)) {
            throw new IllegalArgumentException("Unsupported format. Use csv or pdf");
        }
        return normalized;
    }
}
