package com.qburst.training.personalfinancetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for creating a new user")
public class CreateUserRequest {

    @Schema(description = "UserName", example = "John")
    private String username;

    @Schema(description = "User emain address", example = "john@email.com")
    private String email;

    @Schema(description = "Password", example = "Secretcode1234")
    private String password;

    @Schema(description = "Fullname", example = "John Doe")
    private String fullName;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
