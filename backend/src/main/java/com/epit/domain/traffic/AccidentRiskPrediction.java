package com.epit.domain.traffic;

import com.epit.domain.station.ChargingStation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accident_risk_predictions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccidentRiskPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private ChargingStation station;

    @Column(nullable = false)
    private LocalDateTime predictedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskLevel riskLevel;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal riskScore;

    @Column(length = 100)
    private String primaryRiskFactor;

    @Column(columnDefinition = "jsonb")
    private String riskFactors;

    private LocalDateTime validUntil;

    @Column(length = 20)
    private String modelVersion;

    @Builder
    public AccidentRiskPrediction(ChargingStation station, RiskLevel riskLevel, BigDecimal riskScore,
                                  String primaryRiskFactor, String riskFactors,
                                  LocalDateTime validUntil, String modelVersion) {
        this.station = station;
        this.riskLevel = riskLevel;
        this.riskScore = riskScore;
        this.primaryRiskFactor = primaryRiskFactor;
        this.riskFactors = riskFactors;
        this.validUntil = validUntil;
        this.modelVersion = modelVersion;
    }

    public boolean isExpired() {
        return validUntil != null && LocalDateTime.now().isAfter(validUntil);
    }

    public enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
