package com.epit.domain.vehicle;

import com.epit.domain.station.Camera;
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
@Table(name = "parking_violations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParkingViolation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charger_id", nullable = false)
    private Charger charger;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camera_id")
    private Camera camera;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(length = 20)
    private String licensePlate;

    @Column(nullable = false)
    private LocalDateTime detectedAt = LocalDateTime.now();

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 500)
    private String thumbnailUrl;

    @Column(precision = 5, scale = 4)
    private BigDecimal detectionConfidence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ViolationStatus status = ViolationStatus.PENDING;

    private LocalDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder
    public ParkingViolation(Charger charger, Camera camera, Vehicle vehicle, String licensePlate,
                            String imageUrl, String thumbnailUrl, BigDecimal detectionConfidence) {
        this.charger = charger;
        this.camera = camera;
        this.vehicle = vehicle;
        this.licensePlate = licensePlate;
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.detectionConfidence = detectionConfidence;
    }

    public void resolve(User resolvedBy, String notes) {
        this.status = ViolationStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = resolvedBy;
        this.notes = notes;
    }

    public void markFalsePositive(User resolvedBy, String notes) {
        this.status = ViolationStatus.FALSE_POSITIVE;
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = resolvedBy;
        this.notes = notes;
    }

    public void confirm() {
        this.status = ViolationStatus.CONFIRMED;
    }

    public enum ViolationStatus {
        PENDING, CONFIRMED, RESOLVED, FALSE_POSITIVE
    }
}
