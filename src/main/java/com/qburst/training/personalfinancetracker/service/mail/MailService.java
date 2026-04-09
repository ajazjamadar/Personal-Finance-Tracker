package com.qburst.training.personalfinancetracker.service.mail;

public interface MailService {
    void sendLoginOtp(String recipientEmail, String recipientName, String otpCode);
}
