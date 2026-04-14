package com.qburst.training.personalfinancetracker.service.wallet;

import com.qburst.training.personalfinancetracker.dto.WalletDto;

import java.util.List;

public interface WalletService {
    WalletDto.Response createWallet(WalletDto.Request request);
    List<WalletDto.Response> listWalletsForCurrentUser();
    WalletDto.Response getWalletById(Long walletId);
    void deleteWallet(Long walletId);
}
