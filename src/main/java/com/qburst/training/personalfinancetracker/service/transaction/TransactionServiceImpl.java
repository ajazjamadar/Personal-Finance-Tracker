package com.qburst.training.personalfinancetracker.service.transaction;

import com.qburst.training.personalfinancetracker.dto.TransactionDto;
import com.qburst.training.personalfinancetracker.entity.BankAccount;
import com.qburst.training.personalfinancetracker.entity.Transaction;
import com.qburst.training.personalfinancetracker.entity.Transaction.PaymentMethod;
import com.qburst.training.personalfinancetracker.entity.Transaction.TransactionStatus;
import com.qburst.training.personalfinancetracker.entity.Transaction.TransactionType;
import com.qburst.training.personalfinancetracker.entity.User;
import com.qburst.training.personalfinancetracker.exception.InsufficientBalanceException;
import com.qburst.training.personalfinancetracker.exception.ResourceNotFoundException;
import com.qburst.training.personalfinancetracker.repository.BankAccountRepository;
import com.qburst.training.personalfinancetracker.repository.TransactionRepository;
import com.qburst.training.personalfinancetracker.repository.TransactionSpecifications;
import com.qburst.training.personalfinancetracker.repository.UserRepository;
import com.qburst.training.personalfinancetracker.security.AuthContextService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final AuthContextService authContextService;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  BankAccountRepository bankAccountRepository,
                                  UserRepository userRepository,
                                  AuthContextService authContextService) {
        this.transactionRepository = transactionRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
        this.authContextService = authContextService;
    }

    @Override
    @Transactional
    public TransactionDto.Response recordIncome(TransactionDto.Request request) {
        BankAccount account = getBankAccount(request.bankAccountId());
        authContextService.ensureCanAccessUser(account.getUser().getId());
        account.setBalance(account.getBalance().add(request.amount()));
        bankAccountRepository.save(account);
        return buildAndSave(
            account.getUser(), TransactionType.INCOME,
                request.amount(), request.description(),
            account, null, request.category(), request.receiverName(), resolvePaymentMethod(request.paymentMethod(), PaymentMethod.NET_BANKING));
    }

    @Override
    @Transactional
    public TransactionDto.Response recordExpense(TransactionDto.Request request) {
        BankAccount account = getBankAccount(request.bankAccountId());
        authContextService.ensureCanAccessUser(account.getUser().getId());
        checkBalance(account.getBalance(), request.amount(), "Bank account");
        account.setBalance(account.getBalance().subtract(request.amount()));
        bankAccountRepository.save(account);
        return buildAndSave(
            account.getUser(), TransactionType.EXPENSE,
                request.amount(), request.description(),
            account, null, request.category(), request.receiverName(), resolvePaymentMethod(request.paymentMethod(), PaymentMethod.CARD));
    }

    @Override
    @Transactional
    public TransactionDto.Response recordAtmWithdrawal(TransactionDto.Request request) {
        BankAccount account = getBankAccount(request.bankAccountId());
        authContextService.ensureCanAccessUser(account.getUser().getId());
        checkBalance(account.getBalance(), request.amount(), "Bank account");
        account.setBalance(account.getBalance().subtract(request.amount()));
        bankAccountRepository.save(account);
        return buildAndSave(
                account.getUser(), TransactionType.ATM_WITHDRAWAL,
                request.amount(), request.description(),
            account, null, "ATM", request.receiverName(), resolvePaymentMethod(request.paymentMethod(), PaymentMethod.WALLET));
    }

    @Override
    public List<TransactionDto.Response> getTransactionsByUserId(Long userId, TransactionDto.HistoryFilter filter) {
        Long effectiveUserId = authContextService.resolveUserId(userId);
        validateUserExistsById(effectiveUserId);
        TransactionDto.HistoryFilter normalizedFilter = filter == null ? null : filter.normalized();
        return transactionRepository.findAll(
                        TransactionSpecifications.historyForUser(effectiveUserId, normalizedFilter),
                        Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public TransactionDto.Response getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .map(transaction -> {
                    authContextService.ensureCanAccessUser(transaction.getUser().getId());
                    return toResponse(transaction);
                })
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction not found with id: " + id));
    }

    @Override
    public List<TransactionDto.Response> getRecentActivities() {
        return transactionRepository.findTop50ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private TransactionDto.Response buildAndSave(
            User user,
            TransactionType type,
            BigDecimal amount,
            String description,
            BankAccount sourceBankAccount,
            BankAccount destBankAccount,
            String categoryName,
            String receiverName,
            PaymentMethod paymentMethod) {

        Transaction tx = new Transaction();
        tx.setUser(user);
        tx.setTransactionType(type);
        tx.setAmount(amount);
        tx.setDescription(description);
        tx.setSourceBankAccount(sourceBankAccount);
        tx.setDestBankAccount(destBankAccount);
        tx.setCategoryName(normalizeText(categoryName));
        tx.setReceiverName(normalizeText(receiverName));
        tx.setPaymentMethod(paymentMethod);
        tx.setStatus(TransactionStatus.SUCCESS);

        return toResponse(transactionRepository.save(tx));
    }

    private void checkBalance(BigDecimal current, BigDecimal requested, String accountType) {
        if (requested.compareTo(current) > 0) {
            throw new InsufficientBalanceException(
                    accountType + " has insufficient balance. " +
                            "Available: " + current + ", Requested: " + requested);
        }
    }

    private void validateUserExistsById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }

    private BankAccount getBankAccount(Long bankAccountId) {
        return bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bank account not found with id: " + bankAccountId));
    }

    private TransactionDto.Response toResponse(Transaction tx) {
        return new TransactionDto.Response(
                tx.getId(),
                tx.getUser() == null ? null : tx.getUser().getId(),
                tx.getTransactionType().name(),
                tx.getTransferType() == null ? null : tx.getTransferType().name(),
                tx.getSelfTransfer(),
                tx.getSourceBankAccount() == null ? null : tx.getSourceBankAccount().getId(),
                tx.getDestBankAccount() == null ? null : tx.getDestBankAccount().getId(),
                tx.getDestinationValue(),
                tx.getAmount(),
                tx.getDescription(),
                tx.getCategoryName(),
                tx.getStatus() == null ? null : tx.getStatus().name(),
                tx.getPaymentMethod() == null ? null : tx.getPaymentMethod().name(),
                tx.getReceiverName(),
                tx.getCreatedAt());
    }

    private PaymentMethod resolvePaymentMethod(String raw, PaymentMethod fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        return PaymentMethod.valueOf(raw.trim().toUpperCase(Locale.ROOT).replace(' ', '_'));
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
