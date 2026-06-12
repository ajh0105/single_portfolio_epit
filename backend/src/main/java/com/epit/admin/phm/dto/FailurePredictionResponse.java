package com.epit.admin.phm.dto;

import com.epit.admin.phm.entity.FailurePrediction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FailurePredictionResponse(
        Long id,
        Long chargerId,
        String chargerCode,
        String chargerStatus,
        Integer chargerMaxPowerKw,
        BigDecimal chargerHealthScore,
        LocalDateTime chargerLastMaintenanceAt,
        LocalDateTime predictedAt,
        BigDecimal failureProbability,
        Integer remainingUsefulLifeHours,
        String riskLevel,
        String predictedComponent,
        String modelVersion
) {
    public static FailurePredictionResponse from(FailurePrediction fp) {
        return new FailurePredictionResponse(
                fp.getId(),
                fp.getCharger().getId(),
                fp.getCharger().getChargerCode(),
                fp.getCharger().getStatus().name(),
                fp.getCharger().getMaxPowerKw(),
                fp.getCharger().getHealthScore(),
                fp.getCharger().getLastMaintenanceAt(),
                fp.getPredictedAt(),
                fp.getFailureProbability(),
                fp.getRemainingUsefulLifeHours(),
                fp.getRiskLevel().name(),
                fp.getPredictedComponent(),
                fp.getModelVersion()
        );
    }
}
