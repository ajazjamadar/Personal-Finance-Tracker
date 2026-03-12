package com.qburst.training.personalfinancetracker.service;

import com.qburst.training.personalfinancetracker.dto.TransactionRequest;

public interface TransactionService {
    String recordIncome(TransactionRequest request);
    String recordExpense(TransactionRequest request);
    String recordAtmWithdrawal(TransactionRequest request);
    String getTransactionsByUserId(Long userId);
    String getTransactionById(Long id);

    String recordBankExpense(TransactionRequest request);
}
