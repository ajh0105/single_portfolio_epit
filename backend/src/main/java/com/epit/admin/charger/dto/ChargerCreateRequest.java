package com.epit.admin.charger.dto;

import com.epit.admin.charger.entity.Charger;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChargerCreateRequest {
    @NotNull
    private Long stationId;
    @NotBlank
    private String chargerCode;
    @NotNull
    private Charger.ConnectorType connectorType;
    @NotNull
    private Integer maxPowerKw;
    private Charger.ChargerStatus status = Charger.ChargerStatus.AVAILABLE;
}
