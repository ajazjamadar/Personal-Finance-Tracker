package com.qburst.training.personalfinancetracker.repository;

import com.qburst.training.personalfinancetracker.entity.BankAccount;
import com.qburst.training.personalfinancetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends CrudRepository<BankAccount, Long> {
    List<BankAccount> findByUserId(Long userId);
    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT b FROM BankAccount b WHERE b.bank.bankName = :bankName")
    List<BankAccount> findByBankName(@Param("bankName") String bankName);

    @Query("SELECT b FROM BankAccount b WHERE b.user.fullName LIKE %:fullName%")
    List<BankAccount> findByUserFullName(@Param("fullName") String fullName);

}