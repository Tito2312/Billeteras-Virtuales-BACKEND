package com.fintech.dbilleteras_virtuales.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.dataStructure.HashTable;
import com.fintech.dbilleteras_virtuales.model.User;
import com.fintech.dbilleteras_virtuales.model.Wallet;
import com.fintech.dbilleteras_virtuales.repository.UserRepository;
import com.fintech.dbilleteras_virtuales.repository.WalletRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HashTableService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    private HashTable<String, User> buildUserTable() {
        HashTable<String, User> table = new HashTable<>();
        userRepository.findAll().forEach(u -> table.put(u.getId(), u));
        return table;
    }

    public User getUserById(String userId) {
        HashTable<String, User> table = buildUserTable();
        User user = table.get(userId);
        if (user == null) throw new RuntimeException("Usuario no encontrado");
        return user;
    }

    public List<User> getAllUsers() {
        return buildUserTable().values();
    }

    private HashTable<String, Wallet> buildWalletTable() {
        HashTable<String, Wallet> table = new HashTable<>();
        walletRepository.findAll().forEach(w -> table.put(w.getId(), w));
        return table;
    }

    public Wallet getWalletById(String walletId) {
        HashTable<String, Wallet> table = buildWalletTable();
        Wallet wallet = table.get(walletId);
        if (wallet == null) throw new RuntimeException("Billetera no encontrada");
        return wallet;
    }

    public List<Wallet> getAllWallets() {
        return buildWalletTable().values();
    }
}
