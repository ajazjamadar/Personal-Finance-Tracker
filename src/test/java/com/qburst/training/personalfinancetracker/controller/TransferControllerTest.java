package com.qburst.training.personalfinancetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qburst.training.personalfinancetracker.dto.TransferDto;
import com.qburst.training.personalfinancetracker.entity.Bank;
import com.qburst.training.personalfinancetracker.entity.BankAccount;
import com.qburst.training.personalfinancetracker.entity.User;
import com.qburst.training.personalfinancetracker.entity.Wallet;
import com.qburst.training.personalfinancetracker.repository.BankAccountRepository;
import com.qburst.training.personalfinancetracker.repository.BankRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TransferControllerTest {

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
                .balance(new BigDecimal("2000.00"))
                .build());
    }

    // ─── Bank → Wallet ────────────────────────────────────────────────────────

    @Test
        @DisplayName("POST /bank-to-wallet: should transfer and return 201")
        void bankToWallet_shouldReturn201() throws Exception {
        TransferDto.Request request = new TransferDto.Request(
                bankAccount.getId(), wallet.getId(), new BigDecimal("3000.00"));

        mockMvc.perform(post("/api/transfers/bank-to-wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType").value("TRANSFER"))
                .andExpect(jsonPath("$.amount").value(3000.00))
                .andExpect(jsonPath("$.description").value("Bank to wallet transfer"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    @DisplayName("POST /bank-to-wallet: should return 422 when bank has insufficient balance")
    void bankToWallet_shouldFail_whenInsufficientBalance() throws Exception {
        TransferDto.Request request = new TransferDto.Request(
                bankAccount.getId(), wallet.getId(), new BigDecimal("99999.00"));

        mockMvc.perform(post("/api/transfers/bank-to-wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /bank-to-wallet: should return 404 when bank account not found")
    void bankToWallet_shouldReturn404_whenBankAccountNotFound() throws Exception {
        TransferDto.Request request = new TransferDto.Request(
                999999L, wallet.getId(), new BigDecimal("100.00"));

        mockMvc.perform(post("/api/transfers/bank-to-wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Bank account not found with id: 999999"));
    }

    @Test
    @DisplayName("POST /bank-to-wallet: should return 404 when wallet not found")
    void bankToWallet_shouldReturn404_whenWalletNotFound() throws Exception {
        TransferDto.Request request = new TransferDto.Request(
                bankAccount.getId(), 999999L, new BigDecimal("100.00"));

        mockMvc.perform(post("/api/transfers/bank-to-wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Wallet not found with id: 999999"));
    }

    @Test
    @DisplayName("POST /bank-to-wallet: should return 400 when request is invalid")
    void bankToWallet_shouldReturn400_whenRequestIsInvalid() throws Exception {
        // amount is missing — violates @NotNull
        String invalidJson = """
                {
                  "sourceId": %d,
                  "destinationId": %d
                }
                """.formatted(bankAccount.getId(), wallet.getId());

        mockMvc.perform(post("/api/transfers/bank-to-wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    // ─── Wallet → Bank ────────────────────────────────────────────────────────

    @Test
        @DisplayName("POST /wallet-to-bank: should transfer and return 201")
        void walletToBank_shouldReturn201() throws Exception {
        TransferDto.Request request = new TransferDto.Request(
                wallet.getId(), bankAccount.getId(), new BigDecimal("500.00"));

        mockMvc.perform(post("/api/transfers/wallet-to-bank")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType").value("TRANSFER"))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.description").value("Wallet to bank transfer"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    @DisplayName("POST /wallet-to-bank: should return 422 when wallet has insufficient balance")
    void walletToBank_shouldFail_whenInsufficientBalance() throws Exception {
        TransferDto.Request request = new TransferDto.Request(
                wallet.getId(), bankAccount.getId(), new BigDecimal("99999.00"));

        mockMvc.perform(post("/api/transfers/wallet-to-bank")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /wallet-to-bank: should return 404 when wallet not found")
    void walletToBank_shouldReturn404_whenWalletNotFound() throws Exception {
        TransferDto.Request request = new TransferDto.Request(
                999999L, bankAccount.getId(), new BigDecimal("100.00"));

        mockMvc.perform(post("/api/transfers/wallet-to-bank")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Wallet not found with id: 999999"));
    }

    @Test
    @DisplayName("POST /wallet-to-bank: should return 404 when bank account not found")
    void walletToBank_shouldReturn404_whenBankAccountNotFound() throws Exception {
        TransferDto.Request request = new TransferDto.Request(
                wallet.getId(), 999999L, new BigDecimal("100.00"));

        mockMvc.perform(post("/api/transfers/wallet-to-bank")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Bank account not found with id: 999999"));
    }

    @Test
    @DisplayName("POST /wallet-to-bank: should return 400 when request is invalid")
    void walletToBank_shouldReturn400_whenRequestIsInvalid() throws Exception
    {
        // amount is negative — violates @Positive
        String invalidJson = """
                {
                  "sourceId": %d,
                  "destinationId": %d,
                  "amount": -500
                }
                """.formatted(wallet.getId(), bankAccount.getId());

        mockMvc.perform(post("/api/transfers/wallet-to-bank")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}