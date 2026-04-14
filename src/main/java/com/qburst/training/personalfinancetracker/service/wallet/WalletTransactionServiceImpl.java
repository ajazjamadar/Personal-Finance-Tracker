package com.qburst.training.personalfinancetracker.service.wallet;

import com.qburst.training.personalfinancetracker.dto.WalletTransactionDto;
import com.qburst.training.personalfinancetracker.entity.Wallet;
import com.qburst.training.personalfinancetracker.entity.WalletTransaction;
import com.qburst.training.personalfinancetracker.entity.WalletTransaction.WalletTransactionType;
import com.qburst.training.personalfinancetracker.exception.InsufficientBalanceException;
import com.qburst.training.personalfinancetracker.exception.ResourceNotFoundException;
import com.qburst.training.personalfinancetracker.repository.WalletRepository;
import com.qburst.training.personalfinancetracker.repository.WalletTransactionRepository;
import com.qburst.training.personalfinancetracker.security.AuthContextService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class WalletTransactionServiceImpl implements WalletTransactionService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final AuthContextService authContextService;

    public WalletTransactionServiceImpl(WalletRepository walletRepository,
                                        WalletTransactionRepository walletTransactionRepository,
                                        AuthContextService authContextService) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.authContextService = authContextService;
    }

    @Override
    @Transactional
    public WalletTransactionDto.Response deposit(WalletTransactionDto.DepositWithdrawRequest request) {
        Wallet wallet = getOwnedWallet(request.walletId());
        wallet.setBalance(wallet.getBalance().add(request.amount()));
        walletRepository.save(wallet);

        WalletTransaction tx = saveTransaction(
                wallet,
                WalletTransactionType.CREDIT,
                request.amount(),
                request.category(),
                request.description()
        );
        return toResponse(tx);
    }

    @Override
    @Transactional
    public WalletTransactionDto.Response withdraw(WalletTransactionDto.DepositWithdrawRequest request) {
        Wallet wallet = getOwnedWallet(request.walletId());
        ensureSufficientBalance(wallet.getBalance(), request.amount());
        wallet.setBalance(wallet.getBalance().subtract(request.amount()));
        walletRepository.save(wallet);

        WalletTransaction tx = saveTransaction(
                wallet,
                WalletTransactionType.DEBIT,
                request.amount(),
                request.category(),
                request.description()
        );
        return toResponse(tx);
    }

    @Override
    @Transactional
    public WalletTransactionDto.TransferResponse transfer(WalletTransactionDto.TransferRequest request) {
        if (request.fromWalletId().equals(request.toWalletId())) {
            throw new IllegalArgumentException("From and To wallet cannot be the same");
        }

        Wallet fromWallet = getOwnedWallet(request.fromWalletId());
        Wallet toWallet = getOwnedWallet(request.toWalletId());

        ensureSufficientBalance(fromWallet.getBalance(), request.amount());

        fromWallet.setBalance(fromWallet.getBalance().subtract(request.amount()));
        toWallet.setBalance(toWallet.getBalance().add(request.amount()));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        String category = request.category();
        WalletTransaction debitTx = saveTransaction(
                fromWallet,
                WalletTransactionType.TRANSFER,
                request.amount(),
                category,
                request.description() == null || request.description().isBlank()
                        ? "Transfer to wallet #" + toWallet.getId()
                        : request.description()
        );

        WalletTransaction creditTx = saveTransaction(
                toWallet,
                WalletTransactionType.TRANSFER,
                request.amount(),
                category,
                request.description() == null || request.description().isBlank()
                        ? "Transfer from wallet #" + fromWallet.getId()
                        : request.description()
        );

        return new WalletTransactionDto.TransferResponse(
                fromWallet.getId(),
                toWallet.getId(),
                request.amount(),
                toTransferResponse(debitTx, WalletTransactionType.DEBIT),
                toTransferResponse(creditTx, WalletTransactionType.CREDIT),
                fromWallet.getBalance(),
                toWallet.getBalance(),
                creditTx.getCreatedAt()
        );
    }

    @Override
    public Page<WalletTransactionDto.Response> listWalletTransactions(Long walletId, int page, int size) {
        Wallet wallet = getOwnedWallet(walletId);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return walletTransactionRepository
                .findByWalletIdOrderByCreatedAtDesc(wallet.getId(), PageRequest.of(safePage, safeSize))
                .map(this::toResponse);
    }

    private Wallet getOwnedWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + walletId));
        authContextService.ensureCanAccessUser(wallet.getUser().getId());
        return wallet;
    }

    private void ensureSufficientBalance(BigDecimal current, BigDecimal requested) {
        if (requested.compareTo(current) > 0) {
            throw new InsufficientBalanceException(
                    "Wallet has insufficient balance. Available: " + current + ", Requested: " + requested
            );
        }
    }

    private WalletTransaction saveTransaction(Wallet wallet,
                                              WalletTransactionType type,
                                              BigDecimal amount,
                                              String category,
                                              String description) {
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .type(type)
                .amount(amount)
                .category(normalize(category))
                .description(normalize(description))
                .build();
        return walletTransactionRepository.save(transaction);
    }

    private WalletTransactionDto.Response toResponse(WalletTransaction transaction) {
        return new WalletTransactionDto.Response(
                transaction.getId(),
                transaction.getWallet() == null ? null : transaction.getWallet().getId(),
                transaction.getType() == null ? null : transaction.getType().name(),
                transaction.getAmount(),
                transaction.getCategory(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }

    private WalletTransactionDto.Response toTransferResponse(WalletTransaction transaction,
                                                             WalletTransactionType directionType) {
        return new WalletTransactionDto.Response(
                transaction.getId(),
                transaction.getWallet() == null ? null : transaction.getWallet().getId(),
                directionType.name(),
                transaction.getAmount(),
                transaction.getCategory(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
