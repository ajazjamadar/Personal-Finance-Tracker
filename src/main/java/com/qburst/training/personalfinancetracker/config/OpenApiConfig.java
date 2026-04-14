package com.qburst.training.personalfinancetracker.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(title = "Finance Tracker API", version = "1.0"),
        security = @SecurityRequirement(name = "bearerAuth")
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ))
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Personal Finance Tracker API")
                        .version("1.0.0")
                        .description("REST API for managing personal finance tracker."
                                + "User can create bank accounts,"
                                + "record income and expenses, transfer money to accounts/wallets/mobile/UPI, "
                                + "and generate financial report")
                        .contact(new Contact()
                                .name("Ejaz")
                                .email("ejaz@qburst.com")));
    }
}
