package com.epit.admin.vision.entity;

import com.epit.admin.station.entity.ChargingStation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "parking_violation",
        indexes = {
                @Index(name = "idx_violation_station_time", columnList = "station_id, occurred_at DESC"),
                @Index(name = "idx_violation_status", columnList = "status")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class ParkingViolation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detection_id", nullable = false, unique = true)
    private VehicleDetection detection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private ChargingStation station;

    @Column(nullable = false, length = 20)
    private String plateNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ViolationType violationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ViolationStatus status;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    private LocalDateTime resolvedAt;

    @Column(length = 255)
    private String evidenceImagePath;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(ViolationStatus status) {
        this.status = status;
        if (status == ViolationStatus.RESOLVED) {
            this.resolvedAt = LocalDateTime.now();
        }
    }

    public enum ViolationType {
        NON_EV_OCCUPANCY, OVERSTAY
    }

    public enum ViolationStatus {
        DETECTED, NOTIFIED, RESOLVED, FALSE_POSITIVE
    }
}
