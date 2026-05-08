package com.fintech.dbilleteras_virtuales.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

@Document(collection = "scheduledOperations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledOperation {
    
    @Id
    private String id;

    private String userId;
    private String receiverUserId;
    private String sourceWalletId;
    private String targetWalletId;
    private TransactionType type;
    private double amount;
    private LocalDateTime scheduledDate;
    private boolean executed;
    private LocalDateTime createdAt;

}
