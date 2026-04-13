package com.qburst.training.personalfinancetracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_bank_id")
    private BankAccount sourceBankAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dest_bank_id")
    private BankAccount destBankAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_type", length = 20)
    private TransferType transferType;

    @Column(name = "self_transfer")
    private Boolean selfTransfer;

    @Column(name = "destination_value", length = 150)
    private String destinationValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "category_name", length = 100)
    private String categoryName;

    @Column(name = "receiver_name", length = 150)
    private String receiverName;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = TransactionStatus.SUCCESS;
        }
    }

    public enum TransactionType {
        INCOME,
        EXPENSE,
        ATM_WITHDRAWAL,
        TRANSFER
    }

    public enum TransferType {
        ACCOUNT,
        MOBILE,
        UPI
    }

    public enum TransactionStatus {
        INITIATED,
        SENT,
        SUCCESS,
        FAILED,
        PENDING
    }

    public enum PaymentMethod {
        UPI,
        CARD,
        NET_BANKING,
        WALLET
    }
}
