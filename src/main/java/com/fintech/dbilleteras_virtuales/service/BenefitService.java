package com.fintech.dbilleteras_virtuales.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.model.Benefit;
import com.fintech.dbilleteras_virtuales.model.RedeemedBenefit;
import com.fintech.dbilleteras_virtuales.model.User;
import com.fintech.dbilleteras_virtuales.repository.BenefitRepository;
import com.fintech.dbilleteras_virtuales.repository.RedeemedBenefitRepository;
import com.fintech.dbilleteras_virtuales.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BenefitService {

    private final BenefitRepository benefitRepository;
    private final RedeemedBenefitRepository redeemedBenefitRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;

    public List<Benefit> getAvailableBenefits() {
        return benefitRepository.findByActiveTrue();
    }

    public List<RedeemedBenefit> getRedeemedByUser(String userId) {
        return redeemedBenefitRepository.findByUserId(userId);
    }

    public RedeemedBenefit redeem(String userId, String benefitId, String walletId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Benefit benefit = benefitRepository.findById(benefitId)
                .orElseThrow(() -> new RuntimeException("Beneficio no encontrado"));

        if (!benefit.isActive()) {
            throw new RuntimeException("Este beneficio no está disponible");
        }

        if (user.getPoints() < benefit.getPointsCost()) {
            throw new RuntimeException("Puntos insuficientes. Necesitas "
                    + benefit.getPointsCost() + " puntos y tienes " + user.getPoints());
        }

        user.setPoints(user.getPoints() - benefit.getPointsCost());
        userRepository.save(user);

        if (walletId != null && !walletId.isEmpty() && benefit.getMoneyValue() > 0) {
            walletService.updateBalance(walletId, userId, benefit.getMoneyValue());
        }

        RedeemedBenefit redeemed = RedeemedBenefit.builder()
                .userId(userId)
                .benefitId(benefitId)
                .benefitName(benefit.getName())
                .pointsSpent(benefit.getPointsCost())
                .status("ACTIVE")
                .redeemedAt(LocalDateTime.now())
                .build();

        RedeemedBenefit saved = redeemedBenefitRepository.save(redeemed);

        return saved;
    }

    public List<RedeemedBenefit> getAllRedeemed(){
        return redeemedBenefitRepository.findAll();
    }

    public RedeemedBenefit useBenefit(String redeemedBenefitId, String userId) {
        RedeemedBenefit redeemed = redeemedBenefitRepository.findById(redeemedBenefitId)
                .orElseThrow(() -> new RuntimeException("Canje no encontrado"));

        if (!redeemed.getUserId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para usar este beneficio");
        }

        if (!redeemed.getStatus().equals("ACTIVE")) {
            throw new RuntimeException("Este beneficio ya fue usado o expiró");
        }

        redeemed.setStatus("USED");
        return redeemedBenefitRepository.save(redeemed);
    }

    public Benefit createBenefit(Benefit benefit) {
        if (benefitRepository.findByCode(benefit.getCode()).isPresent()) {
            throw new RuntimeException("Ya existe un beneficio con ese código");
        }
        return benefitRepository.save(benefit);
    }

    public Benefit toggleBenefit(String benefitId) {
        Benefit benefit = benefitRepository.findById(benefitId)
                .orElseThrow(() -> new RuntimeException("Beneficio no encontrado"));
        benefit.setActive(!benefit.isActive());
        return benefitRepository.save(benefit);
    }
}
