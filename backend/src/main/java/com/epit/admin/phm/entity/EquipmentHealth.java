package com.epit.admin.phm.entity;

import com.epit.admin.charger.entity.Charger;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_health",
        indexes = @Index(name = "idx_health_charger_time", columnList = "charger_id, measured_at DESC"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class EquipmentHealth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charger_id", nullable = false)
    private Charger charger;

    @Column(nullable = false)
    private LocalDateTime measuredAt;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal voltage;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal current;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal temperature;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal vibration;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
