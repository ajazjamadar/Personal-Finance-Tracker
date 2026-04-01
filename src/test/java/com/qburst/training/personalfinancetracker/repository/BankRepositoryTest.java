package com.qburst.training.personalfinancetracker.repository;

import com.qburst.training.personalfinancetracker.entity.Bank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BankRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BankRepository bankRepository;

    private Bank bank;

    @BeforeEach
    void setUp() {
        bank = Bank.builder()
                .bankName("Test Bank")
                .bankCode("TESTBANK")
                .build();
        entityManager.persistAndFlush(bank);
    }

    @Test
    @DisplayName("findByBankNameIgnoreCase: should find bank with exact name (case-insensitive)")
    void findByBankNameIgnoreCase_shouldReturnBank() {
        Optional<Bank> found = bankRepository.findByBankNameIgnoreCase("test bank");
        assertThat(found).isPresent();
        assertThat(found.get().getBankCode()).isEqualTo("TESTBANK");
    }

    @Test
    @DisplayName("findByBankNameIgnoreCase: should return empty for unknown bank name")
    void findByBankNameIgnoreCase_shouldReturnEmpty_whenNotFound() {
        Optional<Bank> found = bankRepository.findByBankNameIgnoreCase("Unknown Bank");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsByBankCode: should return true when bank code exists")
    void existsByBankCode_shouldReturnTrue() {
        boolean exists = bankRepository.existsByBankCode("TESTBANK");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByBankCode: should return false for unknown bank code")
    void existsByBankCode_shouldReturnFalse_whenNotFound() {
        boolean exists = bankRepository.existsByBankCode("XYZ");
        assertThat(exists).isFalse();
    }
}