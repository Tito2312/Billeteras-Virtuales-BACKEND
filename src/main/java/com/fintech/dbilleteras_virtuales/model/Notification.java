package com.fintech.dbilleteras_virtuales.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    private String asunto;
    private String message;
    private String email;
    private LocalDate registrationDate = LocalDate.now();

}
