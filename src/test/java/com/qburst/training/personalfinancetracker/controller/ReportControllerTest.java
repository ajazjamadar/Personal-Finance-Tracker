package com.qburst.training.personalfinancetracker.controller;

import com.qburst.training.personalfinancetracker.entity.*;
import com.qburst.training.personalfinancetracker.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReportControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private BankRepository bankRepository;
    @Autowired private BankAccountRepository bankAccountRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private TransactionRepository transactionRepository;

    private User user;

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

        bankAccountRepository.save(BankAccount.builder()
                .user(user)
                .bank(bank)
                .accountNumber("ACC-001")
                .balance(new BigDecimal("5000.00"))
                .build());

        Wallet wallet = walletRepository.save(Wallet.builder()
                .user(user)
                .walletName("My Wallet")
                .balance(new BigDecimal("1000.00"))
                .build());

        transactionRepository.save(Transaction.builder()
                .user(user)
                .sourceWallet(wallet)
                .transactionType(Transaction.TransactionType.INCOME)
                .amount(new BigDecimal("3000.00"))
                .description("Salary")
                .build());

        transactionRepository.save(Transaction.builder()
                .user(user)
                .sourceWallet(wallet)
                .transactionType(Transaction.TransactionType.EXPENSE)
                .amount(new BigDecimal("500.00"))
                .description("Groceries")
                .build());
    }

    @Test
    @DisplayName("GET /api/reports/bank-balances: should return bank balance summary")
    void getBankBalances_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/reports/bank-balances")
                        .param("userId", user.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bankName").value("HDFC Bank"))
                .andExpect(jsonPath("$[0].accountNumber").value("ACC-001"))
                .andExpect(jsonPath("$[0].balance").value(5000.00));
    }

    @Test
    @DisplayName("GET /api/reports/bank-balances: should return 404 when user not found")
    void getBankBalances_shouldReturn404_whenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/reports/bank-balances")
                        .param("userId", "999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/reports/monthly-expenses: should return monthly expense summary")
    void getMonthlyExpenses_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/reports/monthly-expenses")
                        .param("userId", user.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].totalExpense").value(500.00));
    }

    @Test
    @DisplayName("GET /api/reports/monthly-expenses: should return 404 when user not found")
    void getMonthlyExpenses_shouldReturn404_whenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/reports/monthly-expenses")
                        .param("userId", "999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/reports/expense-by-category: should return expense by category")
    void getExpenseByCategory_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/reports/expense-by-category")
                        .param("userId", user.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("Groceries"))
                .andExpect(jsonPath("$[0].totalExpense").value(500.00));
    }

    @Test
    @DisplayName("GET /api/reports/expense-by-category: should return 404 when user not found")
    void getExpenseByCategory_shouldReturn404_whenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/reports/expense-by-category")
                        .param("userId", "999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/reports/income-expense-summary: should return income vs expense summary")
    void getIncomeExpenseSummary_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/reports/income-expense-summary")
                        .param("userId", user.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(3000.00))
                .andExpect(jsonPath("$.totalExpense").value(500.00))
                .andExpect(jsonPath("$.netSavings").value(2500.00));
    }

    @Test
    @DisplayName("GET /api/reports/income-expense-summary: should return 404 when user not found")
    void getIncomeExpenseSummary_shouldReturn404_whenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/reports/income-expense-summary")
                        .param("userId", "999999"))
                .andExpect(status().isNotFound());
    }
}