package com.qburst.training.personalfinancetracker.dto;

import java.math.BigDecimal;

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

    public record IncomeExpenseSummary(
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal netSavings
    ) {}
}