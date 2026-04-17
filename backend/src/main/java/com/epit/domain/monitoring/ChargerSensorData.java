package com.epit.domain.monitoring;

import com.epit.domain.station.Charger;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "charger_sensor_data")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChargerSensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charger_id", nullable = false)
    private Charger charger;

    @Column(nullable = false)
    private LocalDateTime recordedAt = LocalDateTime.now();

    @Column(precision = 8, scale = 2)
    private BigDecimal voltage;

    @Column(precision = 8, scale = 2)
    private BigDecimal currentAmpere;

    @Column(precision = 6, scale = 2)
    private BigDecimal temperatureCelsius;

    @Column(precision = 10, scale = 2)
    private BigDecimal powerOutputKw;

    @Column(precision = 12, scale = 4)
    private BigDecimal energyDeliveredKwh;

    @Column(length = 20)
    private String errorCode;

    @Column(columnDefinition = "jsonb")
    private String rawData;

    @Builder
    public ChargerSensorData(Charger charger, BigDecimal voltage, BigDecimal currentAmpere,
                             BigDecimal temperatureCelsius, BigDecimal powerOutputKw,
                             BigDecimal energyDeliveredKwh, String errorCode, String rawData) {
        this.charger = charger;
        this.voltage = voltage;
        this.currentAmpere = currentAmpere;
        this.temperatureCelsius = temperatureCelsius;
        this.powerOutputKw = powerOutputKw;
        this.energyDeliveredKwh = energyDeliveredKwh;
        this.errorCode = errorCode;
        this.rawData = rawData;
    }
}
