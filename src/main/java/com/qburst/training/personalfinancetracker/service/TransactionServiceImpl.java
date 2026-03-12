package com.qburst.training.personalfinancetracker.service;

import com.qburst.training.personalfinancetracker.dto.TransactionRequest;
import com.qburst.training.personalfinancetracker.exception.InsufficientBalanceException;
import com.qburst.training.personalfinancetracker.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransactionServiceImpl implements TransactionService {

    // Simulated balance — will come from DB in later
    private BigDecimal simulatedWalletBalance = new BigDecimal("1000.00");
    private BigDecimal simulatedBankBalance = new BigDecimal("5000.00");

    @Override
    public String recordIncome(TransactionRequest request) {
        validateTransactionRequest(request);
        simulatedWalletBalance = simulatedWalletBalance.add(request.getAmount());
        return "Income of " + request.getAmount() + " recorded. New balance: " + simulatedWalletBalance;
    }

    @Override
    public String recordExpense(TransactionRequest request) {
        validateTransactionRequest(request);
        checkSufficientBalance(simulatedWalletBalance, request.getAmount(), "Wallet");
        simulatedWalletBalance = simulatedWalletBalance.subtract(request.getAmount());
        return "Expense of " + request.getAmount() + " recorded. New balance: " + simulatedWalletBalance;
    }

    @Override
    public String recordAtmWithdrawal(TransactionRequest request) {
        validateTransactionRequest(request);
        checkSufficientBalance(simulatedBankBalance, request.getAmount(), "Bank account");
        simulatedBankBalance = simulatedBankBalance.subtract(request.getAmount());
        return "ATM withdrawal of " + request.getAmount() + " recorded. New bank balance: " + simulatedBankBalance;
    }

    @Override
    public String getTransactionsByUserId(Long userId) {
        if (userId <= 0) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return "Transactions for user: " + userId;
    }

    @Override
    public String getTransactionById(Long id) {
        if (id <= 0) {
            throw new ResourceNotFoundException("Transaction not found with id: " + id);
        }
        return "Transaction found with id: " + id;
    }

    // ─── Business Rule: Balance Check ────────────────────────────────────────

    private void checkSufficientBalance(BigDecimal currentBalance, BigDecimal amount, String accountType) {
        if (amount.compareTo(currentBalance) > 0) {
            throw new InsufficientBalanceException(
                    accountType + " has insufficient balance. " +
                            "Available: " + currentBalance + ", Requested: " + amount
            );
        }
    }

    @Override
    public String recordBankExpense(TransactionRequest request) {
        validateTransactionRequest(request);
        checkSufficientBalance(simulatedBankBalance, request.getAmount(), "Bank account");
        simulatedBankBalance = simulatedBankBalance.subtract(request.getAmount());
        return "Bank expense of " + request.getAmount() + " recorded. New bank balance: " + simulatedBankBalance;
    }

    private void validateTransactionRequest(TransactionRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be greater than zero");
        }
    }
}

