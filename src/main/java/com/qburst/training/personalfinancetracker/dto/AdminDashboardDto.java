package com.qburst.training.personalfinancetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AdminDashboardDto {

    public record Response(
            KeyMetrics keyMetrics,
            Snapshot snapshot,
            SystemHealth systemHealth,
            RecentActivity recentActivity,
            Alerts alerts,
            Thresholds thresholds,
            LocalDateTime generatedAt
    ) {}

    public record KeyMetrics(
            long totalUsers,
            long totalAccounts,
            long totalTransactions,
            BigDecimal totalTransferVolume
    ) {}

    public record Snapshot(
            ActivityWindow today,
            ActivityWindow thisMonth
    ) {}

    public record ActivityWindow(
            long transactions,
            long transfers,
            long newUsers
    ) {}

    public record SystemHealth(
            long failedTransactionsCount,
            long pendingTransfersCount,
            long failedTransfersCount,
            String apiStatus,
            LocalDateTime checkedAt
    ) {}

    public record RecentActivity(
            List<TransactionItem> latestTransactions,
            List<UserRegistrationItem> recentUserRegistrations,
            List<TransferItem> recentTransfers
    ) {}

    public record Alerts(
            List<TransferItem> failedTransfers,
            List<LowBalanceIssue> lowBalanceIssues,
            List<TransactionItem> suspiciousTransactions
    ) {}

    public record Thresholds(
            BigDecimal lowBalanceThreshold,
            BigDecimal suspiciousTransactionThreshold
    ) {}

    public record TransactionItem(
            Long id,
            Long userId,
            String userName,
            String transactionType,
            String status,
            BigDecimal amount,
            String receiverName,
            String description,
            LocalDateTime createdAt
    ) {}

    public record TransferItem(
            Long id,
            Long userId,
            String userName,
            String transferType,
            String status,
            BigDecimal amount,
            Long sourceAccountId,
            String destinationValue,
            String receiverName,
            LocalDateTime createdAt
    ) {}

    public record UserRegistrationItem(
            Long id,
            String fullName,
            String email,
            String role,
            LocalDateTime createdAt
    ) {}

    public record LowBalanceIssue(
            Long accountId,
            Long userId,
            String userName,
            String bankName,
            String accountNumber,
            BigDecimal balance,
            LocalDateTime createdAt
    ) {}
}
