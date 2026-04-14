package com.qburst.training.personalfinancetracker.repository;

import com.qburst.training.personalfinancetracker.dto.TransactionDto;
import com.qburst.training.personalfinancetracker.entity.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.LocalTime;

public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    public static Specification<Transaction> historyForUser(Long userId, TransactionDto.HistoryFilter filter) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();
            predicates = cb.and(predicates, cb.equal(root.get("user").get("id"), userId));

            if (filter == null) {
                return predicates;
            }

            if (filter.fromDate() != null) {
                LocalDateTime from = filter.fromDate().atStartOfDay();
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }

            if (filter.toDate() != null) {
                LocalDateTime to = filter.toDate().atTime(LocalTime.MAX);
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }

            if (filter.transactionType() != null) {
                if (filter.transactionType() == TransactionDto.HistoryTransactionType.CREDIT) {
                    predicates = cb.and(predicates,
                            cb.equal(root.get("transactionType"), Transaction.TransactionType.INCOME));
                } else {
                    predicates = cb.and(predicates, root.get("transactionType").in(
                            Transaction.TransactionType.EXPENSE,
                            Transaction.TransactionType.TRANSFER));
                }
            }

            if (filter.minAmount() != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("amount"), filter.minAmount()));
            }

            if (filter.maxAmount() != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("amount"), filter.maxAmount()));
            }

            if (filter.status() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), filter.status()));
            }

            if (filter.category() != null) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("categoryName")), "%" + filter.category().toLowerCase() + "%"));
            }

            if (filter.paymentMethod() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("paymentMethod"), filter.paymentMethod()));
            }

            if (filter.accountId() != null) {
                predicates = cb.and(predicates, cb.or(
                        cb.equal(root.get("sourceBankAccount").get("id"), filter.accountId()),
                        cb.equal(root.get("destBankAccount").get("id"), filter.accountId())));
            }

            if (filter.receiver() != null) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("receiverName")), "%" + filter.receiver().toLowerCase() + "%"));
            }

            return predicates;
        };
    }
}
