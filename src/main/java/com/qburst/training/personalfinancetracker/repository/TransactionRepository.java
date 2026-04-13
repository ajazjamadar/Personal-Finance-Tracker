package com.qburst.training.personalfinancetracker.repository;

import com.qburst.training.personalfinancetracker.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    interface MonthlyExpenseRow {
        Integer getYearValue();
        Integer getMonthValue();
        BigDecimal getTotalExpense();
    }

    interface CategoryExpenseRow {
        String getCategory();
        BigDecimal getTotalExpense();
    }

    interface IncomeExpenseSummaryRow {
        BigDecimal getTotalIncome();
        BigDecimal getTotalExpense();
    }

    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Transaction> findTop50ByOrderByCreatedAtDesc();
    List<Transaction> findTop8ByOrderByCreatedAtDesc();
    List<Transaction> findTop8ByTransactionTypeOrderByCreatedAtDesc(Transaction.TransactionType transactionType);
    List<Transaction> findTop8ByTransactionTypeAndStatusOrderByCreatedAtDesc(Transaction.TransactionType transactionType,
                                                                             Transaction.TransactionStatus status);
    List<Transaction> findTop8ByAmountGreaterThanEqualOrderByCreatedAtDesc(BigDecimal amount);
    long countByCreatedAtBetween(LocalDateTime startInclusive, LocalDateTime endExclusive);
    long countByTransactionTypeAndCreatedAtBetween(Transaction.TransactionType transactionType,
                                                   LocalDateTime startInclusive,
                                                   LocalDateTime endExclusive);
    long countByStatus(Transaction.TransactionStatus status);
    long countByTransactionTypeAndStatusIn(Transaction.TransactionType transactionType,
                                           Collection<Transaction.TransactionStatus> statuses);

    @Query("""
            select coalesce(sum(t.amount), 0)
            from Transaction t
            where t.transactionType = :transactionType
              and t.status in :statuses
            """)
    BigDecimal sumAmountByTransactionTypeAndStatuses(@Param("transactionType") Transaction.TransactionType transactionType,
                                                     @Param("statuses") Collection<Transaction.TransactionStatus> statuses);

    @Query("""
            select function('year', t.createdAt) as yearValue,
                   function('month', t.createdAt) as monthValue,
                   coalesce(sum(t.amount), 0) as totalExpense
            from Transaction t
            where t.user.id = :userId
              and t.transactionType in :expenseTypes
              and t.status = :status
            group by function('year', t.createdAt), function('month', t.createdAt)
            order by function('year', t.createdAt) desc, function('month', t.createdAt) desc
            """)
    List<MonthlyExpenseRow> findMonthlyExpenseRowsByUserId(@Param("userId") Long userId,
                                                           @Param("expenseTypes") Collection<Transaction.TransactionType> expenseTypes,
                                                           @Param("status") Transaction.TransactionStatus status);

    @Query("""
            select case
                       when t.categoryName is null or trim(t.categoryName) = '' then 'Uncategorized'
                       else t.categoryName
                   end as category,
                   coalesce(sum(t.amount), 0) as totalExpense
            from Transaction t
            where t.user.id = :userId
              and t.transactionType in :expenseTypes
              and t.status = :status
            group by case
                         when t.categoryName is null or trim(t.categoryName) = '' then 'Uncategorized'
                         else t.categoryName
                     end
            order by coalesce(sum(t.amount), 0) desc
            """)
    List<CategoryExpenseRow> findCategoryExpenseRowsByUserId(@Param("userId") Long userId,
                                                             @Param("expenseTypes") Collection<Transaction.TransactionType> expenseTypes,
                                                             @Param("status") Transaction.TransactionStatus status);

    @Query("""
            select coalesce(sum(case when t.transactionType = :incomeType then t.amount else 0 end), 0) as totalIncome,
                   coalesce(sum(case when t.transactionType in :expenseTypes then t.amount else 0 end), 0) as totalExpense
            from Transaction t
            where t.user.id = :userId
              and t.status = :status
            """)
    IncomeExpenseSummaryRow findIncomeExpenseSummaryByUserId(@Param("userId") Long userId,
                                                             @Param("incomeType") Transaction.TransactionType incomeType,
                                                             @Param("expenseTypes") Collection<Transaction.TransactionType> expenseTypes,
                                                             @Param("status") Transaction.TransactionStatus status);
}
