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

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BankAccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    // Entities shared across tests (set up fresh before each test)
    private User user;
    private Bank bank;
    private BankAccount account;

    @BeforeEach
    void setUp() {
        // Create and persist a User (FK parent for BankAccount)
        user = User.builder()
                .username("alice")
                .email("alice@example.com")
                .passwordHash("hash")
                .fullName("Alice Smith")
                .build();
        entityManager.persistAndFlush(user);

        // Create and persist a Bank (FK parent for BankAccount)
        bank = Bank.builder()
                .bankName("Test Bank")
                .bankCode("TSTB")
                .build();
        entityManager.persistAndFlush(bank);

        // Create BankAccount that links to both User and Bank
        account = BankAccount.builder()
                .user(user)
                .bank(bank)
                .accountNumber("ACC-001")
                .balance(new BigDecimal("5000.00"))
                .build();
        entityManager.persistAndFlush(account);
    }

    @Test
    @DisplayName("FK check: BankAccount should correctly store user and bank references")
    void bankAccount_shouldHaveCorrectForeignKeys() {
        BankAccount found = entityManager.find(BankAccount.class, account.getId());

        // If FK columns were missing, these would be null
        assertThat(found.getUser().getId()).isEqualTo(user.getId());
        assertThat(found.getBank().getId()).isEqualTo(bank.getId());
        assertThat(found.getUser().getFullName()).isEqualTo("Alice Smith");
        assertThat(found.getBank().getBankName()).isEqualTo("Test Bank");
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
        // Create a second user with no accounts
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
        // Add a second account for the same user
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

    @Test
    @DisplayName("findByBankName: should return accounts that belong to the given bank")
    void findByBankName_shouldReturnCorrectAccounts() {
        List<BankAccount> accounts = bankAccountRepository.findByBankName("Test Bank");

        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getAccountNumber()).isEqualTo("ACC-001");
    }

    @Test
    @DisplayName("findByBankName: should return empty list when bank name doesn't match")
    void findByBankName_shouldReturnEmpty_whenBankNotFound() {
        List<BankAccount> accounts = bankAccountRepository.findByBankName("Ghost Bank");
        assertThat(accounts).isEmpty();
    }


    @Test
    @DisplayName("findByUserFullName: should match partial full name (LIKE query)")
     void findByUserFullName_shouldReturnAccounts_onPartialMatch() {
        // The query uses LIKE %:fullName% so a partial name should work
        List<BankAccount> accounts = bankAccountRepository.findByUserFullName("Alice");

        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getUser().getFullName()).isEqualTo("Alice Smith");
    }

    @Test
    @DisplayName("findByUserFullName: should return empty when name doesn't match")
    void findByUserFullName_shouldReturnEmpty_whenNoMatch() {
        List<BankAccount> accounts = bankAccountRepository.findByUserFullName("Nobody");
        assertThat(accounts).isEmpty();
    }


    @Test
    @DisplayName("UNIQUE constraint: duplicate account number should throw on flush")
    void save_shouldFail_whenAccountNumberIsDuplicate() {
        BankAccount duplicate = BankAccount.builder()
                .user(user)
                .bank(bank)
                .accountNumber("ACC-001")   // same number — violates UNIQUE
                .balance(BigDecimal.ZERO)
                .build();

        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> entityManager.persistAndFlush(duplicate)
        );
    }

    @Test
    @DisplayName("@PrePersist: balance should default to ZERO if null is provided")
    void balance_shouldDefaultToZero_whenNullOnCreate() {
        BankAccount noBalance = BankAccount.builder()
                .user(user)
                .bank(bank)
                .accountNumber("ACC-ZERO")
                .balance(null)              // @PrePersist should set this to ZERO
                .build();
        entityManager.persistAndFlush(noBalance);

        BankAccount found = entityManager.find(BankAccount.class, noBalance.getId());
        assertThat(found.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}