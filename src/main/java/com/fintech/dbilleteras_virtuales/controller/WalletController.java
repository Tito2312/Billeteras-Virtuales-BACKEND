package com.fintech.dbilleteras_virtuales.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

import com.fintech.dbilleteras_virtuales.dto.WalletRequest;
import com.fintech.dbilleteras_virtuales.model.Wallet;
import com.fintech.dbilleteras_virtuales.service.WalletService;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<Wallet> create(@RequestBody @Valid WalletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.createWallet(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Wallet>> findAllByUser(@PathVariable String userId) {
        return ResponseEntity.ok(walletService.findAllByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Wallet> findById(@PathVariable String id, @RequestParam String userId) {
        return ResponseEntity.ok(walletService.findById(id, userId));
    }

    @PostMapping("/{id}/balance")
    public ResponseEntity<Double> getBalance(@PathVariable String id, @RequestParam String userId) {
        return ResponseEntity.ok(walletService.getBalance(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Wallet> update(@PathVariable String id, @RequestBody @Valid WalletRequest request,
            @RequestParam String userId) {
        return ResponseEntity.ok(walletService.update(id, userId, request));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Wallet> activate(@PathVariable String id, @RequestParam String userId) {
        return ResponseEntity.ok(walletService.activate(id, userId));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Wallet> deactivate(@PathVariable String id, @RequestParam String userId) {
        return ResponseEntity.ok(walletService.deactivate(id, userId));
    }

    @GetMapping("/by-transfer-key")
    public ResponseEntity<Wallet> findByTransferKey(@RequestParam String transferKey) {
        return ResponseEntity.ok(walletService.findByTransferKey(transferKey));
    }

}
