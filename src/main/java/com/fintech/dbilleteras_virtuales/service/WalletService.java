package com.fintech.dbilleteras_virtuales.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.dto.WalletRequest;
import com.fintech.dbilleteras_virtuales.model.Wallet;
import com.fintech.dbilleteras_virtuales.repository.UserRepository;
import com.fintech.dbilleteras_virtuales.repository.WalletRepository;
import com.fintech.dbilleteras_virtuales.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public Wallet createWallet(WalletRequest request) {
        if (!userRepository.existsById(request.getUserId())) {
            throw new IllegalArgumentException("User not found");
        }

        Wallet wallet = Wallet.builder()
                .userId(request.getUserId())
                .name(request.getName())
                .type(request.getType())
                .balance(0.0)
                .isActive(true)
                .createdAt(LocalDate.now())
                .build();

        return walletRepository.save(wallet);
    }

    public List<Wallet> findAllByUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
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
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            notificationService.notificationLowBalance(user.getEmail(), wallet.getName(), balance);
        }

        return balance;
    }
}
