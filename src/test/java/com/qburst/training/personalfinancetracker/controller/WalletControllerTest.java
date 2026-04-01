package com.qburst.training.personalfinancetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qburst.training.personalfinancetracker.dto.WalletDto;
import com.qburst.training.personalfinancetracker.entity.User;
import com.qburst.training.personalfinancetracker.entity.Wallet;
import com.qburst.training.personalfinancetracker.repository.UserRepository;
import com.qburst.training.personalfinancetracker.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class WalletControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private WalletRepository walletRepository;

    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .username("ejaz")
                .email("ejaz@qburst.com")
                .passwordHash("hashed")
                .fullName("Ejaz Jamadar")
                .build());

        wallet = walletRepository.save(Wallet.builder()
                .user(user)
                .walletName("My Wallet")
                .balance(new BigDecimal("1000.00"))
                .build());
    }

    @Test
    @DisplayName("POST /api/wallets: should create wallet and return 201")
    void createWallet_shouldReturn201() throws Exception {
        WalletDto.Request request = new WalletDto.Request(
                user.getId(), "Travel Wallet", new BigDecimal("500.00"));

        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.walletName").value("Travel Wallet"))
                .andExpect(jsonPath("$.balance").value(500.00))
                .andExpect(jsonPath("$.userId").value(user.getId()));
    }

    @Test
    @DisplayName("POST /api/wallets: should return 404 when user not found")
    void createWallet_shouldReturn404_whenUserNotFound() throws Exception {
        WalletDto.Request request = new WalletDto.Request(
                999999L, "Ghost Wallet", new BigDecimal("100.00"));

        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/wallets: should return 400 when request is invalid")
    void createWallet_shouldReturn400_whenInvalidRequest() throws Exception {
        String invalidJson = """
                {
                  "userId": null,
                  "walletName": "",
                  "initialBalance": -100
                }
                """;

        mockMvc.perform(post("/api/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/wallets/{id}: should return wallet when found")
    void getWalletById_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/wallets/{id}", wallet.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletName").value("My Wallet"))
                .andExpect(jsonPath("$.balance").value(1000.00));
    }

    @Test
    @DisplayName("GET /api/wallets/{id}: should return 404 when not found")
    void getWalletById_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/wallets/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/wallets/user/{userId}: should return all wallets for user")
    void getWalletsByUser_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/wallets/user/{userId}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].walletName").value("My Wallet"));
    }

    @Test
    @DisplayName("DELETE /api/wallets/{id}: should delete wallet and return 204")
    void deleteWallet_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/wallets/{id}", wallet.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/wallets/{id}: should return 404 when wallet not found")
    void deleteWallet_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(delete("/api/wallets/{id}", 999999L))
                .andExpect(status().isNotFound());
    }
}