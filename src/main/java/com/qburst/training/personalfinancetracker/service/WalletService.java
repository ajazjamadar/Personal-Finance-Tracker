package com.qburst.training.personalfinancetracker.service;

import com.qburst.training.personalfinancetracker.dto.CreateWalletRequest;

public interface WalletService {
    String createWallet(CreateWalletRequest request);
    String getWalletById(Long id);
    String getWalletsByUser(Long userId);
}