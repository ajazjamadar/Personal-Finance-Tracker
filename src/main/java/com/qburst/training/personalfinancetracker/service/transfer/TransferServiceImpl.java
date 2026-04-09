package com.qburst.training.personalfinancetracker.service.transfer;

import com.qburst.training.personalfinancetracker.dto.TransferDto;
import com.qburst.training.personalfinancetracker.entity.BankAccount;
import com.qburst.training.personalfinancetracker.entity.Transaction;
import com.qburst.training.personalfinancetracker.entity.Transaction.TransactionType;
import com.qburst.training.personalfinancetracker.exception.InsufficientBalanceException;
import com.qburst.training.personalfinancetracker.exception.ResourceNotFoundException;
import com.qburst.training.personalfinancetracker.repository.BankAccountRepository;
import com.qburst.training.personalfinancetracker.repository.TransactionRepository;
import com.qburst.training.personalfinancetracker.security.AuthContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class TransferServiceImpl implements TransferService {

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    private final AuthContextService authContextService;

    public TransferServiceImpl(BankAccountRepository bankAccountRepository,
                               TransactionRepository transactionRepository,
                               AuthContextService authContextService) {
        this.bankAccountRepository = bankAccountRepository;
        this.transactionRepository = transactionRepository;
        this.authContextService = authContextService;
    }

    @Override
    @Transactional
    public TransferDto.Response transfer(TransferDto.Request request) {
        BankAccount sourceAccount = getBankAccount(request.sourceAccountId());
        authContextService.ensureCanAccessUser(sourceAccount.getUser().getId());

        boolean selfTransfer = Boolean.TRUE.equals(request.selfTransfer());

        checkBalance(sourceAccount.getBalance(), request.amount(), "Source account");
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.amount()));
        bankAccountRepository.save(sourceAccount);

        BankAccount destinationAccount = null;
        String destinationValue;

        switch (request.transferType()) {
            case ACCOUNT -> {
                if (request.destinationAccountId() == null) {
                    throw new IllegalArgumentException("Destination account ID is required for ACCOUNT transfer");
                }
                destinationAccount = getBankAccount(request.destinationAccountId());
                if (selfTransfer && !Objects.equals(sourceAccount.getUser().getId(), destinationAccount.getUser().getId())) {
                    throw new IllegalArgumentException("Self transfer must use your own destination account");
                }

                destinationAccount.setBalance(destinationAccount.getBalance().add(request.amount()));
                bankAccountRepository.save(destinationAccount);
                destinationValue = destinationAccount.getAccountNumber();
            }
            case MOBILE -> {
                if (request.mobileNumber() == null || request.mobileNumber().isBlank()) {
                    throw new IllegalArgumentException("Mobile number is required for MOBILE transfer");
                }
                destinationValue = request.mobileNumber();
            }
            case UPI -> {
                if (request.upiId() == null || request.upiId().isBlank()) {
                    throw new IllegalArgumentException("UPI ID is required for UPI transfer");
                }
                destinationValue = request.upiId();
            }
            default -> throw new IllegalArgumentException("Unsupported transfer type");
        }

        Transaction transaction = new Transaction();
        transaction.setUser(sourceAccount.getUser());
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setAmount(request.amount());
        transaction.setSourceBankAccount(sourceAccount);
        transaction.setDestBankAccount(destinationAccount);
        transaction.setTransferType(Transaction.TransferType.valueOf(request.transferType().name()));
        transaction.setSelfTransfer(selfTransfer);
        transaction.setDestinationValue(destinationValue);
        transaction.setDescription(request.description() == null || request.description().isBlank()
                ? "Transfer via " + request.transferType().name()
                : request.description());

        return toResponse(transactionRepository.save(transaction));
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private BankAccount getBankAccount(Long id) {
        return bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bank account not found with id: " + id));
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

    private TransferDto.Response toResponse(Transaction tx) {
        return new TransferDto.Response(
                tx.getId(),
                tx.getTransactionType().name(),
                tx.getTransferType() == null ? null : tx.getTransferType().name(),
                tx.getSelfTransfer(),
                tx.getSourceBankAccount() == null ? null : tx.getSourceBankAccount().getId(),
                tx.getDestBankAccount() == null ? null : tx.getDestBankAccount().getId(),
                tx.getDestinationValue(),
                tx.getAmount(),
                tx.getDescription(),
                tx.getCreatedAt());
    }
}