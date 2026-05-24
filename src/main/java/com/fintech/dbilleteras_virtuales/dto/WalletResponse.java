package com.fintech.dbilleteras_virtuales.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class WalletResponse {
    String id;
    String userId;
    String name;
    String type;
    double balance;
    boolean isActive;
    LocalDate createdAt;
}
