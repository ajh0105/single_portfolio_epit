package com.epit.admin.station.entity;

import com.epit.admin.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "charging_station")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class ChargingStation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String stationCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(nullable = false)
    private int totalChargers;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OperationStatus operationStatus;

    @Column(length = 50)
    private String cameraDeviceId;

    public void update(String name, String address, BigDecimal latitude, BigDecimal longitude,
                       OperationStatus operationStatus, String cameraDeviceId) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.operationStatus = operationStatus;
        this.cameraDeviceId = cameraDeviceId;
    }

    public void updateOperationStatus(OperationStatus status) {
        this.operationStatus = status;
    }

    public void incrementTotalChargers() {
        this.totalChargers++;
    }

    public enum OperationStatus {
        ACTIVE, MAINTENANCE, CLOSED
    }
}
