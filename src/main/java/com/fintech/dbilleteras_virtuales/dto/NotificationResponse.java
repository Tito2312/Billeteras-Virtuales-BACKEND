package com.fintech.dbilleteras_virtuales.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class NotificationResponse {

    private String asunto;
    private String message;
    private String email;
    private LocalDate registrationDate = LocalDate.now();

}
