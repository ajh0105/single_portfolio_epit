package com.epit.admin.charger.entity;

import com.epit.admin.global.common.BaseTimeEntity;
import com.epit.admin.station.entity.ChargingStation;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "charger")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Charger extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private ChargingStation station;

    @Column(nullable = false, unique = true, length = 30)
    private String chargerCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConnectorType connectorType;

    @Column(nullable = false)
    private int maxPowerKw;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChargerStatus status;

    @Column(precision = 5, scale = 2)
    private BigDecimal healthScore;

    private LocalDateTime lastMaintenanceAt;

    public void updateStatus(ChargerStatus status) {
        this.status = status;
    }

    public void updateHealthScore(BigDecimal healthScore) {
        this.healthScore = healthScore;
    }

    public void update(ConnectorType connectorType, int maxPowerKw) {
        this.connectorType = connectorType;
        this.maxPowerKw = maxPowerKw;
    }

    public enum ConnectorType {
        CCS1, CCS2, CHADEMO, AC3
    }

    public enum ChargerStatus {
        AVAILABLE, CHARGING, FAULT, OFFLINE, RESERVED
    }
}
