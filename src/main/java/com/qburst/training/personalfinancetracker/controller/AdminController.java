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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
