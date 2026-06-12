package com.epit.admin.station.dto;

import com.epit.admin.station.entity.ChargingStation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class StationCreateRequest {
    @NotBlank
    private String stationCode;
    @NotBlank
    private String name;
    @NotBlank
    private String address;
    @NotNull
    private BigDecimal latitude;
    @NotNull
    private BigDecimal longitude;
    @NotNull
    private ChargingStation.OperationStatus operationStatus;
    private String cameraDeviceId;
}
