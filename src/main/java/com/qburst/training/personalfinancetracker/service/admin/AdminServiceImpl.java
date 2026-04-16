package com.qburst.training.personalfinancetracker.service.admin;

import com.qburst.training.personalfinancetracker.dto.AdminDashboardDto;
import com.qburst.training.personalfinancetracker.dto.BankAccountDto;
import com.qburst.training.personalfinancetracker.dto.TransactionDto;
import com.qburst.training.personalfinancetracker.dto.UserDto;
import com.qburst.training.personalfinancetracker.entity.Bank;
import com.qburst.training.personalfinancetracker.entity.BankAccount;
import com.qburst.training.personalfinancetracker.entity.Transaction;
import com.qburst.training.personalfinancetracker.entity.User;
import com.qburst.training.personalfinancetracker.entity.UserRole;
import com.qburst.training.personalfinancetracker.entity.Wallet;
import com.qburst.training.personalfinancetracker.entity.WalletTransaction;
import com.qburst.training.personalfinancetracker.exception.DuplicateResourceException;
import com.qburst.training.personalfinancetracker.exception.ResourceNotFoundException;
import com.qburst.training.personalfinancetracker.repository.BankAccountRepository;
import com.qburst.training.personalfinancetracker.repository.BankRepository;
import com.qburst.training.personalfinancetracker.repository.TransactionRepository;
import com.qburst.training.personalfinancetracker.repository.UserRepository;
import com.qburst.training.personalfinancetracker.repository.WalletRepository;
import com.qburst.training.personalfinancetracker.repository.WalletTransactionRepository;
import com.qburst.training.personalfinancetracker.service.transaction.TransactionService;
import com.qburst.training.personalfinancetracker.util.ExportDocumentBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private static final BigDecimal LOW_BALANCE_THRESHOLD = new BigDecimal("1000.00");
    private static final BigDecimal LOW_WALLET_BALANCE_THRESHOLD = new BigDecimal("500.00");
    private static final BigDecimal SUSPICIOUS_TRANSACTION_THRESHOLD = new BigDecimal("50000.00");
    private static final BigDecimal SUSPICIOUS_WALLET_TRANSACTION_THRESHOLD = new BigDecimal("25000.00");
    private static final Set<Transaction.TransactionStatus> ACTIVE_TRANSFER_STATUSES = EnumSet.of(
            Transaction.TransactionStatus.INITIATED,
            Transaction.TransactionStatus.PENDING,
            Transaction.TransactionStatus.SENT,
            Transaction.TransactionStatus.SUCCESS
    );
    private static final Set<Transaction.TransactionStatus> PENDING_TRANSFER_STATUSES = EnumSet.of(
            Transaction.TransactionStatus.INITIATED,
            Transaction.TransactionStatus.PENDING
    );

    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BankRepository bankRepository;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final TransactionService transactionService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public AdminDashboardDto.Response getDashboard() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfNextMonth = today.withDayOfMonth(1).plusMonths(1).atStartOfDay();
        long totalBankAccounts = bankAccountRepository.count();
        long totalWallets = walletRepository.count();

        AdminDashboardDto.KeyMetrics keyMetrics = new AdminDashboardDto.KeyMetrics(
                userRepository.count(),
                totalBankAccounts,
                totalWallets,
                totalBankAccounts + totalWallets,
                transactionRepository.count(),
                walletTransactionRepository.count(),
                transactionRepository.sumAmountByTransactionTypeAndStatuses(
                        Transaction.TransactionType.TRANSFER,
                        ACTIVE_TRANSFER_STATUSES
                )
        );

        AdminDashboardDto.Snapshot snapshot = new AdminDashboardDto.Snapshot(
                new AdminDashboardDto.ActivityWindow(
                        transactionRepository.countByCreatedAtBetween(startOfToday, startOfTomorrow),
                        walletTransactionRepository.countByCreatedAtBetween(startOfToday, startOfTomorrow),
                        transactionRepository.countByTransactionTypeAndCreatedAtBetween(
                                Transaction.TransactionType.TRANSFER, startOfToday, startOfTomorrow),
                        userRepository.countByCreatedAtBetween(startOfToday, startOfTomorrow)
                ),
                new AdminDashboardDto.ActivityWindow(
                        transactionRepository.countByCreatedAtBetween(startOfMonth, startOfNextMonth),
                        walletTransactionRepository.countByCreatedAtBetween(startOfMonth, startOfNextMonth),
                        transactionRepository.countByTransactionTypeAndCreatedAtBetween(
                                Transaction.TransactionType.TRANSFER, startOfMonth, startOfNextMonth),
                        userRepository.countByCreatedAtBetween(startOfMonth, startOfNextMonth)
                )
        );

        AdminDashboardDto.SystemHealth systemHealth = new AdminDashboardDto.SystemHealth(
                transactionRepository.countByStatus(Transaction.TransactionStatus.FAILED),
                transactionRepository.countByTransactionTypeAndStatusIn(
                        Transaction.TransactionType.TRANSFER, PENDING_TRANSFER_STATUSES),
                transactionRepository.countByTransactionTypeAndStatusIn(
                        Transaction.TransactionType.TRANSFER, Set.of(Transaction.TransactionStatus.FAILED)),
                walletRepository.countByBalanceLessThanEqual(LOW_WALLET_BALANCE_THRESHOLD),
                "UP",
                now
        );

        AdminDashboardDto.RecentActivity recentActivity = new AdminDashboardDto.RecentActivity(
                transactionRepository.findTop8ByOrderByCreatedAtDesc().stream()
                        .map(this::toTransactionItem)
                        .toList(),
                walletTransactionRepository.findTop8ByOrderByCreatedAtDesc().stream()
                        .map(this::toWalletTransactionItem)
                        .toList(),
                userRepository.findTop8ByOrderByCreatedAtDesc().stream()
                        .map(this::toUserRegistrationItem)
                        .toList(),
                transactionRepository.findTop8ByTransactionTypeOrderByCreatedAtDesc(Transaction.TransactionType.TRANSFER)
                        .stream()
                        .map(this::toTransferItem)
                        .toList()
        );

        AdminDashboardDto.Alerts alerts = new AdminDashboardDto.Alerts(
                transactionRepository.findTop8ByTransactionTypeAndStatusOrderByCreatedAtDesc(
                                Transaction.TransactionType.TRANSFER,
                                Transaction.TransactionStatus.FAILED)
                        .stream()
                        .map(this::toTransferItem)
                        .toList(),
                bankAccountRepository.findTop8ByBalanceLessThanEqualOrderByBalanceAsc(LOW_BALANCE_THRESHOLD)
                        .stream()
                        .map(this::toLowBalanceIssue)
                        .toList(),
                walletRepository.findTop8ByBalanceLessThanEqualOrderByBalanceAsc(LOW_WALLET_BALANCE_THRESHOLD)
                        .stream()
                        .map(this::toLowWalletBalanceIssue)
                        .toList(),
                transactionRepository.findTop8ByAmountGreaterThanEqualOrderByCreatedAtDesc(SUSPICIOUS_TRANSACTION_THRESHOLD)
                        .stream()
                        .map(this::toTransactionItem)
                        .toList(),
                walletTransactionRepository
                        .findTop8ByAmountGreaterThanEqualOrderByCreatedAtDesc(SUSPICIOUS_WALLET_TRANSACTION_THRESHOLD)
                        .stream()
                        .map(this::toWalletTransactionItem)
                        .toList()
        );

        return new AdminDashboardDto.Response(
                keyMetrics,
                snapshot,
                systemHealth,
                recentActivity,
                alerts,
                new AdminDashboardDto.Thresholds(
                        LOW_BALANCE_THRESHOLD,
                        LOW_WALLET_BALANCE_THRESHOLD,
                        SUSPICIOUS_TRANSACTION_THRESHOLD,
                        SUSPICIOUS_WALLET_TRANSACTION_THRESHOLD),
                now
        );
    }

    @Override
    public AdminDashboardDto.MonthlyPerformance getMonthlyPerformance(Integer year, Integer month) {
        YearMonth yearMonth = resolveYearMonth(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        LocalDateTime startInclusive = startDate.atStartOfDay();
        LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();

        long transactions = transactionRepository.countByCreatedAtBetween(startInclusive, endExclusive);
        long walletTransactions = walletTransactionRepository.countByCreatedAtBetween(startInclusive, endExclusive);
        long transfers = transactionRepository.countByTransactionTypeAndCreatedAtBetween(
                Transaction.TransactionType.TRANSFER,
                startInclusive,
                endExclusive
        );
        long newUsers = userRepository.countByCreatedAtBetween(startInclusive, endExclusive);
        long failedTransactions = transactionRepository.countByStatusAndCreatedAtBetween(
                Transaction.TransactionStatus.FAILED,
                startInclusive,
                endExclusive
        );
        long pendingTransfers = transactionRepository.countByTransactionTypeAndStatusInAndCreatedAtBetween(
                Transaction.TransactionType.TRANSFER,
                PENDING_TRANSFER_STATUSES,
                startInclusive,
                endExclusive
        );

        BigDecimal income = transactionRepository.sumAmountByTransactionTypeAndStatusesAndCreatedAtBetween(
                Transaction.TransactionType.INCOME,
                Set.of(Transaction.TransactionStatus.SUCCESS),
                startInclusive,
                endExclusive
        );

        BigDecimal expense = transactionRepository.sumAmountByTransactionTypesAndStatusAndCreatedAtBetween(
                List.of(Transaction.TransactionType.EXPENSE, Transaction.TransactionType.TRANSFER),
                Transaction.TransactionStatus.SUCCESS,
                startInclusive,
                endExclusive
        );

        BigDecimal transferVolume = transactionRepository.sumAmountByTransactionTypeAndStatusesAndCreatedAtBetween(
                Transaction.TransactionType.TRANSFER,
                ACTIVE_TRANSFER_STATUSES,
                startInclusive,
                endExclusive
        );

        return new AdminDashboardDto.MonthlyPerformance(
                yearMonth.getYear(),
                yearMonth.getMonthValue(),
                startDate,
                endDate,
                transactions,
                walletTransactions,
                transfers,
                newUsers,
                failedTransactions,
                pendingTransfers,
                income,
                expense,
                transferVolume,
                income.subtract(expense),
                LocalDateTime.now()
        );
    }

    @Override
    public byte[] exportMonthlyPerformanceCsv(Integer year, Integer month) {
        AdminDashboardDto.MonthlyPerformance performance = getMonthlyPerformance(year, month);
        List<List<String>> rows = List.of(
                List.of("Month", performance.year() + "-" + String.format("%02d", performance.month())),
                List.of("Range", performance.startDate() + " to " + performance.endDate()),
                List.of("Transactions", String.valueOf(performance.transactions())),
                List.of("Wallet Transactions", String.valueOf(performance.walletTransactions())),
                List.of("Transfers", String.valueOf(performance.transfers())),
                List.of("New Users", String.valueOf(performance.newUsers())),
                List.of("Failed Transactions", String.valueOf(performance.failedTransactions())),
                List.of("Pending Transfers", String.valueOf(performance.pendingTransfers())),
                List.of("Income", safeAmount(performance.income())),
                List.of("Expense", safeAmount(performance.expense())),
                List.of("Transfer Volume", safeAmount(performance.transferVolume())),
                List.of("Net Flow", safeAmount(performance.netFlow())),
                List.of("Generated At", formatDateTime(performance.generatedAt()))
        );

        return ExportDocumentBuilder.toCsv(List.of("Metric", "Value"), rows);
    }

    @Override
    public byte[] exportMonthlyPerformancePdf(Integer year, Integer month) {
        AdminDashboardDto.MonthlyPerformance performance = getMonthlyPerformance(year, month);
        List<String> lines = new ArrayList<>();
        lines.add("Month: " + performance.year() + "-" + String.format("%02d", performance.month()));
        lines.add("Range: " + performance.startDate() + " to " + performance.endDate());
        lines.add("Transactions: " + performance.transactions());
        lines.add("Wallet Transactions: " + performance.walletTransactions());
        lines.add("Transfers: " + performance.transfers());
        lines.add("New Users: " + performance.newUsers());
        lines.add("Failed Transactions: " + performance.failedTransactions());
        lines.add("Pending Transfers: " + performance.pendingTransfers());
        lines.add("Income: " + safeAmount(performance.income()));
        lines.add("Expense: " + safeAmount(performance.expense()));
        lines.add("Transfer Volume: " + safeAmount(performance.transferVolume()));
        lines.add("Net Flow: " + safeAmount(performance.netFlow()));
        lines.add("Generated At: " + formatDateTime(performance.generatedAt()));

        return ExportDocumentBuilder.toSimplePdf("Admin Monthly Performance", lines);
    }

    @Override
    public List<UserDto.Response> getAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toUserResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserDto.Response createUser(UserDto.Request request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already exists: " + request.username());
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists: " + request.email());
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(request.role() == null ? UserRole.USER : request.role())
                .build();

        return toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDto.Response updateUser(Long id, UserDto.AdminUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!Objects.equals(user.getUsername(), request.username())
                && userRepository.existsByUsernameAndIdNot(request.username(), id)) {
            throw new DuplicateResourceException("Username already exists: " + request.username());
        }

        if (!Objects.equals(user.getEmail(), request.email())
                && userRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new DuplicateResourceException("Email already exists: " + request.email());
        }

        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setRole(request.role() == null ? user.getRole() : request.role());

        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        return toUserResponse(userRepository.save(user));
    }

    @Override
    public List<BankAccountDto.Response> getAllAccounts() {
        return bankAccountRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toBankAccountResponse)
                .toList();
    }

    @Override
    @Transactional
    public BankAccountDto.Response updateAccount(Long id, BankAccountDto.AdminUpdateRequest request) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + id));

        if (!Objects.equals(account.getAccountNumber(), request.accountNumber())
                && bankAccountRepository.existsByAccountNumber(request.accountNumber())) {
            throw new DuplicateResourceException(
                    "Bank account already exists with account number: " + request.accountNumber());
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.userId()));

        Bank bank = bankRepository.findByBankNameIgnoreCase(request.bankName())
                .orElseThrow(() -> new ResourceNotFoundException("Bank not found: " + request.bankName()));

        account.setUser(user);
        account.setBank(bank);
        account.setAccountNumber(request.accountNumber());
        account.setBalance(request.balance());

        return toBankAccountResponse(bankAccountRepository.save(account));
    }

    @Override
    public List<TransactionDto.Response> getRecentActivities() {
        return transactionService.getRecentActivities();
    }

    private UserDto.Response toUserResponse(User user) {
        return new UserDto.Response(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

    private BankAccountDto.Response toBankAccountResponse(BankAccount account) {
        return new BankAccountDto.Response(
                account.getId(),
                account.getUser().getId(),
                account.getBank().getBankName(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCreatedAt()
        );
    }

    private AdminDashboardDto.TransactionItem toTransactionItem(Transaction transaction) {
        return new AdminDashboardDto.TransactionItem(
                transaction.getId(),
                transaction.getUser() == null ? null : transaction.getUser().getId(),
                transaction.getUser() == null ? null : transaction.getUser().getFullName(),
                transaction.getTransactionType() == null ? null : transaction.getTransactionType().name(),
                transaction.getStatus() == null ? null : transaction.getStatus().name(),
                transaction.getAmount(),
                transaction.getReceiverName(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }

    private AdminDashboardDto.TransferItem toTransferItem(Transaction transaction) {
        return new AdminDashboardDto.TransferItem(
                transaction.getId(),
                transaction.getUser() == null ? null : transaction.getUser().getId(),
                transaction.getUser() == null ? null : transaction.getUser().getFullName(),
                transaction.getTransferType() == null ? null : transaction.getTransferType().name(),
                transaction.getStatus() == null ? null : transaction.getStatus().name(),
                transaction.getAmount(),
                transaction.getSourceBankAccount() == null ? null : transaction.getSourceBankAccount().getId(),
                transaction.getDestinationValue(),
                transaction.getReceiverName(),
                transaction.getCreatedAt()
        );
    }

    private AdminDashboardDto.UserRegistrationItem toUserRegistrationItem(User user) {
        return new AdminDashboardDto.UserRegistrationItem(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole() == null ? null : user.getRole().name(),
                user.getCreatedAt()
        );
    }

    private AdminDashboardDto.LowBalanceIssue toLowBalanceIssue(BankAccount account) {
        return new AdminDashboardDto.LowBalanceIssue(
                account.getId(),
                account.getUser() == null ? null : account.getUser().getId(),
                account.getUser() == null ? null : account.getUser().getFullName(),
                account.getBank() == null ? null : account.getBank().getBankName(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCreatedAt()
        );
    }

    private AdminDashboardDto.WalletTransactionItem toWalletTransactionItem(WalletTransaction walletTransaction) {
        Wallet wallet = walletTransaction.getWallet();
        User user = wallet == null ? null : wallet.getUser();
        return new AdminDashboardDto.WalletTransactionItem(
                walletTransaction.getId(),
                wallet == null ? null : wallet.getId(),
                user == null ? null : user.getId(),
                user == null ? null : user.getFullName(),
                wallet == null ? null : wallet.getName(),
                walletTransaction.getType() == null ? null : walletTransaction.getType().name(),
                walletTransaction.getAmount(),
                walletTransaction.getCategory(),
                walletTransaction.getDescription(),
                walletTransaction.getCreatedAt()
        );
    }

    private AdminDashboardDto.LowWalletBalanceIssue toLowWalletBalanceIssue(Wallet wallet) {
        User user = wallet.getUser();
        return new AdminDashboardDto.LowWalletBalanceIssue(
                wallet.getId(),
                user == null ? null : user.getId(),
                user == null ? null : user.getFullName(),
                wallet.getName(),
                wallet.getBalance(),
                wallet.getCurrency(),
                wallet.getCreatedAt()
        );
    }

        private YearMonth resolveYearMonth(Integer year, Integer month) {
                LocalDate now = LocalDate.now();
                int effectiveYear = year == null ? now.getYear() : year;
                int effectiveMonth = month == null ? now.getMonthValue() : month;
                if (effectiveMonth < 1 || effectiveMonth > 12) {
                        throw new IllegalArgumentException("Month must be between 1 and 12");
                }
                return YearMonth.of(effectiveYear, effectiveMonth);
        }

        private String safeAmount(BigDecimal amount) {
                return amount == null ? "0" : amount.toPlainString();
        }

        private String formatDateTime(LocalDateTime value) {
                if (value == null) {
                        return "-";
                }
                return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
}
