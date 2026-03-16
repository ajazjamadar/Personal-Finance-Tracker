package com.qburst.training.personalfinancetracker.service.account;

import com.qburst.training.personalfinancetracker.dto.BankAccountDto;
import com.qburst.training.personalfinancetracker.entity.Bank;
import com.qburst.training.personalfinancetracker.entity.BankAccount;
import com.qburst.training.personalfinancetracker.entity.User;
import com.qburst.training.personalfinancetracker.exception.ResourceNotFoundException;
import com.qburst.training.personalfinancetracker.repository.BankAccountRepository;
import com.qburst.training.personalfinancetracker.repository.BankRepository;
import com.qburst.training.personalfinancetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final BankRepository bankRepository;

    public BankAccountServiceImpl(BankAccountRepository bankAccountRepository,
                                  UserRepository userRepository,
                                  BankRepository bankRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
        this.bankRepository = bankRepository;
    }

    @Override
    @Transactional
    public BankAccountDto.Response createBankAccount(BankAccountDto.Request request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.userId()));

        Bank bank = bankRepository.findByBankNameIgnoreCase(request.bankName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bank not found: " + request.bankName()));

        BankAccount account = BankAccount.builder()
                .user(user)
                .bank(bank)
                .accountNumber(request.accountNumber())
                .balance(request.initialBalance())
                .build();

        return toResponse(bankAccountRepository.save(account));
    }

    @Override
    public BankAccountDto.Response getBankAccountById(Long id) {
        return bankAccountRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bank account not found with id: " + id));
    }

    @Override
    public List<BankAccountDto.Response> getBankAccountsByUserId(Long userId) {
        return bankAccountRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<BankAccountDto.Response> searchAccounts(String bankName, String fullName) {
        if (bankName != null) {
            return bankAccountRepository.findByBankBankName(bankName)
                    .stream().map(this::toResponse).toList();
        }
        if (fullName != null) {
            return bankAccountRepository.findByUserFullName(fullName)
                    .stream().map(this::toResponse).toList();
        }
        return bankAccountRepository.findAll()
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void deleteBankAccount(Long id) {
        if (!bankAccountRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Bank account not found with id: " + id);
        }
        bankAccountRepository.deleteById(id);
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