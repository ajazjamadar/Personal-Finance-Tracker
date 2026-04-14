package com.qburst.training.personalfinancetracker.dto;

import java.math.BigDecimal;
import java.util.List;

public class ReportDto {

    public record MonthlyExpense(
            String month,
            int year,
            BigDecimal totalExpense
    ) {}

    public record CategoryExpense(
            String category,
            BigDecimal totalExpense
    ) {}

    public record WalletBalance(
            String walletName,
            BigDecimal balance,
            String currency
    ) {}

    public record IncomeExpenseSummary(
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal netSavings
    ) {}

    public record Overview(
            List<BankAccountDto.BalanceSummary> bankBalances,
            List<WalletBalance> walletBalances,
            List<MonthlyExpense> monthlyExpenses,
            List<CategoryExpense> expenseByCategory,
            IncomeExpenseSummary incomeExpenseSummary
    ) {}
}
