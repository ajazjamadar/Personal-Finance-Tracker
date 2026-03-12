package com.qburst.training.personalfinancetracker.exception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message){
        super(message);
    }
}
