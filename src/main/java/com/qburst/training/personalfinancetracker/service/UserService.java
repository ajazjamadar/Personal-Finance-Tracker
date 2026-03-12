package com.qburst.training.personalfinancetracker.service;

import com.qburst.training.personalfinancetracker.dto.CreateUserRequest;

    public interface UserService{
        String createUser(CreateUserRequest request);
        String getUserById(Long Id);
    }
