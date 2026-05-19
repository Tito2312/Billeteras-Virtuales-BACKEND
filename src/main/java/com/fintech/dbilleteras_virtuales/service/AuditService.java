package com.fintech.dbilleteras_virtuales.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.model.Audit;
import com.fintech.dbilleteras_virtuales.service.TransactionService;
import com.fintech.dbilleteras_virtuales.repository.AuditRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class AuditService {

    private final TransactionService transactionService;
    private final AuditRepository auditRepository;

    private String transaccionId;
    private String userId;
    private String auditId;
    private String nivelRiesgo;
    private String descripcion;

    public List<Audit> getAuditsByUser(String userId) {
        return auditRepository.findByUserId(userId);
    }

    public Audit registrarEvento(String userId, String transactionId, String nivelRiesgo, String descripcion) {
        Audit audit = Audit.builder()
                .userId(userId)
                .transactionId(transactionId)
                .nivelRiesgo(nivelRiesgo)
                .descripcion(descripcion)
                .fecha(LocalDateTime.now())
                .build();
        return auditRepository.save(audit);
    }
}
