package com.epit.domain.station;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "charging_stations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChargingStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StationStatus status = StationStatus.ACTIVE;

    @Column(nullable = false)
    private int totalChargers = 0;

    @Column(length = 50)
    private String region;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Charger> chargers = new ArrayList<>();

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Camera> cameras = new ArrayList<>();

    @Builder
    public ChargingStation(String name, String address, BigDecimal latitude, BigDecimal longitude,
                           StationStatus status, String region) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status != null ? status : StationStatus.ACTIVE;
        this.region = region;
    }

    public void updateStatus(StationStatus status) {
        this.status = status;
    }

    public void incrementChargerCount() {
        this.totalChargers++;
    }

    public void decrementChargerCount() {
        if (this.totalChargers > 0) this.totalChargers--;
    }

    public enum StationStatus {
        ACTIVE, INACTIVE, MAINTENANCE
    }
}
