package com.qburst.training.personalfinancetracker.service.transaction;

import com.qburst.training.personalfinancetracker.dto.TransactionDto;
import com.qburst.training.personalfinancetracker.entity.BankAccount;
import com.qburst.training.personalfinancetracker.entity.Transaction;
import com.qburst.training.personalfinancetracker.entity.Transaction.TransactionType;
import com.qburst.training.personalfinancetracker.entity.User;
import com.qburst.training.personalfinancetracker.entity.Wallet;
import com.qburst.training.personalfinancetracker.exception.InsufficientBalanceException;
import com.qburst.training.personalfinancetracker.exception.ResourceNotFoundException;
import com.qburst.training.personalfinancetracker.repository.BankAccountRepository;
import com.qburst.training.personalfinancetracker.repository.TransactionRepository;
import com.qburst.training.personalfinancetracker.repository.UserRepository;
import com.qburst.training.personalfinancetracker.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  WalletRepository walletRepository,
                                  BankAccountRepository bankAccountRepository,
                                  UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public TransactionDto.Response recordIncome(TransactionDto.Request request) {
        Wallet wallet = getWallet(request.walletId());
        wallet.setBalance(wallet.getBalance().add(request.amount()));
        walletRepository.save(wallet);
        return buildAndSave(
                wallet.getUser(), TransactionType.INCOME,
                request.amount(), request.description(),
                null, wallet, null, null);
    }

    @Override
    @Transactional
    public TransactionDto.Response recordExpense(TransactionDto.Request request) {
        Wallet wallet = getWallet(request.walletId());
        checkBalance(wallet.getBalance(), request.amount(), "Wallet");
        wallet.setBalance(wallet.getBalance().subtract(request.amount()));
        walletRepository.save(wallet);
        return buildAndSave(
                wallet.getUser(), TransactionType.EXPENSE,
                request.amount(), request.description(),
                wallet, null, null, null);
    }

    @Override
    @Transactional
    public TransactionDto.Response recordAtmWithdrawal(TransactionDto.Request request) {
        BankAccount account = getBankAccount(request.bankAccountId());
        checkBalance(account.getBalance(), request.amount(), "Bank account");
        account.setBalance(account.getBalance().subtract(request.amount()));
        bankAccountRepository.save(account);
        return buildAndSave(
                account.getUser(), TransactionType.ATM_WITHDRAWAL,
                request.amount(), request.description(),
                null, null, account, null);
    }

    @Override
    @Transactional
    public TransactionDto.Response recordBankExpense(TransactionDto.Request request) {
        BankAccount account = getBankAccount(request.bankAccountId());
        checkBalance(account.getBalance(), request.amount(), "Bank account");
        account.setBalance(account.getBalance().subtract(request.amount()));
        bankAccountRepository.save(account);
        return buildAndSave(
                account.getUser(), TransactionType.EXPENSE,
                request.amount(), request.description(),
                null, null, account, null);
    }

    @Override
    public List<TransactionDto.Response> getTransactionsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public TransactionDto.Response getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction not found with id: " + id));
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private TransactionDto.Response buildAndSave(
            User user,
            TransactionType type,
            BigDecimal amount,
            String description,
            Wallet sourceWallet,
            Wallet destWallet,
            BankAccount sourceBankAccount,
            BankAccount destBankAccount) {

        Transaction tx = new Transaction();
        tx.setUser(user);
        tx.setTransactionType(type);
        tx.setAmount(amount);
        tx.setDescription(description);
        tx.setSourceWallet(sourceWallet);
        tx.setDestWallet(destWallet);
        tx.setSourceBankAccount(sourceBankAccount);
        tx.setDestBankAccount(destBankAccount);

        return toResponse(transactionRepository.save(tx));
    }

    private void checkBalance(BigDecimal current, BigDecimal requested, String accountType) {
        if (requested.compareTo(current) > 0) {
            throw new InsufficientBalanceException(
                    accountType + " has insufficient balance. " +
                            "Available: " + current + ", Requested: " + requested);
        }
    }

    private Wallet getWallet(Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wallet not found with id: " + walletId));
    }

    private BankAccount getBankAccount(Long bankAccountId) {
        return bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bank account not found with id: " + bankAccountId));
    }

    private TransactionDto.Response toResponse(Transaction tx) {
        return new TransactionDto.Response(
                tx.getId(),
                tx.getTransactionType().name(),
                tx.getAmount(),
                tx.getDescription(),
                tx.getCreatedAt());
    }
}