package com.fintech.dbilleteras_virtuales.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LevelBenefitService {

    public double getCommissionRate(String level) {
        return switch (level) {
            case "Bronze" -> 0.05;
            case "Silver" -> 0.03;
            case "Gold" -> 0.015;
            case "Platinum" -> 0.0;
            default -> 0.05;
        };
    }

    public static int getDailyTransactionLimit(String level) {
        return switch (level) {
            case "Bronze" -> 10;
            case "Silver" -> 25;
            case "Gold" -> 50;
            case "Platinum" -> Integer.MAX_VALUE;
            default -> 10;
        };
    }

    public double getPointsBonus(String level) {
        return switch (level) {
            case "Bronze" -> 0.0;
            case "Silver" -> 0.10;
            case "Gold" -> 0.25;
            case "Platinum" -> 0.50;
            default -> 0.0;
        };
    }

    public int getProcessingPriority(String level) {
        return switch (level) {
            case "Platinum" -> 1;
            case "Gold" -> 2;
            case "Silver" -> 3;
            case "Bronze" -> 4;
            default -> 4;
        };
    }

    public double applyCommission(String level, double amount) {
        double commissionRate = getCommissionRate(level);
        return amount * commissionRate;
    }

    public int applyPointsBonus(String level, int basePoints) {
        double bonusRate = getPointsBonus(level);
        return (int) (basePoints * (1 + bonusRate));
    }

    public String getLevelBenefits(String level) {
        return switch (level) {
            case "Bronze" ->
                "Comisión: 5%, Sin bono de puntos";
            case "Silver" ->
                "Comisión: 3%, Bono de puntos: 10%";
            case "Gold" ->
                "$10000, Comisión: 1.5%, Bono de puntos: 25%, Prioridad media";
            case "Platinum" ->
                "Comisión: 0%, Bono de puntos: 50%, Prioridad alta";
            default -> "Nivel no reconocido";
        };
    }
}
