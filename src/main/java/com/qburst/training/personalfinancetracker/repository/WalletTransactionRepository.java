package com.qburst.training.personalfinancetracker.repository;

import com.qburst.training.personalfinancetracker.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    Page<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);
    List<WalletTransaction> findTop8ByOrderByCreatedAtDesc();
    List<WalletTransaction> findTop8ByAmountGreaterThanEqualOrderByCreatedAtDesc(BigDecimal amount);
    long countByCreatedAtBetween(LocalDateTime startInclusive, LocalDateTime endExclusive);
}
