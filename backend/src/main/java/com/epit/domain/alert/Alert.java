package com.epit.domain.alert;

import com.epit.domain.station.ChargingStation;
import com.epit.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(length = 50)
    private String entityType;

    private Long entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private ChargingStation station;

    @Column(nullable = false)
    private boolean isResolved = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime acknowledgedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acknowledged_by")
    private User acknowledgedBy;

    private LocalDateTime resolvedAt;

    @Builder
    public Alert(AlertType alertType, Severity severity, String title, String message,
                 String entityType, Long entityId, ChargingStation station) {
        this.alertType = alertType;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.entityType = entityType;
        this.entityId = entityId;
        this.station = station;
    }

    public void acknowledge(User user) {
        this.acknowledgedBy = user;
        this.acknowledgedAt = LocalDateTime.now();
    }

    public void resolve() {
        this.isResolved = true;
        this.resolvedAt = LocalDateTime.now();
    }

    public enum AlertType {
        PARKING_VIOLATION, EQUIPMENT_FAULT, ACCIDENT_RISK, CHARGER_STATUS
    }

    public enum Severity {
        INFO, WARNING, CRITICAL
    }
}
