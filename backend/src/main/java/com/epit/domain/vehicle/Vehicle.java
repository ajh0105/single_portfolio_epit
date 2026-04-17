package com.epit.domain.vehicle;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String licensePlate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleType vehicleType = VehicleType.UNKNOWN;

    @Column(length = 50)
    private String make;

    @Column(length = 50)
    private String model;

    @Column(length = 30)
    private String color;

    @Column(precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Column(nullable = false, updatable = false)
    private LocalDateTime firstDetectedAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime lastDetectedAt = LocalDateTime.now();

    @Builder
    public Vehicle(String licensePlate, VehicleType vehicleType, String make,
                   String model, String color, BigDecimal confidenceScore) {
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType != null ? vehicleType : VehicleType.UNKNOWN;
        this.make = make;
        this.model = model;
        this.color = color;
        this.confidenceScore = confidenceScore;
    }

    public void updateDetection(VehicleType vehicleType, BigDecimal confidenceScore) {
        this.vehicleType = vehicleType;
        this.confidenceScore = confidenceScore;
        this.lastDetectedAt = LocalDateTime.now();
    }

    public enum VehicleType {
        EV, NON_EV, UNKNOWN
    }
}
