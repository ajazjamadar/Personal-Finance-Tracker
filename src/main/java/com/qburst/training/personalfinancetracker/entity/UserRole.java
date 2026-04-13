package com.qburst.training.personalfinancetracker.entity;

import java.util.Locale;

public enum UserRole {
    USER,
    ADMIN;

    public static UserRole fromValue(String rawRole) {
        if (rawRole == null) {
            throw new IllegalArgumentException("Role value cannot be null");
        }

        String normalized = rawRole.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }

        return UserRole.valueOf(normalized);
    }
}
