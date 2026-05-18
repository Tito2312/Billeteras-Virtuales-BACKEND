package com.fintech.dbilleteras_virtuales.dto;

import java.time.LocalDateTime;

import com.fintech.dbilleteras_virtuales.model.TransactionType;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ScheduledOperationRequestDto{
    
    @NotBlank(message = "El userId es obligatorio")
    private String userId;

    private String sourceWalletId;

    private String targetWalletId;

    @NotNull(message = "El tipo de operación es obligatorio")
    private TransactionType type;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a 0")
    private Double amount; 

    @NotNull(message = "La fecha es obligatoria")
    private LocalDateTime scheduledDate;
}
