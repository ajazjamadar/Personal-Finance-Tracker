package com.qburst.training.personalfinancetracker.service.admin;

import com.qburst.training.personalfinancetracker.dto.AdminDashboardDto;
import com.qburst.training.personalfinancetracker.entity.Bank;
import com.qburst.training.personalfinancetracker.entity.BankAccount;
import com.qburst.training.personalfinancetracker.entity.Transaction;
import com.qburst.training.personalfinancetracker.entity.User;
import com.qburst.training.personalfinancetracker.entity.UserRole;
import com.qburst.training.personalfinancetracker.repository.BankAccountRepository;
import com.qburst.training.personalfinancetracker.repository.BankRepository;
import com.qburst.training.personalfinancetracker.repository.TransactionRepository;
import com.qburst.training.personalfinancetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminServiceImplTest {

    @Autowired private AdminService adminService;
    @Autowired private UserRepository userRepository;
    @Autowired private BankRepository bankRepository;
    @Autowired private BankAccountRepository bankAccountRepository;
    @Autowired private TransactionRepository transactionRepository;

    private User primaryUser;
    private User recentAdmin;
    private BankAccount lowBalanceAccount;

    @BeforeEach
    void setUp() {
        Bank bank = bankRepository.findByBankNameIgnoreCase("HDFC Bank")
                .orElseGet(() -> bankRepository.save(Bank.builder()
                        .bankName("HDFC Bank")
                        .bankCode("HDFC")
                        .build()));

        primaryUser = userRepository.save(User.builder()
                .username("finance-user")
                .email("finance-user@example.com")
                .passwordHash("hashed")
                .fullName("Finance User")
                .role(UserRole.USER)
                .build());

        recentAdmin = userRepository.save(User.builder()
                .username("ops-admin")
                .email("ops-admin@example.com")
                .passwordHash("hashed")
                .fullName("Operations Admin")
                .role(UserRole.ADMIN)
                .build());

        lowBalanceAccount = bankAccountRepository.save(BankAccount.builder()
                .user(primaryUser)
                .bank(bank)
                .accountNumber("ACC-LOW-001")
                .balance(new BigDecimal("500.00"))
                .build());

        BankAccount healthyAccount = bankAccountRepository.save(BankAccount.builder()
                .user(recentAdmin)
                .bank(bank)
                .accountNumber("ACC-HEALTHY-001")
                .balance(new BigDecimal("15000.00"))
                .build());

        LocalDateTime now = LocalDateTime.now();

        transactionRepository.save(Transaction.builder()
                .user(primaryUser)
                .sourceBankAccount(lowBalanceAccount)
                .transactionType(Transaction.TransactionType.TRANSFER)
                .transferType(Transaction.TransferType.ACCOUNT)
                .destinationValue("88990011")
                .amount(new BigDecimal("1200.00"))
                .receiverName("Vendor One")
                .status(Transaction.TransactionStatus.SENT)
                .categoryName("Transfer")
                .createdAt(now.minusHours(1))
                .build());

        transactionRepository.save(Transaction.builder()
                .user(primaryUser)
                .sourceBankAccount(lowBalanceAccount)
                .transactionType(Transaction.TransactionType.TRANSFER)
                .transferType(Transaction.TransferType.UPI)
                .destinationValue("vendor@upi")
                .amount(new BigDecimal("300.00"))
                .receiverName("Vendor Two")
                .status(Transaction.TransactionStatus.PENDING)
                .categoryName("Transfer")
                .createdAt(now.minusMinutes(20))
                .build());

        transactionRepository.save(Transaction.builder()
                .user(primaryUser)
                .sourceBankAccount(lowBalanceAccount)
                .transactionType(Transaction.TransactionType.TRANSFER)
                .transferType(Transaction.TransferType.MOBILE)
                .destinationValue("9999999999")
                .amount(new BigDecimal("900.00"))
                .receiverName("Vendor Fail")
                .status(Transaction.TransactionStatus.FAILED)
                .categoryName("Transfer")
                .createdAt(now.minusMinutes(10))
                .build());

        transactionRepository.save(Transaction.builder()
                .user(recentAdmin)
                .sourceBankAccount(healthyAccount)
                .transactionType(Transaction.TransactionType.EXPENSE)
                .amount(new BigDecimal("60000.00"))
                .receiverName("Large Merchant")
                .description("High-value purchase")
                .status(Transaction.TransactionStatus.SUCCESS)
                .categoryName("Operations")
                .createdAt(now.minusMinutes(5))
                .build());
    }

    @Test
    @DisplayName("Admin dashboard aggregates metrics, health, activity, and alerts")
    void getDashboard_shouldReturnFinanceSummary() {
        AdminDashboardDto.Response dashboard = adminService.getDashboard();

        assertThat(dashboard.keyMetrics().totalUsers()).isEqualTo(2);
        assertThat(dashboard.keyMetrics().totalAccounts()).isEqualTo(2);
        assertThat(dashboard.keyMetrics().totalTransactions()).isEqualTo(4);
        assertThat(dashboard.keyMetrics().totalTransferVolume()).isEqualByComparingTo("1500.00");

        assertThat(dashboard.snapshot().today().transactions()).isEqualTo(4);
        assertThat(dashboard.snapshot().today().transfers()).isEqualTo(3);
        assertThat(dashboard.snapshot().today().newUsers()).isEqualTo(2);
        assertThat(dashboard.snapshot().thisMonth().transactions()).isEqualTo(4);

        assertThat(dashboard.systemHealth().failedTransactionsCount()).isEqualTo(1);
        assertThat(dashboard.systemHealth().pendingTransfersCount()).isEqualTo(1);
        assertThat(dashboard.systemHealth().failedTransfersCount()).isEqualTo(1);
        assertThat(dashboard.systemHealth().apiStatus()).isEqualTo("UP");

        assertThat(dashboard.recentActivity().latestTransactions()).hasSize(4);
        assertThat(dashboard.recentActivity().recentUserRegistrations()).hasSize(2);
        assertThat(dashboard.recentActivity().recentTransfers()).hasSize(3);

        assertThat(dashboard.alerts().failedTransfers()).hasSize(1);
        assertThat(dashboard.alerts().lowBalanceIssues()).hasSize(1);
        assertThat(dashboard.alerts().lowBalanceIssues().getFirst().accountId()).isEqualTo(lowBalanceAccount.getId());
        assertThat(dashboard.alerts().suspiciousTransactions()).hasSize(1);
        assertThat(dashboard.alerts().suspiciousTransactions().getFirst().amount()).isEqualByComparingTo("60000.00");
    }
}
