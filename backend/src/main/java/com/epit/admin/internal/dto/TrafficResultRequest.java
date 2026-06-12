package com.epit.admin.internal.dto;

import com.epit.admin.traffic.entity.RiskPrediction;
import com.epit.admin.traffic.entity.TrafficData;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TrafficResultRequest {

    @NotNull
    private Long stationId;

    @NotNull
    private TrafficDataPayload trafficData;

    @NotNull
    private RiskPayload risk;

    @Getter
    @NoArgsConstructor
    public static class TrafficDataPayload {
        private LocalDateTime collectedAt;
        private Integer vehicleCount;
        private Double avgSpeedKmh;
        private TrafficData.CongestionLevel congestionLevel;
        private TrafficData.Weather weather;
        private TrafficData.RoadSurface roadSurface;
    }

    @Getter
    @NoArgsConstructor
    public static class RiskPayload {
        private RiskPrediction.RiskLevel riskLevel;
        private Double riskScore;
        private java.util.Map<String, Double> contributingFactors;
        private String modelVersion;
    }
}
