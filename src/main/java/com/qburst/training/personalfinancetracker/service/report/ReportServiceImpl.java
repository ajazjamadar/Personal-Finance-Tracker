package com.qburst.training.personalfinancetracker.service.report;

import com.qburst.training.personalfinancetracker.dto.BankAccountDto;
import com.qburst.training.personalfinancetracker.dto.ReportDto;
import com.qburst.training.personalfinancetracker.entity.Transaction.TransactionType;
import com.qburst.training.personalfinancetracker.exception.ResourceNotFoundException;
import com.qburst.training.personalfinancetracker.repository.BankAccountRepository;
import com.qburst.training.personalfinancetracker.repository.TransactionRepository;
import com.qburst.training.personalfinancetracker.repository.UserRepository;
import com.qburst.training.personalfinancetracker.security.AuthContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

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
                Long effectiveUserId = authContextService.resolveUserId(userId);
                validateUser(effectiveUserId);
                return bankAccountRepository.findByUserId(effectiveUserId)
                .stream()
                .map(a -> new BankAccountDto.BalanceSummary(
                        a.getBank().getBankName(),
                        a.getAccountNumber(),
                        a.getBalance()))
                .toList();
    }

    @Override
    public List<ReportDto.MonthlyExpense> getMonthlyExpenses(Long userId) {
                Long effectiveUserId = authContextService.resolveUserId(userId);
                validateUser(effectiveUserId);
                return transactionRepository.findByUserIdOrderByCreatedAtDesc(effectiveUserId)
                .stream()
                .filter(t -> t.getTransactionType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        t -> t.getCreatedAt().getMonth()
                                .getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                                + "-" + t.getCreatedAt().getYear(),
                        Collectors.reducing(BigDecimal.ZERO,
                                t -> t.getAmount(), BigDecimal::add)))
                .entrySet().stream()
                .map(e -> {
                    String[] parts = e.getKey().split("-");
                    return new ReportDto.MonthlyExpense(
                            parts[0], Integer.parseInt(parts[1]), e.getValue());
                })
                .toList();
    }

    @Override
    public List<ReportDto.CategoryExpense> getExpenseByCategory(Long userId) {
                Long effectiveUserId = authContextService.resolveUserId(userId);
                validateUser(effectiveUserId);
                return transactionRepository.findByUserIdOrderByCreatedAtDesc(effectiveUserId)
                .stream()
                .filter(t -> t.getTransactionType() == TransactionType.EXPENSE
                        && t.getDescription() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getDescription(),
                        Collectors.reducing(BigDecimal.ZERO,
                                t -> t.getAmount(), BigDecimal::add)))
                .entrySet().stream()
                .map(e -> new ReportDto.CategoryExpense(e.getKey(), e.getValue()))
                .toList();
    }

    @Override
    public ReportDto.IncomeExpenseSummary getIncomeExpenseSummary(Long userId) {
                Long effectiveUserId = authContextService.resolveUserId(userId);
                validateUser(effectiveUserId);
                var transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(effectiveUserId);

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.INCOME)
                .map(t -> t.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.EXPENSE)
                .map(t -> t.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ReportDto.IncomeExpenseSummary(
                totalIncome,
                totalExpense,
                totalIncome.subtract(totalExpense));
    }

    private void validateUser(Long userId) throws ResourceNotFoundException {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }
}