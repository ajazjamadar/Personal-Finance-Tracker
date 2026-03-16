package com.qburst.training.personalfinancetracker.service.transfer;

import com.qburst.training.personalfinancetracker.dto.TransactionResponse;
import com.qburst.training.personalfinancetracker.dto.TransferRequest;

public interface TransferService {
    TransactionResponse bankToWallet(TransferRequest request);
    TransactionResponse walletToBank(TransferRequest request);
}
