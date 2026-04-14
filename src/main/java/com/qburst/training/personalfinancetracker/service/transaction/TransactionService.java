package com.qburst.training.personalfinancetracker.service.transaction;

import com.qburst.training.personalfinancetracker.dto.TransactionDto;
import java.util.List;

public interface TransactionService {
    TransactionDto.Response recordIncome(TransactionDto.Request request);
    TransactionDto.Response recordExpense(TransactionDto.Request request);
    List<TransactionDto.Response> getTransactionsByUserId(Long userId, TransactionDto.HistoryFilter filter);
    TransactionDto.Response getTransactionById(Long id);
    List<TransactionDto.Response> getRecentActivities();
}
