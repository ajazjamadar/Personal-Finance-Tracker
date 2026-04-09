package com.qburst.training.personalfinancetracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("Personal Finance Tracker API")
                        .version("1.0.0")
                        .description("REST API for managing personal finance tracker."
                                + "User can create bank accounts,"
                                + "record income and expenses, transfer money to accounts/mobile/UPI, "
                                + "and generate financial report")
                        .contact(new Contact()
                                .name("Ejaz")
                                .email("ejaz@qburst.com")));
    }
}
