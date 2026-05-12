package com.fintech.dbilleteras_virtuales.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.model.Notification;
import com.fintech.dbilleteras_virtuales.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.Value;

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

        String subject = " Transacción rechazada";
        String message = "Hola,Lamentamos informarte que tu " + type.toLowerCase()
                + " ha sido rechazada.Por favor verifica:- Que tienes saldo suficiente o que la billetera destino existe y está activa\n- Que los datos de la transacción son correctos\n\nSi el problema persiste, contacta a soporte.\n\nSaludos,\nEquipo de Billeteras Virtuales";

        return sendEmail(message, subject, email);

    }

    public Notification LevelUp(String email, String level, String beneficios) {
        String subject = "SE HA ACTUALIZADO TU NIVEL";
        String message = " Hola, has actualizado tu nivel " + level
                + ", sigue recargando para seguir subiendo y tener mas beneficios "
                + beneficios;
        return sendEmail(message, subject, email);

    }

    public Notification TransactionReverse(String email, int puntos) {

        String subject = "TRANSACCION REVERTIDA";
        String message = " Hola, tu transaccion fue revertida, se te descontaron" + puntos + " puntos ";
        return sendEmail(message, subject, email);

    }

    public Notification TransferNotification(String emailUser1, String name1, String emailUser2, String name2,
            double amoutn) {

        String subject1 = "TRANFERECNCIA EXITOSA";
        String message1 = " Hola, ya hemos enviado en dinero a" + name2;

        String subject2 = "FELICIDADES, TE HAN ENVIADO" + amoutn;
        String message2 = " Hola, " + name1 + " te ha enviado " + amoutn + "";

        sendEmail(message2, subject2, emailUser2);

        return sendEmail(message1, subject1, emailUser1);

    }

    public Notification sendVerificationEmail(String email, String token) {
        String link = "http://localhost:8080/api/auth/verify-email?token=" + token;

        String subject = "Verifica tu cuenta - Billeteras Virtuales";
        String message = "Hola,\n\n" +
                "Gracias por registrarte. Haz click en el siguiente enlace para activar tu cuenta:\n\n" +
                link + "\n\n" +
                "Este enlace es de un solo uso.\n\n" +
                "Si no creaste esta cuenta, ignora este mensaje.";

        return sendEmail(message, subject, email);
    }

}
