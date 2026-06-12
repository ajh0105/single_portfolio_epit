package com.epit.admin.station.dto;

import com.epit.admin.station.entity.ChargingStation;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class StationResponse {
    private Long id;
    private String stationCode;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private int totalChargers;
    private ChargingStation.OperationStatus operationStatus;
    private String cameraDeviceId;
    private LocalDateTime createdAt;

    public static StationResponse from(ChargingStation s) {
        return StationResponse.builder()
                .id(s.getId())
                .stationCode(s.getStationCode())
                .name(s.getName())
                .address(s.getAddress())
                .latitude(s.getLatitude())
                .longitude(s.getLongitude())
                .totalChargers(s.getTotalChargers())
                .operationStatus(s.getOperationStatus())
                .cameraDeviceId(s.getCameraDeviceId())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
