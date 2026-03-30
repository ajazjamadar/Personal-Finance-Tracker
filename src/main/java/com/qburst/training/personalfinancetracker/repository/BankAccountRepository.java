package com.qburst.training.personalfinancetracker.repository;

import com.qburst.training.personalfinancetracker.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findByUserId(Long userId);
    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT b FROM BankAccount b WHERE b.bank.bankName = :bankName")
    List<BankAccount> findByBankName(@Param("bankName") String bankName);

    @Query("SELECT b FROM BankAccount b WHERE b.user.fullName LIKE %:fullName%")
    List<BankAccount> findByUserFullName(@Param("fullName") String fullName);
}