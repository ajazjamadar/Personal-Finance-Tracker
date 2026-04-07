package com.qburst.training.personalfinancetracker.controller;

import com.qburst.training.personalfinancetracker.dto.UserDto;
import com.qburst.training.personalfinancetracker.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Manage user profile and account information")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a user")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    public ResponseEntity<UserDto.Response> createUser(@Valid @RequestBody UserDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user profile by ID")
    @ApiResponse(responseCode = "200", description = "User found")
    public ResponseEntity<UserDto.Response> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
