package com.epit.admin.traffic.entity;

import com.epit.admin.station.entity.ChargingStation;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "traffic_data",
        indexes = @Index(name = "idx_traffic_station_time", columnList = "station_id, collected_at DESC"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class TrafficData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private ChargingStation station;

    @Column(nullable = false)
    private LocalDateTime collectedAt;

    @Column(nullable = false)
    private int vehicleCount;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal avgSpeedKmh;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CongestionLevel congestionLevel;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Weather weather;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RoadSurface roadSurface;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum CongestionLevel { SMOOTH, SLOW, CONGESTED }
    public enum Weather { CLEAR, RAIN, SNOW, FOG }
    public enum RoadSurface { DRY, WET, ICY }
}
