package com.fintech.dbilleteras_virtuales.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

@Document(collection = "audits")
@Data
@Builder
@AllArgsConstructor

public class Audit {

}
