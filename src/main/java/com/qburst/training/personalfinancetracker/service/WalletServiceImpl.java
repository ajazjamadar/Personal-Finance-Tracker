package com.qburst.training.personalfinancetracker.service;

import com.qburst.training.personalfinancetracker.dto.CreateWalletRequest;
import com.qburst.training.personalfinancetracker.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class WalletServiceImpl implements WalletService {

    @Override
    public String createWallet(CreateWalletRequest request){
        return "Wallet created for user: " + request.getUserId();
    }

    @Override
    public String getWalletById(Long id){
        if (id<=0){
            throw new ResourceNotFoundException("Wallet not found with id:" + id);
        }
        return "Wallet found with id:" + id;
    }

    public String getWalletByUserId(Long userId){
        return "Wallet for user:" + userId;
    }

    @Override
    public String getWalletsByUser(Long userId){
        return "Wallets for user:" + userId;
    }
}
