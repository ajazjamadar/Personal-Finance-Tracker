package com.qburst.training.personalfinancetracker.repository;

import com.qburst.training.personalfinancetracker.entity.Bank;
import com.qburst.training.personalfinancetracker.entity.BankAccount;
import com.qburst.training.personalfinancetracker.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BankAccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    private User user;
    private Bank bank;
    private BankAccount account;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("alice")
                .email("alice@example.com")
                .passwordHash("hash")
                .fullName("Alice Smith")
                .build();
        entityManager.persistAndFlush(user);

        bank = Bank.builder()
                .bankName("Test Bank")
                .bankCode("TSTB")
                .build();
        entityManager.persistAndFlush(bank);

        account = BankAccount.builder()
                .user(user)
                .bank(bank)
                .accountNumber("ACC-001")
                .balance(new BigDecimal("5000.00"))
                .build();
        entityManager.persistAndFlush(account);
    }

    @Test
    @DisplayName("findByUserId: should return accounts belonging to the given user")
    void findByUserId_shouldReturnAccountsForUser() {
        List<BankAccount> accounts = bankAccountRepository.findByUserId(user.getId());

        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getAccountNumber()).isEqualTo("ACC-001");
    }

    @Test
    @DisplayName("findByUserId: should return empty list for user with no accounts")
    void findByUserId_shouldReturnEmpty_whenUserHasNoAccounts() {
        User newUser = User.builder()
                .username("bob")
                .email("bob@example.com")
                .passwordHash("hash")
                .fullName("Bob Jones")
                .build();
        entityManager.persistAndFlush(newUser);

        List<BankAccount> accounts = bankAccountRepository.findByUserId(newUser.getId());
        assertThat(accounts).isEmpty();
    }

    @Test
    @DisplayName("findByUserId: should return multiple accounts if user has more than one")
    void findByUserId_shouldReturnAll_whenUserHasMultipleAccounts() {
        BankAccount second = BankAccount.builder()
                .user(user)
                .bank(bank)
                .accountNumber("ACC-002")
                .balance(new BigDecimal("1000.00"))
                .build();
        entityManager.persistAndFlush(second);

        List<BankAccount> accounts = bankAccountRepository.findByUserId(user.getId());
        assertThat(accounts).hasSize(2);
    }

    @Test
    @DisplayName("existsByAccountNumber: should return true when account number exists")
    void existsByAccountNumber_shouldReturnTrue() {
        boolean exists = bankAccountRepository.existsByAccountNumber("ACC-001");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByAccountNumber: should return false for unknown account number")
    void existsByAccountNumber_shouldReturnFalse_whenNotFound() {
        boolean exists = bankAccountRepository.existsByAccountNumber("DOES-NOT-EXIST");
        assertThat(exists).isFalse();
    }

}
