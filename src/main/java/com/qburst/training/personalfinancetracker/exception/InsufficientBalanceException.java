package com.qburst.training.personalfinancetracker.exception;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message){
        super(message);
    }
}
