package com.fintech.dbilleteras_virtuales.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fintech.dbilleteras_virtuales.dto.AuthResponse;
import com.fintech.dbilleteras_virtuales.dto.ChangePasswordLoggedRequest;
import com.fintech.dbilleteras_virtuales.dto.ChangePasswordRequest;
import com.fintech.dbilleteras_virtuales.dto.LoginRequest;
import com.fintech.dbilleteras_virtuales.dto.RegisterRequest;
import com.fintech.dbilleteras_virtuales.dto.ResetPasswordRequest;
import com.fintech.dbilleteras_virtuales.model.User;
import com.fintech.dbilleteras_virtuales.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok("Cuenta activada exitosamente. Ya puedes iniciar sesión.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok("Se ha enviado un enlace de recuperación a tu correo electrónico");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ChangePasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Contraseña actualizada exitosamente");
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody @Valid ChangePasswordLoggedRequest request,
                                                 @AuthenticationPrincipal User user) {
        authService.changePassword(user.getId(), request.getCurrentPassword(),
                                   request.getNewPassword(), request.getConfirmPassword());
        return ResponseEntity.ok("Contraseña cambiada exitosamente");
    }
}