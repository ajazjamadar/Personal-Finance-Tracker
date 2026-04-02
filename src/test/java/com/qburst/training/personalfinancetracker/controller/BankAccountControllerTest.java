package com.qburst.training.personalfinancetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qburst.training.personalfinancetracker.dto.BankAccountDto;
import com.qburst.training.personalfinancetracker.entity.Bank;
import com.qburst.training.personalfinancetracker.entity.BankAccount;
import com.qburst.training.personalfinancetracker.entity.User;
import com.qburst.training.personalfinancetracker.repository.BankAccountRepository;
import com.qburst.training.personalfinancetracker.repository.BankRepository;
import com.qburst.training.personalfinancetracker.repository.UserRepository;
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
class BankAccountControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private BankRepository bankRepository;
    @Autowired private BankAccountRepository bankAccountRepository;

    private User user;
    private Bank bank;
    private BankAccount bankAccount;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .username("ejaz")
                .email("ejaz@qburst.com")
                .passwordHash("hashed")
                .fullName("Ejaz Jamadar")
                .build());

        bank = bankRepository.findByBankNameIgnoreCase("HDFC Bank")
                .orElseGet(() -> bankRepository.save(Bank.builder()
                        .bankName("HDFC Bank")
                        .bankCode("HDFC")
                        .build()));

        bankAccount = bankAccountRepository.save(BankAccount.builder()
                .user(user)
                .bank(bank)
                .accountNumber("ACC-001")
                .balance(new BigDecimal("5000.00"))
                .build());
    }

    @Test
    @DisplayName("POST /api/bank-accounts: should create bank account and return 201")
    void createBankAccount_shouldReturn201() throws Exception {
        BankAccountDto.Request request = new BankAccountDto.Request(
                user.getId(), "HDFC Bank", "ACC-002", new BigDecimal("2000.00"));

        mockMvc.perform(post("/api/bank-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value("ACC-002"))
                .andExpect(jsonPath("$.balance").value(2000.00))
                .andExpect(jsonPath("$.bankName").value("HDFC Bank"));
    }

    @Test
    @DisplayName("POST /api/bank-accounts: should return 409 when account number is duplicate")
    void createBankAccount_shouldFail_whenDuplicateAccountNumber() throws Exception {
        BankAccountDto.Request request = new BankAccountDto.Request(
                user.getId(), "HDFC Bank", "ACC-001", new BigDecimal("1000.00"));

        mockMvc.perform(post("/api/bank-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/bank-accounts: should return 404 when bank not found")
    void createBankAccount_shouldReturn404_whenBankNotFound() throws Exception {
        BankAccountDto.Request request = new BankAccountDto.Request(
                user.getId(), "Ghost Bank", "ACC-003", new BigDecimal("1000.00"));

        mockMvc.perform(post("/api/bank-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/bank-accounts/{id}: should return account when found")
    void getBankAccountById_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/bank-accounts/{id}", bankAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("ACC-001"))
                .andExpect(jsonPath("$.balance").value(5000.00));
    }

    @Test
    @DisplayName("GET /api/bank-accounts/{id}: should return 404 when not found")
    void getBankAccountById_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/bank-accounts/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/bank-accounts/user/{userId}: should return accounts for user")
    void getAccountsByUser_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/bank-accounts/user/{userId}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].accountNumber").value("ACC-001"));
    }

    @Test
    @DisplayName("GET /api/bank-accounts/search: should return accounts by bank name")
    void searchAccounts_byBankName_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/bank-accounts/search")
                        .param("bankName", "HDFC Bank"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bankName").value("HDFC Bank"));
    }

    @Test
    @DisplayName("GET /api/bank-accounts/search: should return accounts by full name")
    void searchAccounts_byFullName_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/bank-accounts/search")
                        .param("fullName", "Ejaz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("DELETE /api/bank-accounts/{id}: should delete account and return 204")
    void deleteBankAccount_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/bank-accounts/{id}", bankAccount.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/bank-accounts/{id}: should return 404 when not found")
    void deleteBankAccount_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/bank-accounts/{id}", 999999L))
                .andExpect(status().isNotFound());
    }
}