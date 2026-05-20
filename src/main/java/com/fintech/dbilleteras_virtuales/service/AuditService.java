package com.fintech.dbilleteras_virtuales.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.model.Audit;
import com.fintech.dbilleteras_virtuales.repository.AuditRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class AuditService {

    private final AuditRepository auditRepository;

    public List<Audit> getAuditsByUser(String userId) {
        return auditRepository.findByUserId(userId);
    }

    public Audit registrarEvento(String userId, String transactionId, String nivelRiesgo, String descripcion) {
        Audit audit = Audit.builder()
                .userId(userId)
                .transactionId(transactionId)
                .nivelRiesgo(nivelRiesgo)
                .descripcion(descripcion)
                .date(LocalDateTime.now())
                .build();
        return auditRepository.save(audit);
    }

    public List<Audit> historyUserAudit(String userId) {

        List<Audit> auditsUser = auditRepository.findByUserId(userId);

        return auditsUser;

    }

    public List<Audit> historyAudits() {
        List<Audit> audits = auditRepository.findAll();

        return audits;
    }

    public List<Audit> AuditsDateRange(String firs, String second) {

        LocalDateTime firtHour = LocalDateTime.parse(firs);
        LocalDateTime secondHour = LocalDateTime.parse(second);

        List<Audit> audits = auditRepository.findByDateBetween(firtHour, secondHour);

        return audits;

    }

    public List<Audit> AuditsToday() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(23, 59, 59);
        return auditRepository.findByDateBetween(start, end);
    }

    public LinkedList<Audit> AuditsLowLevel(String nivelRiesgo) {

        List<Audit> audits = auditRepository.findByNivelRiesgo(nivelRiesgo);

        LinkedList<Audit> auditsLow = new LinkedList<>();

        for (Audit t : audits) {
            auditsLow.add(t);

        }

        return auditsLow;

    }

    public List<Audit> getAuditsByUserAndDateRange(String userId, String start, String end) {
        LocalDateTime startDate = LocalDateTime.parse(start);
        LocalDateTime endDate = LocalDateTime.parse(end);

        return auditRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }

    public List<Audit> getAuditsByRiskLevelAndDateRange(String riskLevel, String start, String end) {
        LocalDateTime startDate = LocalDateTime.parse(start);

        LocalDateTime endDate = LocalDateTime.parse(end);
        return auditRepository.findByNivelRiesgoAndDateBetween(riskLevel, startDate, endDate);
    }

}
