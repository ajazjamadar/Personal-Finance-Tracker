package com.qburst.training.personalfinancetracker.service.transfer;

import com.qburst.training.personalfinancetracker.dto.TransactionResponse;
import com.qburst.training.personalfinancetracker.dto.TransferRequest;
import com.qburst.training.personalfinancetracker.entity.BankAccount;
import com.qburst.training.personalfinancetracker.entity.Transaction;
import com.qburst.training.personalfinancetracker.entity.Transaction.TransactionType;
import com.qburst.training.personalfinancetracker.entity.Wallet;
import com.qburst.training.personalfinancetracker.exception.InsufficientBalanceException;
import com.qburst.training.personalfinancetracker.exception.ResourceNotFoundException;
import com.qburst.training.personalfinancetracker.repository.BankAccountRepository;
import com.qburst.training.personalfinancetracker.repository.TransactionRepository;
import com.qburst.training.personalfinancetracker.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TransferServiceImpl implements TransferService {

    private final BankAccountRepository bankAccountRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public TransferServiceImpl(BankAccountRepository bankAccountRepository,
                               WalletRepository walletRepository,
                               TransactionRepository transactionRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public TransactionResponse bankToWallet(TransferRequest request) {
        BankAccount bank = getBankAccount(request.sourceId());
        Wallet wallet = getWallet(request.destinationId());

        checkBalance(bank.getBalance(), request.amount(), "Bank account");

        bank.setBalance(bank.getBalance().subtract(request.amount()));
        wallet.setBalance(wallet.getBalance().add(request.amount()));

        bankAccountRepository.save(bank);
        walletRepository.save(wallet);

        Transaction tx1 = new Transaction();
        tx1.setUser(bank.getUser());
        tx1.setTransactionType(TransactionType.TRANSFER);
        tx1.setAmount(request.amount());
        tx1.setSourceBankAccount(bank);
        tx1.setDestWallet(wallet);
        tx1.setDescription("Bank to wallet transfer");
        return toResponse(transactionRepository.save(tx1));
    }

    @Override
    @Transactional
    public TransactionResponse walletToBank(TransferRequest request) {
        Wallet wallet = getWallet(request.sourceId());
        BankAccount bank = getBankAccount(request.destinationId());

        checkBalance(wallet.getBalance(), request.amount(), "Wallet");

        wallet.setBalance(wallet.getBalance().subtract(request.amount()));
        bank.setBalance(bank.getBalance().add(request.amount()));

        walletRepository.save(wallet);
        bankAccountRepository.save(bank);

        Transaction tx2 = new Transaction();
        tx2.setUser(wallet.getUser());
        tx2.setTransactionType(TransactionType.TRANSFER);
        tx2.setAmount(request.amount());
        tx2.setSourceWallet(wallet);
        tx2.setDestBankAccount(bank);
        tx2.setDescription("Wallet to bank transfer");
        return toResponse(transactionRepository.save(tx2));
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private BankAccount getBankAccount(Long id) {
        return bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bank account not found with id: " + id));
    }

    private Wallet getWallet(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wallet not found with id: " + id));
    }

    private void checkBalance(java.math.BigDecimal current,
                              java.math.BigDecimal requested,
                              String accountType) {
        if (requested.compareTo(current) > 0) {
            throw new InsufficientBalanceException(
                    accountType + " has insufficient balance. " +
                            "Available: " + current + ", Requested: " + requested);
        }
    }

    private TransactionResponse toResponse(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getTransactionType().name(),
                tx.getAmount(),
                tx.getDescription(),
                tx.getCreatedAt());
    }
}