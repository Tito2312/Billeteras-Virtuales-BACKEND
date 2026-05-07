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
public class Transaction {

    @Id
    private String id;

    private String userId;
    private String sourceWallet;
    private String targetWallet;
    private TransactionType type;
    private double amount;
    private TransactionStatus status;
    private int points;
    private boolean reversed;
    private LocalDateTime createdAt;

}