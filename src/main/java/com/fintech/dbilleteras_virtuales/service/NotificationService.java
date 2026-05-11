package com.fintech.dbilleteras_virtuales.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.model.Notification;
import com.fintech.dbilleteras_virtuales.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;

    public Notification sendEmail(String message, String subject, String addressee) {
        SimpleMailMessage correo = new SimpleMailMessage();

        correo.setTo(addressee);
        correo.setSubject(subject);
        correo.setText(message);

        mailSender.send(correo);

        Notification notification = Notification.builder()
                .asunto(subject)
                .message(message)
                .email(addressee)
                .build();

        return notificationRepository.save(notification);
    }

    public Notification notificationLowBalance(String email, String nombreBilletera, double saldo) {

        String subject = "⚠️ Alerta: Saldo bajo en tu billetera";
        String message = "Hola,\n\nTu billetera \"" + nombreBilletera + "\" tiene un saldo actual de $"
                + String.format("%.2f", saldo)
                + ".\n\nTe recomendamos recargar saldo para evitar inconvenientes con tus transacciones.\n\nSaludos,\nEquipo de Billeteras Virtuales";

        return sendEmail(message, subject, email);

    }

    public Notification notificationTransaction(String email, String tipo, double saldo) {

        String subject = "✅ Confirmación: " + tipo + " exitosa";
        String message = "Hola,\n\nTu " + tipo.toLowerCase() + " por un monto de $" + String.format("%.2f", saldo)
                + " ha sido procesada exitosamente.\n\nPuedes verificar el detalle de esta transacción en tu historial.\n\nSaludos,\nEquipo de Billeteras Virtuales";

        return sendEmail(message, subject, email);

    }

    public Notification rejetedTransaction(String email, String type) {

        String subject = "❌ Transacción rechazada";
        String message = "Hola,\n\nLamentamos informarte que tu " + type.toLowerCase()
                + " ha sido rechazada.\n\nPor favor verifica:\n- Que tienes saldo suficiente\n- Que la billetera destino existe y está activa\n- Que los datos de la transacción son correctos\n\nSi el problema persiste, contacta a soporte.\n\nSaludos,\nEquipo de Billeteras Virtuales";

        return sendEmail(message, subject, email);

    }

}
