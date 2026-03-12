package com.qburst.training.personalfinancetracker.service;

import com.qburst.training.personalfinancetracker.dto.CreateBankAccountRequest;

public interface BankAccountService {
    String createAccount(CreateBankAccountRequest request);
    String getAccountById(Long id);
    String getAccountByUserId(Long userId);
    String deleteAccount(Long id);
}
