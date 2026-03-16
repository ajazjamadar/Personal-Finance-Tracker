package com.qburst.training.personalfinancetracker.service;

import com.qburst.training.personalfinancetracker.dto.WalletDto;
import java.util.List;

public interface WalletService {
    WalletDto.Response createWallet(WalletDto.Request request);
    WalletDto.Response getWalletById(Long id);
    List<WalletDto.Response> getWalletsByUserId(Long userId);
    void deleteWallet(Long id);
}