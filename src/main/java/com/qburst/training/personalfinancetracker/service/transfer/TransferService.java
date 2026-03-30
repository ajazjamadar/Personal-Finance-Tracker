package com.qburst.training.personalfinancetracker.service.transfer;

import com.qburst.training.personalfinancetracker.dto.TransferDto;

public interface TransferService {
    TransferDto.Response bankToWallet(TransferDto.Request request);
    TransferDto.Response walletToBank(TransferDto.Request request);
}
