package com.qburst.training.personalfinancetracker.service.wallet;

import com.qburst.training.personalfinancetracker.dto.WalletTransactionDto;
import org.springframework.data.domain.Page;

public interface WalletTransactionService {
    WalletTransactionDto.Response deposit(WalletTransactionDto.DepositWithdrawRequest request);
    WalletTransactionDto.Response withdraw(WalletTransactionDto.DepositWithdrawRequest request);
    WalletTransactionDto.TransferResponse transfer(WalletTransactionDto.TransferRequest request);
    Page<WalletTransactionDto.Response> listWalletTransactions(Long walletId, int page, int size);
}
