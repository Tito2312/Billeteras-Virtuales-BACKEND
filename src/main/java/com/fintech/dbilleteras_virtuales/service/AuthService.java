package com.fintech.dbilleteras_virtuales.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.dto.AuthResponse;
import com.fintech.dbilleteras_virtuales.dto.LoginRequest;
import com.fintech.dbilleteras_virtuales.dto.RegisterRequest;
import com.fintech.dbilleteras_virtuales.model.Role;
import com.fintech.dbilleteras_virtuales.model.User;
import com.fintech.dbilleteras_virtuales.model.Wallet;
import com.fintech.dbilleteras_virtuales.repository.UserRepository;
import com.fintech.dbilleteras_virtuales.repository.WalletRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final WalletRepository walletRepository;
    private final WalletService walletService;

    public AuthResponse register(RegisterRequest request) {
        logger.info("📝 Intentando registrar usuario: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("❌ Email ya registrado: {}", request.getEmail());
            throw new RuntimeException("El correo electronico ya esta en uso");
        }

        String verificationToken = UUID.randomUUID().toString();
        logger.info("🔑 Token de verificación generado: {}", verificationToken);

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhone())
                .documentNumber(request.getDocumentNumber())
                .isActive(false)
                .verificationToken(verificationToken)
                .role(Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(user);
        logger.info("✅ Usuario guardado en BD: {} con ID: {}", savedUser.getEmail(), savedUser.getId());

        Wallet defaultWallet = Wallet.builder()
            .userId(savedUser.getId())
            .name("Mi billetera")
            .type("DAILY")
            .balance(0)
            .isActive(true)
            .createdAt(LocalDate.now())
            .transferKey(walletService.generateTransferKey("Mi billetera", savedUser.getDocumentNumber()))
            .build();

        walletRepository.save(defaultWallet);

        try {
            notificationService.sendVerificationEmail(savedUser.getEmail(), verificationToken);
            logger.info("📧 Email de verificación enviado a: {}", savedUser.getEmail());
        } catch (Exception e) {
            logger.error("❌ Error al enviar email de verificación a {}: {}", savedUser.getEmail(), e.getMessage());

            logger.warn("⚠️ El usuario se registró pero el email no se pudo enviar. Verificar configuración SMTP.");
        }

        String token = jwtService.generateToken(savedUser);
        logger.info("🎫 Token JWT generado para: {}", savedUser.getEmail());

        return new AuthResponse(token, savedUser.getId(), savedUser.getName(), savedUser.getLevel(),
                user.getRole().toString());
    }

    public AuthResponse login(LoginRequest request) {
        logger.info("🔐 Intento de login: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.warn("❌ Usuario no encontrado: {}", request.getEmail());
                    return new RuntimeException("Credenciales inválidas");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("❌ Contraseña incorrecta para: {}", request.getEmail());
            throw new RuntimeException("Credenciales inválidas");
        }

        if (!user.isActive()) {
            logger.warn("⚠️ Cuenta no verificada: {}", request.getEmail());
            throw new RuntimeException("Cuenta no verificada. Por favor, revisa tu correo para activar tu cuenta.");
        }

        String token = jwtService.generateToken(user);
        logger.info("✅ Login exitoso: {}", user.getEmail());

        return new AuthResponse(token, user.getId(), user.getName(), user.getLevel(), user.getRole().toString());
    }

    public void verifyEmail(String token) {
        logger.info("🔍 Verificando email con token: {}", token);

        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> {
                    logger.warn("❌ Token de verificación inválido: {}", token);
                    return new RuntimeException("Token de verificación inválido o ya usado");
                });

        if (user.isActive()) {
            logger.warn("⚠️ Cuenta ya estaba verificada: {}", user.getEmail());
            throw new RuntimeException("La cuenta ya fue verificada anteriormente");
        }

        user.setActive(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        logger.info("✅ Cuenta verificada exitosamente: {}", user.getEmail());
    }

public void forgotPassword(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("No existe un usuario con ese correo electrónico"));

    String token = UUID.randomUUID().toString();
    user.setResetPasswordToken(token);
    user.setResetPasswordExpiry(LocalDateTime.now().plusHours(1));
    userRepository.save(user);

    notificationService.sendResetPasswordEmail(user.getEmail(), token);
}

public void resetPassword(String token, String newPassword) {
    User user = userRepository.findByResetPasswordToken(token)
        .orElseThrow(() -> new RuntimeException("Token inválido o expirado"));

    if (user.getResetPasswordExpiry().isBefore(LocalDateTime.now())) {
        throw new RuntimeException("El enlace ha expirado. Solicita un nuevo restablecimiento.");
    }

    user.setPassword(passwordEncoder.encode(newPassword));
    user.setResetPasswordToken(null);
    user.setResetPasswordExpiry(null);
    userRepository.save(user);
}

public void changePassword(String userId, String currentPassword, String newPassword, String confirmPassword) {
    if (!newPassword.equals(confirmPassword)) {
        throw new RuntimeException("Las contraseñas nuevas no coinciden");
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
        throw new RuntimeException("Contraseña actual incorrecta");
    }

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
}
}
