package com.qburst.training.personalfinancetracker.controller;

import com.qburst.training.personalfinancetracker.dto.CreateUserRequest;
import com.qburst.training.personalfinancetracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Register and manage application users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(
            summary = "Create a new user",
            description = "Registers a new user. Accepts: username, email, password, fullName"
    )
    @ApiResponse(responseCode = "201", description = "User created successfully")
    public ResponseEntity<String> createUser(@RequestBody CreateUserRequest request) {
        return ResponseEntity.status(201).body(userService.createUser(request));
    }

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticates a user with email and password"
    )
    @ApiResponse(responseCode = "200", description = "Login successful")
    public ResponseEntity<String> login(@RequestBody CreateUserRequest request) {
        // TODO Day 7: implement auth logic in UserService
        return ResponseEntity.ok("Login successful");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    @ApiResponse(responseCode = "200", description = "User found")
    public ResponseEntity<String> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}