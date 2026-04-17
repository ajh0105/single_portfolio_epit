package com.epit.domain.station;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "cameras")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Camera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private ChargingStation station;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charger_id")
    private Charger charger;

    @Column(nullable = false, length = 100)
    private String cameraName;

    @Column(length = 255)
    private String locationDescription;

    @Column(length = 50)
    private String ipAddress;

    @Column(length = 500)
    private String streamUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CameraStatus status = CameraStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Camera(ChargingStation station, Charger charger, String cameraName,
                  String locationDescription, String ipAddress, String streamUrl) {
        this.station = station;
        this.charger = charger;
        this.cameraName = cameraName;
        this.locationDescription = locationDescription;
        this.ipAddress = ipAddress;
        this.streamUrl = streamUrl;
    }

    public void updateStatus(CameraStatus status) {
        this.status = status;
    }

    public enum CameraStatus {
        ACTIVE, INACTIVE, ERROR
    }
}
