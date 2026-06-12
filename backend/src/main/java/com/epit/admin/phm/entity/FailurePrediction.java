package com.epit.admin.phm.entity;

import com.epit.admin.charger.entity.Charger;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "failure_prediction",
        indexes = {
                @Index(name = "idx_prediction_charger_time", columnList = "charger_id, predicted_at DESC"),
                @Index(name = "idx_prediction_risk", columnList = "risk_level")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class FailurePrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charger_id", nullable = false)
    private Charger charger;

    @Column(nullable = false)
    private LocalDateTime predictedAt;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal failureProbability;

    private Integer remainingUsefulLifeHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskLevel riskLevel;

    @Column(length = 50)
    private String predictedComponent;

    @Column(nullable = false, length = 30)
    private String modelVersion;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum RiskLevel {
        NORMAL, WARNING, CRITICAL
    }
}
