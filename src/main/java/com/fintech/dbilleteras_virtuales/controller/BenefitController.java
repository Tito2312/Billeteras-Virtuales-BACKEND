package com.fintech.dbilleteras_virtuales.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.fintech.dbilleteras_virtuales.model.Benefit;
import com.fintech.dbilleteras_virtuales.model.RedeemedBenefit;
import com.fintech.dbilleteras_virtuales.service.BenefitService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/benefits")
@RequiredArgsConstructor
public class BenefitController {

    private final BenefitService benefitService;

    // Listar beneficios disponibles — cualquier usuario autenticado
    @GetMapping
    public ResponseEntity<List<Benefit>> getAvailableBenefits() {
        return ResponseEntity.ok(benefitService.getAvailableBenefits());
    }

    // Listar canjes del usuario
    @GetMapping("/redeemed/{userId}")
    public ResponseEntity<List<RedeemedBenefit>> getRedeemedByUser(@PathVariable String userId) {
        return ResponseEntity.ok(benefitService.getRedeemedByUser(userId));
    }

    // Canjear un beneficio
    @PostMapping("/redeem")
    public ResponseEntity<RedeemedBenefit> redeem(@RequestParam String userId,
                                                   @RequestParam String benefitId) {
        return ResponseEntity.ok(benefitService.redeem(userId, benefitId));
    }

    // Marcar beneficio como usado
    @PatchMapping("/use/{redeemedBenefitId}")
    public ResponseEntity<RedeemedBenefit> useBenefit(@PathVariable String redeemedBenefitId,
                                                       @RequestParam String userId) {
        return ResponseEntity.ok(benefitService.useBenefit(redeemedBenefitId, userId));
    }

    // Crear beneficio — solo ADMIN
    @PostMapping
    public ResponseEntity<Benefit> createBenefit(@RequestBody Benefit benefit) {
        return ResponseEntity.status(HttpStatus.CREATED).body(benefitService.createBenefit(benefit));
    }

    // Activar / desactivar beneficio — solo ADMIN
    @PatchMapping("/toggle/{benefitId}")
    public ResponseEntity<Benefit> toggleBenefit(@PathVariable String benefitId) {
        return ResponseEntity.ok(benefitService.toggleBenefit(benefitId));
    }
}
