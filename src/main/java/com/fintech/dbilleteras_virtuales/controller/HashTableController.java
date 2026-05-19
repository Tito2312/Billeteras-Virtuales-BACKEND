package com.fintech.dbilleteras_virtuales.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fintech.dbilleteras_virtuales.model.User;
import com.fintech.dbilleteras_virtuales.model.Wallet;
import com.fintech.dbilleteras_virtuales.service.HashTableService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hashtable")
@RequiredArgsConstructor
public class HashTableController {

    private final HashTableService hashTableService;

    // -- USUARIOS --

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(hashTableService.getAllUsers());
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(hashTableService.getUserById(userId));
    }

    // -- BILLETERAS --

    @GetMapping("/wallets")
    public ResponseEntity<List<Wallet>> getAllWallets() {
        return ResponseEntity.ok(hashTableService.getAllWallets());
    }

    @GetMapping("/wallets/{walletId}")
    public ResponseEntity<Wallet> getWalletById(@PathVariable String walletId) {
        return ResponseEntity.ok(hashTableService.getWalletById(walletId));
    }
}