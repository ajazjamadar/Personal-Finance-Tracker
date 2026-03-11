package com.qburst.training.personalfinancetracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qburst.training.personalfinancetracker.dto.CreateUserRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Register and manage application users")
public class UserController {

    @PostMapping
    @Operation(
            summary = "Create a new user",
            description = "Registers a new user. Request will accept: username, email, password, full_name"
    )
    public ResponseEntity<String> createuser(@RequestBody CreateUserRequest request){
        // TODO: Call UserService.createUser(request)
        return ResponseEntity.status(201).body("User Created Successfully");
    }

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticates a user with email and password"
    )
    public ResponseEntity<String> login(@RequestBody CreateUserRequest request){
        // TODO: Call UserService.login(request)
        return ResponseEntity.ok("Login successful");
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get user by ID"
    )

    public ResponseEntity<String> getUserBYid(@PathVariable Long id){
        return ResponseEntity.ok("User details for ID:" + id);
    }
}
