package com.qburst.training.personalfinancetracker.repository;

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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class WalletRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WalletRepository walletRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("eve")
                .email("eve@email.com")
                .passwordHash("password")
                .fullName("Eve Smith")
                .build();
        entityManager.persistAndFlush(user);
    }

    @Test
    @DisplayName("findByUserId: should return wallets belonging to the user")
    void findByUserId_shouldReturnUserWallets() {
        Wallet w1 = Wallet.builder().user(user).walletName("Cash").balance(new BigDecimal("100")).build();
        Wallet w2 = Wallet.builder().user(user).walletName("UPI").balance(new BigDecimal("200")).build();
        entityManager.persistAndFlush(w1);
        entityManager.persistAndFlush(w2);

        List<Wallet> wallets = walletRepository.findByUserId(user.getId());

        assertThat(wallets).hasSize(2);
        assertThat(wallets).extracting(Wallet::getWalletName)
                .containsExactlyInAnyOrder("Cash", "UPI");
    }

    @Test
    @DisplayName("findByUserId: should return empty list for user with no wallets")
    void findByUserId_shouldReturnEmpty_whenNoWallets() {
        User noWalletUser = User.builder()
                .username("frank")
                .email("frank@example.com")
                .passwordHash("hash")
                .fullName("Frank Black")
                .build();
        entityManager.persistAndFlush(noWalletUser);

        List<Wallet> wallets = walletRepository.findByUserId(noWalletUser.getId());
        assertThat(wallets).isEmpty();
    }
}