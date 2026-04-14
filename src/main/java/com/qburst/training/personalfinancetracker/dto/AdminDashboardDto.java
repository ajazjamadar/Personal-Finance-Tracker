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
            long totalBankAccounts,
            long totalWallets,
            long totalAccounts,
            long totalTransactions,
            long totalWalletTransactions,
            BigDecimal totalTransferVolume
    ) {}

    public record Snapshot(
            ActivityWindow today,
            ActivityWindow thisMonth
    ) {}

    public record ActivityWindow(
            long transactions,
            long walletTransactions,
            long transfers,
            long newUsers
    ) {}

    public record SystemHealth(
            long failedTransactionsCount,
            long pendingTransfersCount,
            long failedTransfersCount,
            long lowWalletBalanceCount,
            String apiStatus,
            LocalDateTime checkedAt
    ) {}

    public record RecentActivity(
            List<TransactionItem> latestTransactions,
            List<WalletTransactionItem> latestWalletTransactions,
            List<UserRegistrationItem> recentUserRegistrations,
            List<TransferItem> recentTransfers
    ) {}

    public record Alerts(
            List<TransferItem> failedTransfers,
            List<LowBalanceIssue> lowBalanceIssues,
            List<LowWalletBalanceIssue> lowWalletBalanceIssues,
            List<TransactionItem> suspiciousTransactions,
            List<WalletTransactionItem> suspiciousWalletTransactions
    ) {}

    public record Thresholds(
            BigDecimal lowBalanceThreshold,
            BigDecimal lowWalletBalanceThreshold,
            BigDecimal suspiciousTransactionThreshold,
            BigDecimal suspiciousWalletTransactionThreshold
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

    public record WalletTransactionItem(
            Long id,
            Long walletId,
            Long userId,
            String userName,
            String walletName,
            String type,
            BigDecimal amount,
            String category,
            String description,
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

    public record LowWalletBalanceIssue(
            Long walletId,
            Long userId,
            String userName,
            String walletName,
            BigDecimal balance,
            String currency,
            LocalDateTime createdAt
    ) {}
}
