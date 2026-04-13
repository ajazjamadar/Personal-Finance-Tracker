package com.qburst.training.personalfinancetracker.service.account;

import com.qburst.training.personalfinancetracker.dto.BankAccountDto;
import java.util.List;

public interface BankAccountService {
    BankAccountDto.Response createBankAccount(BankAccountDto.Request request);
    BankAccountDto.Response getBankAccountById(Long id);
    List<BankAccountDto.Response> getBankAccountsByUserId(Long userId);
    void deleteBankAccount(Long id);
}
