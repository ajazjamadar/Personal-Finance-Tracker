package com.qburst.training.personalfinancetracker.service;

import com.qburst.training.personalfinancetracker.dto.CreateUserRequest;
import com.qburst.training.personalfinancetracker.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Override
    public String createUser(CreateUserRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        return "User created:" + request.getUsername();
    }

    @Override
    public String getUserById(Long id) {
        if (id <= 0) {
            throw new ResourceNotFoundException("User not found with id:" + id);
        }
        return "User found with id:" + id;
    }
}