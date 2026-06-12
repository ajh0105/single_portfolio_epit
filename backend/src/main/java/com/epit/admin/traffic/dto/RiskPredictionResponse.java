package com.epit.admin.traffic.dto;

import com.epit.admin.traffic.entity.RiskPrediction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public record RiskPredictionResponse(
        Long id,
        Long stationId,
        Long trafficDataId,
        LocalDateTime predictedAt,
        String riskLevel,
        BigDecimal riskScore,
        Map<String, Double> contributingFactors,
        String modelVersion
) {
    public static RiskPredictionResponse from(RiskPrediction r) {
        return new RiskPredictionResponse(
                r.getId(),
                r.getStation().getId(),
                r.getTrafficData().getId(),
                r.getPredictedAt(),
                r.getRiskLevel().name(),
                r.getRiskScore(),
                r.getContributingFactors(),
                r.getModelVersion()
        );
    }
}
