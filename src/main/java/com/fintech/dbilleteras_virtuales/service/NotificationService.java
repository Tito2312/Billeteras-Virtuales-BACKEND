package com.fintech.dbilleteras_virtuales.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class NotificationService {

    private final JavaMailSender mailSender = null;

    public void sendEmail(String message, String subject, String addressee) {
        SimpleMailMessage correo = new SimpleMailMessage();

        correo.setTo(addressee);
        correo.setSubject(subject);
        correo.setText(message);

        mailSender.send(correo);

    }

    public void notificationLowBalance(String email, String nombreBilletera, double saldo) {

        String subject = "   Billetera" + nombreBilletera + " con bajo saldo";
        String message = "Recarga mas saldo";

        sendEmail(message, subject, email);

    }

    public void notificationTransaction(String email, String tipo, double saldo) {

        String subject = " transferncia de tipo " + tipo;
        String message = "transferncia de tipo " + tipo + " con saldo de " + saldo + " realizada";

        sendEmail(message, subject, email);

    }

    public void rejetedTransaction(String email, String type) {

        String subject = "SE HA RECHAZADO SU TRANSACCION";
        String message = " La transaccion de tipo " + type + " a sido rechazada";

        sendEmail(message, subject, email);

    }

}
