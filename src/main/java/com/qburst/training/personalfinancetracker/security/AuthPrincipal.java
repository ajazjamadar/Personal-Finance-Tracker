package com.qburst.training.personalfinancetracker.security;

import com.qburst.training.personalfinancetracker.entity.UserRole;

public record AuthPrincipal(
        Long userId,
        String email,
        UserRole role
) {
}
