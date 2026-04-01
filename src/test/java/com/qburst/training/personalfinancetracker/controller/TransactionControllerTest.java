package com.qburst.training.personalfinancetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qburst.training.personalfinancetracker.dto.TransactionDto;
import com.qburst.training.personalfinancetracker.entity.*;
import com.qburst.training.personalfinancetracker.repository.*;
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
class TransactionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private BankRepository bankRepository;
    @Autowired private BankAccountRepository bankAccountRepository;
    @Autowired private WalletRepository walletRepository;

    private User user;
    private BankAccount bankAccount;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .username("ejaz")
                .email("ejaz@qburst.com")
                .passwordHash("hashed")
                .fullName("Ejaz Jamadar")
                .build());

        Bank bank = bankRepository.findByBankNameIgnoreCase("HDFC Bank")
                .orElseGet(() -> bankRepository.save(Bank.builder()
                        .bankName("HDFC Bank")
                        .bankCode("HDFC")
                        .build()));

        bankAccount = bankAccountRepository.save(BankAccount.builder()
                .user(user)
                .bank(bank)
                .accountNumber("ACC-001")
                .balance(new BigDecimal("10000.00"))
                .build());

        wallet = walletRepository.save(Wallet.builder()
                .user(user)
                .walletName("My Wallet")
                .balance(new BigDecimal("5000.00"))
                .build());
    }

    @Test
    @DisplayName("POST /api/transactions/income: should record income and return 201")
    void recordIncome_shouldReturn201() throws Exception {
        TransactionDto.Request request = new TransactionDto.Request(
                wallet.getId(), null, new BigDecimal("3000.00"), "SALARY", "Monthly salary");

        mockMvc.perform(post("/api/transactions/income")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType").value("INCOME"))
                .andExpect(jsonPath("$.amount").value(3000.00))
                .andExpect(jsonPath("$.description").value("Monthly salary"));
    }

    @Test
    @DisplayName("POST /api/transactions/expense: should record expense and return 201")
    void recordExpense_shouldReturn201() throws Exception {
        TransactionDto.Request request = new TransactionDto.Request(
                wallet.getId(), null, new BigDecimal("500.00"), "FOOD", "Lunch");

        mockMvc.perform(post("/api/transactions/expense")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType").value("EXPENSE"))
                .andExpect(jsonPath("$.amount").value(500.00));
    }

    @Test
    @DisplayName("POST /api/transactions/expense: should fail when wallet has insufficient balance")
    void recordExpense_shouldFail_whenInsufficientBalance() throws Exception {
        TransactionDto.Request request = new TransactionDto.Request(
                wallet.getId(), null, new BigDecimal("99999.00"), "FOOD", "Too expensive");

        mockMvc.perform(post("/api/transactions/expense")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /api/transactions/atm-withdrawal: should record withdrawal and return 201")
    void recordAtmWithdrawal_shouldReturn201() throws Exception {
        TransactionDto.Request request = new TransactionDto.Request(
                null, bankAccount.getId(), new BigDecimal("1000.00"), null, "ATM withdrawal");

        mockMvc.perform(post("/api/transactions/atm-withdrawal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType").value("ATM_WITHDRAWAL"))
                .andExpect(jsonPath("$.amount").value(1000.00));
    }

    @Test
    @DisplayName("POST /api/transactions/bank-expense: should record bank expense and return 201")
    void recordBankExpense_shouldReturn201() throws Exception {
        TransactionDto.Request request = new TransactionDto.Request(
                null, bankAccount.getId(), new BigDecimal("2000.00"), "BILLS", "Electricity bill");

        mockMvc.perform(post("/api/transactions/bank-expense")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType").value("EXPENSE"))
                .andExpect(jsonPath("$.amount").value(2000.00));
    }

    @Test
    @DisplayName("GET /api/transactions/user/{userId}: should return transaction history")
    void getTransactionHistory_shouldReturn200() throws Exception {
        // Record one transaction first
        TransactionDto.Request request = new TransactionDto.Request(
                wallet.getId(), null, new BigDecimal("100.00"), "MISC", "Test");
        mockMvc.perform(post("/api/transactions/income")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/transactions/user/{userId}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/transactions/user/{userId}: should return 404 when user not found")
    void getTransactionHistory_shouldReturn404_whenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/transactions/user/{userId}", 999999L))
                .andExpect(status().isNotFound());
    }
}