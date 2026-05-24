package com.fintech.dbilleteras_virtuales.service;

import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.model.TransactionType;
import com.fintech.dbilleteras_virtuales.model.User;
import com.fintech.dbilleteras_virtuales.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final LevelBenefitService levelBenefitService;

    public int calculatePoints(TransactionType type, double amount) {

        int multiplier = switch (type) {
            case RECHARGE -> 1;
            case WITHDRAWAL -> 2;
            case TRANSFER -> 3;
        };
        return (int) (amount / 500) * multiplier;
    }

    public void updateUserPoints(String userId, int points) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String oldLevel = user.getLevel();

        user.setPoints(user.getPoints() + points);

        String newLevel = calculateLevel(user.getPoints());
        user.setLevel(newLevel);
        userRepository.save(user);

        if (!oldLevel.equals(newLevel)) {
            String benefits = levelBenefitService.getLevelBenefits(newLevel);
            notificationService.LevelUp(user.getEmail(), newLevel, benefits);

        }
    }

    public String calculateLevel(int points) {
        if (points <= 500)
            return "Bronze";
        else if (points <= 1000)
            return "Silver";
        else if (points <= 5000)
            return "Gold";
        else
            return "Platinum";
    }
}
