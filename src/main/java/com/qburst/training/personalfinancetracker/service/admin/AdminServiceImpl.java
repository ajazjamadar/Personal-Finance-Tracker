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
import com.qburst.training.personalfinancetracker.exception.DuplicateResourceException;
import com.qburst.training.personalfinancetracker.exception.ResourceNotFoundException;
import com.qburst.training.personalfinancetracker.repository.BankAccountRepository;
import com.qburst.training.personalfinancetracker.repository.BankRepository;
import com.qburst.training.personalfinancetracker.repository.TransactionRepository;
import com.qburst.training.personalfinancetracker.repository.UserRepository;
import com.qburst.training.personalfinancetracker.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private static final BigDecimal LOW_BALANCE_THRESHOLD = new BigDecimal("1000.00");
    private static final BigDecimal SUSPICIOUS_TRANSACTION_THRESHOLD = new BigDecimal("50000.00");
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

        AdminDashboardDto.KeyMetrics keyMetrics = new AdminDashboardDto.KeyMetrics(
                userRepository.count(),
                bankAccountRepository.count(),
                transactionRepository.count(),
                transactionRepository.sumAmountByTransactionTypeAndStatuses(
                        Transaction.TransactionType.TRANSFER,
                        ACTIVE_TRANSFER_STATUSES
                )
        );

        AdminDashboardDto.Snapshot snapshot = new AdminDashboardDto.Snapshot(
                new AdminDashboardDto.ActivityWindow(
                        transactionRepository.countByCreatedAtBetween(startOfToday, startOfTomorrow),
                        transactionRepository.countByTransactionTypeAndCreatedAtBetween(
                                Transaction.TransactionType.TRANSFER, startOfToday, startOfTomorrow),
                        userRepository.countByCreatedAtBetween(startOfToday, startOfTomorrow)
                ),
                new AdminDashboardDto.ActivityWindow(
                        transactionRepository.countByCreatedAtBetween(startOfMonth, startOfNextMonth),
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
                "UP",
                now
        );

        AdminDashboardDto.RecentActivity recentActivity = new AdminDashboardDto.RecentActivity(
                transactionRepository.findTop8ByOrderByCreatedAtDesc().stream()
                        .map(this::toTransactionItem)
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
                transactionRepository.findTop8ByAmountGreaterThanEqualOrderByCreatedAtDesc(SUSPICIOUS_TRANSACTION_THRESHOLD)
                        .stream()
                        .map(this::toTransactionItem)
                        .toList()
        );

        return new AdminDashboardDto.Response(
                keyMetrics,
                snapshot,
                systemHealth,
                recentActivity,
                alerts,
                new AdminDashboardDto.Thresholds(LOW_BALANCE_THRESHOLD, SUSPICIOUS_TRANSACTION_THRESHOLD),
                now
        );
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
}
