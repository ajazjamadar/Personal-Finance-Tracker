package com.qburst.training.personalfinancetracker.repository;

import com.qburst.training.personalfinancetracker.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    List<Wallet> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Wallet> findTop8ByBalanceLessThanEqualOrderByBalanceAsc(BigDecimal balance);
    Optional<Wallet> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
    long countByBalanceLessThanEqual(BigDecimal balance);
}
