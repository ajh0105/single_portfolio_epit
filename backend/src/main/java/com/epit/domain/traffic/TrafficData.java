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
@Table(name = "traffic_data")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrafficData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private ChargingStation station;

    @Column(nullable = false)
    private LocalDateTime recordedAt = LocalDateTime.now();

    private Integer trafficVolume;

    @Column(precision = 6, scale = 2)
    private BigDecimal avgSpeedKmh;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CongestionLevel congestionLevel;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private WeatherCondition weatherCondition;

    private Integer visibilityM;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private RoadSurfaceCondition roadSurfaceCondition;

    @Column(nullable = false)
    private int incidentCount = 0;

    @Column(columnDefinition = "jsonb")
    private String rawData;

    @Builder
    public TrafficData(ChargingStation station, Integer trafficVolume, BigDecimal avgSpeedKmh,
                       CongestionLevel congestionLevel, WeatherCondition weatherCondition,
                       Integer visibilityM, RoadSurfaceCondition roadSurfaceCondition,
                       int incidentCount, String rawData) {
        this.station = station;
        this.trafficVolume = trafficVolume;
        this.avgSpeedKmh = avgSpeedKmh;
        this.congestionLevel = congestionLevel;
        this.weatherCondition = weatherCondition;
        this.visibilityM = visibilityM;
        this.roadSurfaceCondition = roadSurfaceCondition;
        this.incidentCount = incidentCount;
        this.rawData = rawData;
    }

    public enum CongestionLevel { SMOOTH, SLOW, CONGESTED, BLOCKED }
    public enum WeatherCondition { CLEAR, RAIN, SNOW, FOG, STORM }
    public enum RoadSurfaceCondition { DRY, WET, ICY, SNOWY }
}
