package com.fintech.dbilleteras_virtuales.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "redeemed_benefits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedeemedBenefit {

    @Id
    private String id;
    private String userId;
    private String benefitId;
    private String benefitName;
    private int pointsSpent;
    private String status; 
    private LocalDateTime redeemedAt;
}
