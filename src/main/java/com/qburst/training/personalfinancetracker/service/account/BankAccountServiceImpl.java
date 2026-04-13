package com.qburst.training.personalfinancetracker.service.account;

import com.qburst.training.personalfinancetracker.dto.BankAccountDto;
import com.qburst.training.personalfinancetracker.entity.Bank;
import com.qburst.training.personalfinancetracker.entity.BankAccount;
import com.qburst.training.personalfinancetracker.entity.User;
import com.qburst.training.personalfinancetracker.exception.DuplicateResourceException;
import com.qburst.training.personalfinancetracker.exception.ResourceNotFoundException;
import com.qburst.training.personalfinancetracker.repository.BankAccountRepository;
import com.qburst.training.personalfinancetracker.repository.BankRepository;
import com.qburst.training.personalfinancetracker.repository.UserRepository;
import com.qburst.training.personalfinancetracker.security.AuthContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final BankRepository bankRepository;
        private final AuthContextService authContextService;

    @Override
    @Transactional
    public BankAccountDto.Response createBankAccount(BankAccountDto.Request request) {
                Long effectiveUserId = authContextService.resolveUserId(request.userId());
                log.info("Creating bank account for userId={} accountNumber={}", effectiveUserId, request.accountNumber());

        if (bankAccountRepository.existsByAccountNumber(request.accountNumber())) {
            throw new DuplicateResourceException(
                    "Bank account already exists with account number: " + request.accountNumber());
        }

        User user = userRepository.findById(effectiveUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + effectiveUserId));

        Bank bank = bankRepository.findByBankNameIgnoreCase(request.bankName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bank not found: " + request.bankName()));

        BankAccount account = BankAccount.builder()
                .user(user)
                .bank(bank)
                .accountNumber(request.accountNumber())
                .balance(request.initialBalance())
                .build();

        BankAccount saved = bankAccountRepository.save(account);
                log.info("Created bank account id={} for userId={}", saved.getId(), effectiveUserId);
        return toResponse(saved);
    }

    @Override
    public BankAccountDto.Response getBankAccountById(Long id) {
        log.debug("Fetching bank account by id={}", id);
        return bankAccountRepository.findById(id)
                .map(account -> {
                    authContextService.ensureCanAccessUser(account.getUser().getId());
                    return toResponse(account);
                })
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bank account not found with id: " + id));
    }

    @Override
    public List<BankAccountDto.Response> getBankAccountsByUserId(Long userId) {
                Long effectiveUserId = authContextService.resolveUserId(userId);
                log.debug("Fetching bank accounts for userId={}", effectiveUserId);
                return bankAccountRepository.findByUserId(effectiveUserId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteBankAccount(Long id) {
        log.warn("Deleting bank account id={}", id);
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bank account not found with id: " + id));
                authContextService.ensureCanAccessUser(account.getUser().getId());
        bankAccountRepository.delete(account);
        log.info("Deleted bank account id={}", id);
    }

    private BankAccountDto.Response toResponse(BankAccount account) {
        return new BankAccountDto.Response(
                account.getId(),
                account.getUser().getId(),
                account.getBank().getBankName(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCreatedAt());
    }
}
