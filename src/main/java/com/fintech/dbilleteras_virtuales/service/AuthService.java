package com.fintech.dbilleteras_virtuales.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.dto.AuthResponse;
import com.fintech.dbilleteras_virtuales.dto.LoginRequest;
import com.fintech.dbilleteras_virtuales.dto.RegisterRequest;
import com.fintech.dbilleteras_virtuales.model.User;
import com.fintech.dbilleteras_virtuales.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public AuthResponse register(RegisterRequest request) {
        logger.info("📝 Intentando registrar usuario: {}", request.getEmail());
        
        // Verificar si el email ya existe
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("❌ Email ya registrado: {}", request.getEmail());
            throw new RuntimeException("El correo electronico ya esta en uso");
        }

        // Generar token de verificación
        String verificationToken = UUID.randomUUID().toString();
        logger.info("🔑 Token de verificación generado: {}", verificationToken);

        // Crear usuario (inactivo hasta verificar email)
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhone())
                .documentNumber(request.getDocumentNumber())
                .isActive(false)
                .verificationToken(verificationToken)
                .role(Role.USER)
                .build();

        // Guardar usuario en la base de datos
        User savedUser = userRepository.save(user);
        logger.info("✅ Usuario guardado en BD: {} con ID: {}", savedUser.getEmail(), savedUser.getId());

        // Intentar enviar email de verificación
        try {
            notificationService.sendVerificationEmail(savedUser.getEmail(), verificationToken);
            logger.info("📧 Email de verificación enviado a: {}", savedUser.getEmail());
        } catch (Exception e) {
            logger.error("❌ Error al enviar email de verificación a {}: {}", savedUser.getEmail(), e.getMessage());
            // No relanzamos la excepción para que el registro no falle
            logger.warn("⚠️ El usuario se registró pero el email no se pudo enviar. Verificar configuración SMTP.");
        }

        // Generar token JWT
        String token = jwtService.generateToken(savedUser);
        logger.info("🎫 Token JWT generado para: {}", savedUser.getEmail());
        
        return new AuthResponse(token, savedUser.getId(), savedUser.getName(), savedUser.getLevel());
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
        
        return new AuthResponse(token, user.getId(), user.getName(), user.getLevel());
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
}