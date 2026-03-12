package com.qburst.training.personalfinancetracker.service;

import com.qburst.training.personalfinancetracker.dto.CreateBankAccountRequest;
import com.qburst.training.personalfinancetracker.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class BankAccountServiceImpl implements BankAccountService{

    @Override
    public String createAccount(CreateBankAccountRequest request){
        return "Bank account created successfully for user: " + request.getUserId();
    }

    @Override
    public String getAccountById(Long id){
        if (id <= 0){
            throw new ResourceNotFoundException("Bank account not found with id: " + id);
        }
        return "Bank account found with id: " + id;
    }

    @Override
    public String getAccountByUserId(Long userId){
        if (userId <= 0){
            throw new ResourceNotFoundException("Bank account not found for user id: " + userId);
        }
        return "Account for User:" + userId;
    }

    @Override
    public String deleteAccount(Long id){
        if (id <= 0){
            throw new ResourceNotFoundException("Bank account not found with id:" + id);
        }
        return "Bank account deleted with id: " + id;
    }
}
