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

        String subject = "Billetera " + nombreBilletera + " con bajo saldo";
        String message = "Recarga más saldo";

        return sendEmail(message, subject, email);

    }

    public Notification notificationTransaction(String email, String tipo, double saldo) {

        String subject = "Transferencia de tipo " + tipo;
        String message = "Transferencia de tipo " + tipo + " con saldo de " + saldo + " realizada";

        return sendEmail(message, subject, email);

    }

    public Notification rejetedTransaction(String email, String type) {

        String subject = "SE HA RECHAZADO SU TRANSACCION";
        String message = "La transaccion de tipo " + type + " ha sido rechazada";

        return sendEmail(message, subject, email);

    }

}
