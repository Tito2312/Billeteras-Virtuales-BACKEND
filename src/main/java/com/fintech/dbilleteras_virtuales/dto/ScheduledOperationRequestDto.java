package com.fintech.dbilleteras_virtuales.dto;

import java.time.LocalDateTime;

import com.fintech.dbilleteras_virtuales.model.TransactionType;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ScheduledOperationRequestDto{
    
    @NotBlank
    private String userId;

    private String sourceWalletId;

    @NotBlank
    private String targetWalletId;

    @NotBlank
    private TransactionType type;

    @Positive
    @NotBlank
    private double amount;

    @NotNull
    private LocalDateTime scheduledDate;
}
