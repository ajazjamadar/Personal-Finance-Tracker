package com.qburst.training.personalfinancetracker.service.report;

import com.qburst.training.personalfinancetracker.dto.BankAccountDto;
import com.qburst.training.personalfinancetracker.dto.ReportDto;
import com.qburst.training.personalfinancetracker.dto.TransactionDto;
import com.qburst.training.personalfinancetracker.entity.Transaction;
import com.qburst.training.personalfinancetracker.entity.Transaction.TransactionType;
import com.qburst.training.personalfinancetracker.exception.ResourceNotFoundException;
import com.qburst.training.personalfinancetracker.repository.BankAccountRepository;
import com.qburst.training.personalfinancetracker.repository.TransactionRepository;
import com.qburst.training.personalfinancetracker.repository.UserRepository;
import com.qburst.training.personalfinancetracker.repository.WalletRepository;
import com.qburst.training.personalfinancetracker.security.AuthContextService;
import com.qburst.training.personalfinancetracker.service.transaction.TransactionService;
import com.qburst.training.personalfinancetracker.util.ExportDocumentBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {
    private static final List<TransactionType> EXPENSE_TYPES = List.of(
            TransactionType.EXPENSE,
            TransactionType.TRANSFER
    );
    private static final Transaction.TransactionStatus INCLUDED_STATUS = Transaction.TransactionStatus.SUCCESS;

    private final BankAccountRepository bankAccountRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AuthContextService authContextService;
    private final TransactionService transactionService;

    public ReportServiceImpl(BankAccountRepository bankAccountRepository,
                             WalletRepository walletRepository,
                             TransactionRepository transactionRepository,
                             UserRepository userRepository,
                             AuthContextService authContextService,
                             TransactionService transactionService) {
        this.bankAccountRepository = bankAccountRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.authContextService = authContextService;
        this.transactionService = transactionService;
    }

    @Override
    public List<BankAccountDto.BalanceSummary> getBankBalanceSummary(Long userId) {
        Long effectiveUserId = resolveAndValidateUserById(userId);
        return bankAccountRepository.findBalanceRowsByUserId(effectiveUserId)
                .stream()
                .map(row -> new BankAccountDto.BalanceSummary(
                        row.getBankName(),
                        row.getAccountNumber(),
                        row.getBalance()))
                .toList();
    }

    @Override
    public List<ReportDto.MonthlyExpense> getMonthlyExpenses(Long userId) {
        Long effectiveUserId = resolveAndValidateUserById(userId);
        return transactionRepository.findMonthlyExpenseRowsByUserId(effectiveUserId, EXPENSE_TYPES, INCLUDED_STATUS)
                .stream()
                .map(row -> new ReportDto.MonthlyExpense(
                        monthLabel(row.getMonthValue()),
                        row.getYearValue(),
                        row.getTotalExpense()))
                .toList();
    }

    @Override
    public List<ReportDto.CategoryExpense> getExpenseByCategory(Long userId) {
        Long effectiveUserId = resolveAndValidateUserById(userId);
        return transactionRepository.findCategoryExpenseRowsByUserId(effectiveUserId, EXPENSE_TYPES, INCLUDED_STATUS)
                .stream()
                .map(row -> new ReportDto.CategoryExpense(row.getCategory(), row.getTotalExpense()))
                .toList();
    }

    @Override
    public ReportDto.IncomeExpenseSummary getIncomeExpenseSummary(Long userId) {
        Long effectiveUserId = resolveAndValidateUserById(userId);
        TransactionRepository.IncomeExpenseSummaryRow summaryRow =
                transactionRepository.findIncomeExpenseSummaryByUserId(
                        effectiveUserId,
                        TransactionType.INCOME,
                        EXPENSE_TYPES,
                        INCLUDED_STATUS
                );

        BigDecimal totalIncome = summaryRow == null ? BigDecimal.ZERO : summaryRow.getTotalIncome();
        BigDecimal totalExpense = summaryRow == null ? BigDecimal.ZERO : summaryRow.getTotalExpense();

        return new ReportDto.IncomeExpenseSummary(
                totalIncome,
                totalExpense,
                totalIncome.subtract(totalExpense));
    }

    @Override
    public ReportDto.Overview getOverview(Long userId) {
        Long effectiveUserId = resolveAndValidateUserById(userId);
        return new ReportDto.Overview(
                getBankBalanceSummary(effectiveUserId),
                getWalletBalances(effectiveUserId),
                getMonthlyExpenses(effectiveUserId),
                getExpenseByCategory(effectiveUserId),
                getIncomeExpenseSummary(effectiveUserId)
        );
    }

            @Override
            public byte[] exportBankStatementCsv(Long userId, LocalDate fromDate, LocalDate toDate) {
            validateDateRange(fromDate, toDate);
            Long effectiveUserId = resolveAndValidateUserById(userId);

            List<TransactionDto.Response> transactions = transactionService.getTransactionsByUserId(
                effectiveUserId,
                new TransactionDto.HistoryFilter(fromDate, toDate, null, null, null, null, null, null, null, null)
            );

            BigDecimal totalCredits = transactions.stream()
                .filter(row -> "INCOME".equalsIgnoreCase(row.transactionType()))
                .map(TransactionDto.Response::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalDebits = transactions.stream()
                .filter(row -> !"INCOME".equalsIgnoreCase(row.transactionType()))
                .map(TransactionDto.Response::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<List<String>> rows = new ArrayList<>();
            rows.add(List.of("Date Range", fromDate + " to " + toDate, "", "", "", "", ""));
            rows.add(List.of("Total Credits", totalCredits.toPlainString(), "", "", "", "", ""));
            rows.add(List.of("Total Debits", totalDebits.toPlainString(), "", "", "", "", ""));
            rows.add(List.of("Net", totalCredits.subtract(totalDebits).toPlainString(), "", "", "", "", ""));
            rows.add(List.of("", "", "", "", "", "", ""));

            for (TransactionDto.Response transaction : transactions) {
                rows.add(List.of(
                    formatDateTime(transaction.createdAt()),
                    safeText(transaction.transactionType()),
                    transaction.amount() == null ? "0" : transaction.amount().toPlainString(),
                    safeText(transaction.status()),
                    safeText(transaction.category()),
                    safeText(transaction.paymentMethod()),
                    safeText(transaction.description())
                ));
            }

            return ExportDocumentBuilder.toCsv(
                List.of("Created At", "Type", "Amount", "Status", "Category", "Payment Method", "Description"),
                rows
            );
            }

            @Override
            public byte[] exportBankStatementPdf(Long userId, LocalDate fromDate, LocalDate toDate) {
            validateDateRange(fromDate, toDate);
            Long effectiveUserId = resolveAndValidateUserById(userId);
            List<TransactionDto.Response> transactions = transactionService.getTransactionsByUserId(
                effectiveUserId,
                new TransactionDto.HistoryFilter(fromDate, toDate, null, null, null, null, null, null, null, null)
            );

            BigDecimal totalCredits = transactions.stream()
                .filter(row -> "INCOME".equalsIgnoreCase(row.transactionType()))
                .map(TransactionDto.Response::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalDebits = transactions.stream()
                .filter(row -> !"INCOME".equalsIgnoreCase(row.transactionType()))
                .map(TransactionDto.Response::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<String> lines = new ArrayList<>();
            lines.add("Date Range: " + fromDate + " to " + toDate);
            lines.add("Total Credits: " + totalCredits.toPlainString());
            lines.add("Total Debits: " + totalDebits.toPlainString());
            lines.add("Net: " + totalCredits.subtract(totalDebits).toPlainString());
            lines.add(" ");
            lines.add("Transactions:");

            transactions.stream().limit(34).forEach(transaction -> lines.add(
                formatDateTime(transaction.createdAt())
                    + " | " + safeText(transaction.transactionType())
                    + " | " + safeText(transaction.status())
                    + " | " + (transaction.amount() == null ? "0" : transaction.amount().toPlainString())
                    + " | " + safeText(transaction.category())
            ));

            return ExportDocumentBuilder.toSimplePdf("Bank Statement", lines);
            }

            @Override
            public byte[] exportOverviewCsv(Long userId) {
            ReportDto.Overview overview = getOverview(userId);
            List<List<String>> rows = new ArrayList<>();

            rows.add(List.of("Summary", "", ""));
            rows.add(List.of("Total Income", safeAmount(overview.incomeExpenseSummary().totalIncome()), ""));
            rows.add(List.of("Total Expense", safeAmount(overview.incomeExpenseSummary().totalExpense()), ""));
            rows.add(List.of("Net Savings", safeAmount(overview.incomeExpenseSummary().netSavings()), ""));
            rows.add(List.of("", "", ""));

            rows.add(List.of("Bank Balances", "", ""));
            overview.bankBalances().forEach(balance -> rows.add(List.of(
                safeText(balance.bankName()),
                safeText(balance.accountNumber()),
                safeAmount(balance.balance())
            )));
            rows.add(List.of("", "", ""));

            rows.add(List.of("Wallet Balances", "", ""));
            overview.walletBalances().forEach(balance -> rows.add(List.of(
                safeText(balance.walletName()),
                safeText(balance.currency()),
                safeAmount(balance.balance())
            )));
            rows.add(List.of("", "", ""));

            rows.add(List.of("Expense by Category", "", ""));
            overview.expenseByCategory().forEach(expense -> rows.add(List.of(
                safeText(expense.category()),
                safeAmount(expense.totalExpense()),
                ""
            )));
            rows.add(List.of("", "", ""));

            rows.add(List.of("Monthly Expenses", "", ""));
            overview.monthlyExpenses().forEach(monthlyExpense -> rows.add(List.of(
                monthlyExpense.month() + " " + monthlyExpense.year(),
                safeAmount(monthlyExpense.totalExpense()),
                ""
            )));

            return ExportDocumentBuilder.toCsv(List.of("Section", "Value 1", "Value 2"), rows);
            }

            @Override
            public byte[] exportOverviewPdf(Long userId) {
            ReportDto.Overview overview = getOverview(userId);
            List<String> lines = new ArrayList<>();
            lines.add("Income: " + safeAmount(overview.incomeExpenseSummary().totalIncome()));
            lines.add("Expense: " + safeAmount(overview.incomeExpenseSummary().totalExpense()));
            lines.add("Net Savings: " + safeAmount(overview.incomeExpenseSummary().netSavings()));
            lines.add(" ");
            lines.add("Bank Balances:");
            overview.bankBalances().stream().limit(8).forEach(item -> lines.add(
                safeText(item.bankName()) + " | " + safeText(item.accountNumber()) + " | " + safeAmount(item.balance())
            ));
            lines.add(" ");
            lines.add("Wallet Balances:");
            overview.walletBalances().stream().limit(8).forEach(item -> lines.add(
                safeText(item.walletName()) + " | " + safeText(item.currency()) + " | " + safeAmount(item.balance())
            ));
            lines.add(" ");
            lines.add("Top Categories:");
            overview.expenseByCategory().stream().limit(8).forEach(item -> lines.add(
                safeText(item.category()) + " | " + safeAmount(item.totalExpense())
            ));

            return ExportDocumentBuilder.toSimplePdf("Overall Financial Report", lines);
            }

    private List<ReportDto.WalletBalance> getWalletBalances(Long userId) {
        return walletRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(wallet -> new ReportDto.WalletBalance(
                        wallet.getName(),
                        wallet.getBalance(),
                        wallet.getCurrency()))
                .toList();
    }

    private Long resolveAndValidateUserById(Long userId) {
        Long effectiveUserId = authContextService.resolveUserId(userId);
        validateUserExistsById(effectiveUserId);
        return effectiveUserId;
    }

    private String monthLabel(Integer monthValue) {
        if (monthValue == null || monthValue < 1 || monthValue > 12) {
            return "Unknown";
        }
        return Month.of(monthValue).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    private void validateUserExistsById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("Both fromDate and toDate are required");
        }
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate cannot be after toDate");
        }
    }

    private String safeText(String value) {
        return value == null ? "-" : value;
    }

    private String safeAmount(BigDecimal value) {
        return value == null ? "0" : value.toPlainString();
    }

    private String formatDateTime(java.time.LocalDateTime value) {
        if (value == null) {
            return "-";
        }
        return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
