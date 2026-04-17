package com.epit.domain.monitoring;

import com.epit.domain.station.Charger;
import com.epit.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_failure_predictions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EquipmentFailurePrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charger_id", nullable = false)
    private Charger charger;

    @Column(nullable = false)
    private LocalDateTime predictedAt = LocalDateTime.now();

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal failureProbability;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private FailureType failureType;

    private LocalDateTime predictedFailureAt;

    @Column(precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PredictionStatus status = PredictionStatus.ACTIVE;

    @Column(length = 20)
    private String modelVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acknowledged_by")
    private User acknowledgedBy;

    private LocalDateTime acknowledgedAt;

    @Builder
    public EquipmentFailurePrediction(Charger charger, BigDecimal failureProbability,
                                      FailureType failureType, LocalDateTime predictedFailureAt,
                                      BigDecimal confidenceScore, String recommendation,
                                      String modelVersion) {
        this.charger = charger;
        this.failureProbability = failureProbability;
        this.failureType = failureType;
        this.predictedFailureAt = predictedFailureAt;
        this.confidenceScore = confidenceScore;
        this.recommendation = recommendation;
        this.modelVersion = modelVersion;
    }

    public void acknowledge(User user) {
        this.status = PredictionStatus.ACKNOWLEDGED;
        this.acknowledgedBy = user;
        this.acknowledgedAt = LocalDateTime.now();
    }

    public void resolve() {
        this.status = PredictionStatus.RESOLVED;
    }

    public enum FailureType {
        VOLTAGE_ANOMALY, OVERHEATING, CONNECTOR_FAULT, COOLANT_LEAK, COMMUNICATION_ERROR
    }

    public enum PredictionStatus {
        ACTIVE, ACKNOWLEDGED, RESOLVED, EXPIRED
    }
}
