package com.fintech.dbilleteras_virtuales.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.model.Notification;
import com.fintech.dbilleteras_virtuales.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;

    public Notification sendEmail(String message, String subject, String addressee) {
        logger.info("📧 Intentando enviar email a: {}", addressee);
        logger.info("📧 Asunto: {}", subject);
        
        try {
            SimpleMailMessage correo = new SimpleMailMessage();
            correo.setTo(addressee);
            correo.setSubject(subject);
            correo.setText(message);
            correo.setFrom("walletteachdata@gmail.com"); // Remitente explícito

            mailSender.send(correo);
            logger.info("✅ Email enviado exitosamente a: {}", addressee);

            Notification notification = Notification.builder()
                    .asunto(subject)
                    .message(message)
                    .email(addressee)
                    .build();

            return notificationRepository.save(notification);
            
        } catch (Exception e) {
            logger.error("❌ Error al enviar email a {}: {}", addressee, e.getMessage());
            logger.error("❌ Detalle del error: ", e);
            throw new RuntimeException("Error al enviar email: " + e.getMessage(), e);
        }
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

    public Notification LevelUp(String email, String level, String beneficios) {
        String subject = "🎉 ¡HAS ACTUALIZADO TU NIVEL!";
        String message = "Hola,\n\n¡Felicidades! Has alcanzado el nivel " + level
                + ".\n\nAhora disfrutas de los siguientes beneficios:\n" + beneficios
                + "\n\nSigue usando FinWallet para seguir subiendo de nivel.\n\nSaludos,\nEquipo de Billeteras Virtuales";
        return sendEmail(message, subject, email);
    }

    public Notification TransactionReverse(String email, int puntos) {
        String subject = "🔄 TRANSACCIÓN REVERTIDA";
        String message = "Hola,\n\nTu transacción fue revertida exitosamente.\n\nSe te descontaron " + puntos + " puntos.\n\nSaludos,\nEquipo de Billeteras Virtuales";
        return sendEmail(message, subject, email);
    }

    public Notification TransferNotification(String emailUser1, String name1, String emailUser2, String name2, double amount) {
        String subject1 = "✅ TRANSFERENCIA EXITOSA";
        String message1 = "Hola " + name1 + ",\n\nHas transferido $" + String.format("%.2f", amount) + " a " + name2 + " exitosamente.\n\nSaludos,\nEquipo de Billeteras Virtuales";

        String subject2 = "💰 HAS RECIBIDO UNA TRANSFERENCIA";
        String message2 = "Hola " + name2 + ",\n\nHas recibido $" + String.format("%.2f", amount) + " de " + name1 + ".\n\nSaludos,\nEquipo de Billeteras Virtuales";

        sendEmail(message2, subject2, emailUser2);
        return sendEmail(message1, subject1, emailUser1);
    }

    public Notification sendVerificationEmail(String email, String token) {
        String link = "http://localhost:3000/verify-email?token=" + token;

        String subject = "🔐 Verifica tu cuenta - FinWallet";
        String message = "Hola,\n\n" +
                "Gracias por registrarte en FinWallet.\n\n" +
                "Para activar tu cuenta, haz clic en el siguiente enlace:\n\n" +
                link + "\n\n" +
                "Este enlace es válido por tiempo limitado y es de un solo uso.\n\n" +
                "Si no creaste esta cuenta, ignora este mensaje.\n\n" +
                "Saludos,\nEquipo de FinWallet";

        logger.info("📧 Enviando email de verificación a: {} con token: {}", email, token);
        return sendEmail(message, subject, email);
    }
}