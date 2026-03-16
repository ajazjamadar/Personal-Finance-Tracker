package com.qburst.training.personalfinancetracker.service;

import com.qburst.training.personalfinancetracker.dto.TransactionDto;
import java.util.List;

public interface TransactionService {
    TransactionDto.Response recordIncome(TransactionDto.Request request);
    TransactionDto.Response recordExpense(TransactionDto.Request request);
    TransactionDto.Response recordAtmWithdrawal(TransactionDto.Request request);
    TransactionDto.Response recordBankExpense(TransactionDto.Request request);
    List<TransactionDto.Response> getTransactionsByUserId(Long userId);
    TransactionDto.Response getTransactionById(Long id);
}