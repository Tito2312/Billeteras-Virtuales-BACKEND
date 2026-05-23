package com.fintech.dbilleteras_virtuales.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "Benefits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Benefit {
    
    @Id
    private String id;
    private String code;
    private String name;
    private String description;
    private int pointsCost;
    private boolean active = true;
}
