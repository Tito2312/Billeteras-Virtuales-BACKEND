package com.fintech.dbilleteras_virtuales.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.dto.WalletRequest;
import com.fintech.dbilleteras_virtuales.model.User;
import com.fintech.dbilleteras_virtuales.model.Wallet;
import com.fintech.dbilleteras_virtuales.repository.UserRepository;
import com.fintech.dbilleteras_virtuales.repository.WalletRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public Wallet createWallet(WalletRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!userRepository.existsById(request.getUserId())) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        String transferKey = generateTransferKey(request.getName(), user.getDocumentNumber());

        Wallet wallet = Wallet.builder()
                .userId(request.getUserId())
                .name(request.getName())
                .type(request.getType())
                .balance(0.0)
                .isActive(true)
                .createdAt(LocalDate.now())
                .transferKey(transferKey)
                .build();

        return walletRepository.save(wallet);
    }

    public List<Wallet> findAllByUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        return walletRepository.findAllByUserId(userId);
    }

    public Wallet findById(String id, String userId) {
        Optional<Wallet> walletOpt = walletRepository.findById(id);
        if (walletOpt.isEmpty() || !walletOpt.get().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Billetera no encontrada o acceso denegado");
        }
        return walletOpt.get();
    }

    public Wallet update(String id, String userId, WalletRequest request) {
        Wallet wallet = findById(id, userId);
        wallet.setName(request.getName());
        wallet.setType(request.getType());
        return walletRepository.save(wallet);
    }

    public Wallet activate(String id, String userId) {
        Wallet wallet = findById(id, userId);
        wallet.setActive(true);
        return walletRepository.save(wallet);
    }

    public Wallet deactivate(String id, String userId) {
        Wallet wallet = findById(id, userId);
        wallet.setActive(false);
        return walletRepository.save(wallet);
    }

    public Double getBalance(String id, String userId) {
        Wallet wallet = findById(id, userId);
        double balance = wallet.getBalance();

        if (balance < 100.0) {
            var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            notificationService.notificationLowBalance(user.getEmail(), wallet.getName(), balance);
        }

        return balance;
    }

    public boolean hasSufficientBalance(String walletId, String userId, double amount) {
        Wallet wallet = findById(walletId, userId);
        return wallet.getBalance() >= amount && wallet.isActive();
    }

    public Wallet updateBalance(String walletId, String userId, double amount) {
        Wallet wallet = findById(walletId, userId);

        if (!wallet.isActive()) {
            throw new RuntimeException("La billetera está inactiva");
        }

        double newBalance = wallet.getBalance() + amount;

        if (newBalance < 0) {
            throw new RuntimeException("Saldo insuficiente");
        }

        wallet.setBalance(newBalance);
        return walletRepository.save(wallet);
    }

    public Wallet validateWalletExists(String walletId, String userId) {
        Wallet wallet = findById(walletId, userId);
        if (!wallet.isActive()) {
            throw new RuntimeException("La billetera está inactiva");
        }
        return wallet;
    }

    public Wallet validateWalletExistsForOwner(String walletId, String ownerId) {
        Wallet wallet = findById(walletId, ownerId);
        if (!wallet.isActive()) {
            throw new RuntimeException("La billetera está inactiva");
        }
        return wallet;
    }

    public Wallet findByTransferKey(String transferKey) {
    return walletRepository.findByTransferKey(transferKey)
        .orElseThrow(() -> new RuntimeException("Billetera no encontrada con esa clave: " + transferKey));
}


    private String generateTransferKey(String walletName, String documentNumber) {
        String cleanName = walletName.trim().toLowerCase().replace(" ", "");
        return cleanName + documentNumber;
    }
}
