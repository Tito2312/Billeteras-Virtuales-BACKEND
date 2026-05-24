package com.fintech.dbilleteras_virtuales.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fintech.dbilleteras_virtuales.service.AuditService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/user")
    public ResponseEntity<?> getAuditsByUser(@RequestParam String userId) {
        return ResponseEntity.ok(auditService.getAuditsByUser(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllAudits() {
        return ResponseEntity.ok(auditService.historyAudits());
    }

    @GetMapping("/today")
    public ResponseEntity<?> getAuditsToday() {
        return ResponseEntity.ok(auditService.AuditsToday());
    }

    @GetMapping("/date-range")
    public ResponseEntity<?> getAuditsByDateRange(@RequestParam String start, @RequestParam String end) {
        return ResponseEntity.ok(auditService.AuditsDateRange(start, end));
    }

    @GetMapping("/risk-level")
    public ResponseEntity<?> getAuditsByRiskLevel(@RequestParam String riskLevel) {
        return ResponseEntity.ok(auditService.AuditsLowLevel(riskLevel));
    }

    @GetMapping("/user/date-range")
    public ResponseEntity<?> getAuditsByUserAndDateRange(@RequestParam String userId,
            @RequestParam String start, @RequestParam String end) {
        return ResponseEntity.ok(auditService.getAuditsByUserAndDateRange(userId, start, end));
    }

    @GetMapping("/risk-level/date-range")
    public ResponseEntity<?> getAuditsByRiskLevelAndDateRange(@RequestParam String riskLevel,
            @RequestParam String start, @RequestParam String end) {
        return ResponseEntity.ok(auditService.getAuditsByRiskLevelAndDateRange(riskLevel, start, end));
    }
}
