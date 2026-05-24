package com.fintech.dbilleteras_virtuales.service;

import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class AsyncEmailService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncEmailService.class);
    private static final String RESEND_URL = "https://api.resend.com/emails";

    @Value("${resend.api.key}")
    private String resendApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void send(SimpleMailMessage correo) {
        String destinatario = Arrays.toString(correo.getTo());
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + resendApiKey);

            Map<String, Object> body = Map.of(
                    "from", "FinWallet <noreply@walletechuq.online>",
                    "to", correo.getTo(),
                    "subject", correo.getSubject(),
                    "text", correo.getText());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            String response = restTemplate.postForObject(RESEND_URL, request, String.class);
            logger.info("✅ Email enviado a: {} | Resend: {}", destinatario, response);

        } catch (HttpClientErrorException e) {
            logger.error("❌ Resend rechazó el email a: {} | Status: {} | Error: {}",
                    destinatario, e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("❌ Error enviando email a: {} | {}", destinatario, e.getMessage());
        }
    }
}
