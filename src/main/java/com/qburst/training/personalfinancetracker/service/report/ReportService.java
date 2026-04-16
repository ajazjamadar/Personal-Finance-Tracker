package com.qburst.training.personalfinancetracker.service.report;

import com.qburst.training.personalfinancetracker.dto.BankAccountDto;
import com.qburst.training.personalfinancetracker.dto.ReportDto;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    List<BankAccountDto.BalanceSummary> getBankBalanceSummary(Long userId);
    List<ReportDto.MonthlyExpense> getMonthlyExpenses(Long userId);
    List<ReportDto.CategoryExpense> getExpenseByCategory(Long userId);
    ReportDto.IncomeExpenseSummary getIncomeExpenseSummary(Long userId);
    ReportDto.Overview getOverview(Long userId);
    byte[] exportBankStatementCsv(Long userId, LocalDate fromDate, LocalDate toDate);
    byte[] exportBankStatementPdf(Long userId, LocalDate fromDate, LocalDate toDate);
    byte[] exportOverviewCsv(Long userId);
    byte[] exportOverviewPdf(Long userId);
}
