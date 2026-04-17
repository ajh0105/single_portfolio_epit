package com.epit.domain.station;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "chargers",
       uniqueConstraints = @UniqueConstraint(columnNames = {"station_id", "charger_number"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Charger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private ChargingStation station;

    @Column(nullable = false, length = 20)
    private String chargerNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChargerType chargerType;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal maxPowerKw;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChargerStatus status = ChargerStatus.AVAILABLE;

    @Column(length = 30)
    private String firmwareVersion;

    private LocalDate installationDate;

    private LocalDate lastMaintenanceDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Charger(ChargingStation station, String chargerNumber, ChargerType chargerType,
                   BigDecimal maxPowerKw, String firmwareVersion, LocalDate installationDate) {
        this.station = station;
        this.chargerNumber = chargerNumber;
        this.chargerType = chargerType;
        this.maxPowerKw = maxPowerKw;
        this.firmwareVersion = firmwareVersion;
        this.installationDate = installationDate;
    }

    public void updateStatus(ChargerStatus status) {
        this.status = status;
    }

    public void recordMaintenance() {
        this.lastMaintenanceDate = LocalDate.now();
        this.status = ChargerStatus.AVAILABLE;
    }

    public enum ChargerType {
        AC_SLOW, DC_FAST, ULTRA_FAST
    }

    public enum ChargerStatus {
        AVAILABLE, IN_USE, FAULT, MAINTENANCE
    }
}
