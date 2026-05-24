package com.fintech.dbilleteras_virtuales.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NotificationRequest {
    @NotBlank
    private String asunto;

    @NotBlank
    private String message;

    @Email
    @NotBlank
    private String email;
}
