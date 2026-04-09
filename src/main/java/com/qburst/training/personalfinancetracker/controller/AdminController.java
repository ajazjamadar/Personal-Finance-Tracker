package com.qburst.training.personalfinancetracker.controller;

import com.qburst.training.personalfinancetracker.dto.TransactionDto;
import com.qburst.training.personalfinancetracker.dto.UserDto;
import com.qburst.training.personalfinancetracker.service.transaction.TransactionService;
import com.qburst.training.personalfinancetracker.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administrative access APIs")
public class AdminController {

    private final UserService userService;
    private final TransactionService transactionService;

    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserDto.Response>> listUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/activities")
    @Operation(summary = "Get recent activities")
    public ResponseEntity<List<TransactionDto.Response>> listActivities() {
        return ResponseEntity.ok(transactionService.getRecentActivities());
    }
}
