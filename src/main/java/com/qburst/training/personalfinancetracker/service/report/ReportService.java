package com.qburst.training.personalfinancetracker.service.report;

import com.qburst.training.personalfinancetracker.dto.BankAccountDto;
import com.qburst.training.personalfinancetracker.dto.ReportDto;

import java.util.List;

public interface ReportService {
    List<BankAccountDto.BalanceSummary> getBankBalanceSummary(Long userId);
    List<ReportDto.MonthlyExpense> getMonthlyExpenses(Long userId);
    List<ReportDto.CategoryExpense> getExpenseByCategory(Long userId);
    ReportDto.IncomeExpenseSummary getIncomeExpenseSummary(Long userId);
}