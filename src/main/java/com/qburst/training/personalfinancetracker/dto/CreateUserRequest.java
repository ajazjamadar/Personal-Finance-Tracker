package com.qburst.training.personalfinancetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Request body for creating a new user")
@Getter
@Setter
public class CreateUserRequest {

    @Schema(description = "UserName", example = "John")
    private String username;

    @Schema(description = "User emain address", example = "john@email.com")
    private String email;

    @Schema(description = "Password", example = "Secretcode1234")
    private String password;

    @Schema(description = "Fullname", example = "John Doe")
    private String fullName;

}
