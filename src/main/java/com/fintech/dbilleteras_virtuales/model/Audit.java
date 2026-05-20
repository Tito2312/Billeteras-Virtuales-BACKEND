package com.fintech.dbilleteras_virtuales.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

@Document(collection = "audits")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class Audit {
    @Id
    private String id;

    private String transactionId;

    private String userId;

    private String nivelRiesgo;

    private String descripcion;

    private LocalDateTime date;

}
