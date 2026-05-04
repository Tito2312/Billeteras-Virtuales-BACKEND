package com.fintech.dbilleteras_virtuales.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

@Document(collection = "wallets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    
    @Id
    private String id;

    private String userId;

    private String name;
    private String type;
    private double balance;
    private boolean isActive = true;
    private LocalDate createdAt;
}
