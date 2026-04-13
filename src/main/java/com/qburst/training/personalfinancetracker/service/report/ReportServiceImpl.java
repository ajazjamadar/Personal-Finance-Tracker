package com.qburst.training.personalfinancetracker.service.report;

import com.qburst.training.personalfinancetracker.dto.BankAccountDto;
import com.qburst.training.personalfinancetracker.dto.ReportDto;
import com.qburst.training.personalfinancetracker.entity.Transaction;
import com.qburst.training.personalfinancetracker.entity.Transaction.TransactionType;
import com.qburst.training.personalfinancetracker.exception.ResourceNotFoundException;
import com.qburst.training.personalfinancetracker.repository.BankAccountRepository;
import com.qburst.training.personalfinancetracker.repository.TransactionRepository;
import com.qburst.training.personalfinancetracker.repository.UserRepository;
import com.qburst.training.personalfinancetracker.security.AuthContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {
    private static final List<TransactionType> EXPENSE_TYPES = List.of(
            TransactionType.EXPENSE,
            TransactionType.ATM_WITHDRAWAL,
            TransactionType.TRANSFER
    );
    private static final Transaction.TransactionStatus INCLUDED_STATUS = Transaction.TransactionStatus.SUCCESS;

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AuthContextService authContextService;

    public ReportServiceImpl(BankAccountRepository bankAccountRepository,
                             TransactionRepository transactionRepository,
                             UserRepository userRepository,
                             AuthContextService authContextService) {
        this.bankAccountRepository = bankAccountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.authContextService = authContextService;
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
                getMonthlyExpenses(effectiveUserId),
                getExpenseByCategory(effectiveUserId),
                getIncomeExpenseSummary(effectiveUserId)
        );
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
}
