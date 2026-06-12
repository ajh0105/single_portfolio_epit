package com.epit.admin.internal.dto;

import com.epit.admin.phm.entity.FailurePrediction;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class PhmResultRequest {

    @NotNull
    private Long chargerId;

    @NotNull
    private LocalDateTime predictedAt;

    @NotNull
    private Double failureProbability;

    private Integer remainingUsefulLifeHours;

    @NotNull
    private FailurePrediction.RiskLevel riskLevel;

    private String predictedComponent;

    @NotNull
    private String modelVersion;

    private List<RawHealthPoint> rawSeries;

    @Getter
    @NoArgsConstructor
    public static class RawHealthPoint {
        private LocalDateTime measuredAt;
        private Double voltage;
        private Double current;
        private Double temperature;
        private Double vibration;
    }
}
