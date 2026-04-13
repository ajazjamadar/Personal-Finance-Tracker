package com.qburst.training.personalfinancetracker.repository;

import com.qburst.training.personalfinancetracker.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    interface BankBalanceRow {
        String getBankName();
        String getAccountNumber();
        java.math.BigDecimal getBalance();
    }

    List<BankAccount> findByUserId(Long userId);
    List<BankAccount> findAllByOrderByCreatedAtDesc();
    List<BankAccount> findTop8ByBalanceLessThanEqualOrderByBalanceAsc(BigDecimal threshold);
    boolean existsByAccountNumber(String accountNumber);

    @Query("""
            select b.bank.bankName as bankName,
                   b.accountNumber as accountNumber,
                   b.balance as balance
            from BankAccount b
            where b.user.id = :userId
            order by b.bank.bankName asc, b.accountNumber asc
            """)
    List<BankBalanceRow> findBalanceRowsByUserId(@Param("userId") Long userId);
}
