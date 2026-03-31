package com.qburst.training.personalfinancetracker.repository;

import com.qburst.training.personalfinancetracker.entity.Transaction;
import com.qburst.training.personalfinancetracker.entity.User;
import com.qburst.training.personalfinancetracker.entity.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    private User user;
    private Wallet wallet;
    @BeforeEach
    void setUp(){
        user = User.builder()
                .username("ajaz")
                .email("ajaz@email.com")
                .passwordHash("password")
                .fullName("Ajaz Jamadar")
                .build();
        entityManager.persistAndFlush(user);

        wallet = Wallet.builder()
                .user(user)
                .walletName("My Wallet")
                .balance(new BigDecimal("2000.00"))
                .build();
        entityManager.persistAndFlush(wallet);
    }

    //Table Creation/ FK varification

    @Test
    @DisplayName("FK Check: transaction should correctly link to user and wallet")
    void transaction_shouldHaveCorrectForeignKey(){
        Transaction tx = Transaction.builder()
                .user(user)
                .sourceWallet(wallet)
                .transactionType(Transaction.TransactionType.EXPENSE)
                .amount(new BigDecimal("1000.00"))
                .build();
        entityManager.persistAndFlush(tx);

        Transaction found = entityManager.find(Transaction.class,tx.getId());

        //verify fk to user table
        assertThat(found.getUser().getId()).isEqualTo(user.getId());
        //verify fk to wallets table
        assertThat(found.getSourceWallet().getId()).isEqualTo(wallet.getId());
    }

    //findByUserId
    @Test
    @DisplayName("findByUserId: should return all transactions for the given user")
    void findByUserId_shouldReturnTransactions() {
        // Persist two transactions for our test user
        entityManager.persistAndFlush(buildTx(Transaction.TransactionType.INCOME, "200.00", "Salary"));
        entityManager.persistAndFlush(buildTx(Transaction.TransactionType.EXPENSE, "50.00", "Coffee"));

        List<Transaction> txs = transactionRepository.findByUserId(user.getId());

        assertThat(txs).hasSize(2);
    }

    @Test
    @DisplayName("findByUserId: should return empty list for user with no transactions")
    void findUserId_shouldReturnEmpty_whenNoTransactions(){
        User newUser = User.builder()
                .username("dev")
                .email("dev@email.com")
                .passwordHash("hash")
                .fullName("dev kumara")
                .build();
        entityManager.persistAndFlush(newUser);

        List<Transaction> txs = transactionRepository.findByUserId(newUser.getId());
        assertThat(txs).isEmpty();
    }

    //findByUserIdOrderByCreatedDesc
    @Test
    @DisplayName("findByUserIdOrderByCreatedAtDesc: most recent transaction should come first")
    void findByUserIdOrderByCreatedAt_shouldReturnOrderByNewestFirst() throws InterruptedException{
        Transaction first = buildTx(Transaction.TransactionType.INCOME, "100.00", "First");
        Transaction second = buildTx(Transaction.TransactionType.EXPENSE,"30.00", "Second");

        LocalDateTime now = LocalDateTime.now();
        first.setCreatedAt(now.minusMinutes(1));
        second.setCreatedAt(now);

        entityManager.persistAndFlush(first);
        entityManager.persistAndFlush(second);

        List<Transaction> ordered = transactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        assertThat(ordered).hasSize(2);

        assertThat(ordered.get(0).getDescription()).isEqualTo("Second");
        assertThat(ordered.get(1).getDescription()).isEqualTo("First");
    }

    //TransactionType enum persisted a String
    @Test
    @DisplayName("TransactionType: all enum values should be persistable")
    void transactionType_allValues_shouldPersistCorrectly(){
        for (Transaction.TransactionType type : Transaction.TransactionType.values()){
            Transaction tx = Transaction.builder()
                    .user(user)
                    .transactionType(type)
                    .amount(new BigDecimal("10.00"))
                    .build();
            entityManager.persistAndFlush(tx);

            Transaction found = entityManager.find(Transaction.class, tx.getId());
            assertThat(found.getTransactionType()).isEqualTo(type);
        }
    }

    private Transaction buildTx(Transaction.TransactionType type, String amount, String description) {
        return Transaction.builder()
                .user(user)
                .sourceWallet(wallet)
                .transactionType(type)
                .amount(new BigDecimal(amount))
                .description(description)
                .build();
    }
}
