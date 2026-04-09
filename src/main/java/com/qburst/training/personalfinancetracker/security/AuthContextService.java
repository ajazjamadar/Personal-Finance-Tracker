package com.qburst.training.personalfinancetracker.security;

import com.qburst.training.personalfinancetracker.entity.UserRole;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AuthContextService {

    public Long currentUserId() {
        AuthPrincipal principal = currentPrincipal();
        return principal.userId();
    }

    public String currentEmail() {
        AuthPrincipal principal = currentPrincipal();
        return principal.email();
    }

    public UserRole currentRole() {
        AuthPrincipal principal = currentPrincipal();
        return principal.role();
    }

    public boolean isAdmin() {
        return currentRole() == UserRole.ADMIN;
    }

    public Long resolveUserId(Long requestedUserId) {
        if (isAdmin()) {
            if (requestedUserId == null) {
                throw new IllegalArgumentException("User ID is required for admin requests");
            }
            return requestedUserId;
        }
        return currentUserId();
    }

    public void ensureCanAccessUser(Long targetUserId) {
        if (isAdmin()) {
            return;
        }
        if (!Objects.equals(currentUserId(), targetUserId)) {
            throw new AccessDeniedException("You can access only your own data");
        }
    }

    private AuthPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AccessDeniedException("Authentication is required");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthPrincipal authPrincipal) {
            return authPrincipal;
        }

        throw new AccessDeniedException("Invalid authentication context");
    }
}
