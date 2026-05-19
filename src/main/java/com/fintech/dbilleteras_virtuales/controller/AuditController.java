package com.fintech.dbilleteras_virtuales.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fintech.dbilleteras_virtuales.dto.AuthResponse;
import com.fintech.dbilleteras_virtuales.dto.LoginRequest;
import com.fintech.dbilleteras_virtuales.dto.RegisterRequest;
import com.fintech.dbilleteras_virtuales.service.AuditService;
import com.fintech.dbilleteras_virtuales.service.AuthService;
import com.fintech.dbilleteras_virtuales.model.User;
import com.fintech.dbilleteras_virtuales.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @PostMapping("/HistoryUserAudit")
    public ResponseEntity HistoryUserAudit(@RequestBody String userId) {

        return ResponseEntity.ok(auditService.historyUserAudit(userId));
    }

}
