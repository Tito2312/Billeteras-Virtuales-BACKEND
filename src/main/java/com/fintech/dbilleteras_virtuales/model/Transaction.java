package com.fintech.dbilleteras_virtuales.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Document(collection = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction implements Comparable<Transaction> {

    @Id
    private String id;
    private String userId;
    private String receiverUserId;
    private String sourceWallet;
    private String targetWallet;
    private TransactionType type;
    private double amount;
    private double originalAmount;     
    private double commissionAmount;    
    private TransactionStatus status;
    private int points;
    private boolean reversed;
    private LocalDateTime createdAt;
    private LocalDateTime reversedAt;

    @Override
    public int compareTo(Transaction other) {
        return Double.compare(other.amount, this.amount);
    }
}
