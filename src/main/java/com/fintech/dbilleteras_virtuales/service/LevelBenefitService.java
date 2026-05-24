package com.fintech.dbilleteras_virtuales.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LevelBenefitService {

    public double getCommissionRate(String level) {
        return switch (level) {
            case "Bronce", "Bronze" -> 0.05;
            case "Plata", "Silver" -> 0.03;
            case "Oro", "Gold" -> 0.015;
            case "Platino", "Platinum" -> 0.0;
            default -> 0.05;
        };
    }

    public int getDailyTransactionLimit(String level) {
        return switch (level) {
            case "Bronce", "Bronze" -> 10;
            case "Plata", "Silver" -> 25;
            case "Oro", "Gold" -> 50;
            case "Platino", "Platinum" -> Integer.MAX_VALUE;
            default -> 10;
        };
    }

    public double getPointsBonus(String level) {
        return switch (level) {
            case "Bronce", "Bronze" -> 0.0;
            case "Plata", "Silver" -> 0.10;
            case "Oro", "Gold" -> 0.25;
            case "Platino", "Platinum" -> 0.50;
            default -> 0.0;
        };
    }

    public int getProcessingPriority(String level) {
        return switch (level) {
            case "Platino", "Platinum" -> 1;  
            case "Oro", "Gold" -> 2;
            case "Plata", "Silver" -> 3;
            case "Bronce", "Bronze" -> 4;      
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
            case "Bronce", "Bronze" ->
                "Comisión: 5%, Sin bono de puntos";
            case "Plata", "Silver" ->
                "Comisión: 3%, Bono de puntos: 10%";
            case "Oro", "Gold" ->
                "Comisión: 1.5%, Bono de puntos: 25%, Prioridad media";
            case "Platino", "Platinum" ->
                "Comisión: 0%, Bono de puntos: 50%, Prioridad alta";
            default -> "Nivel no reconocido";
        };
    }
}
