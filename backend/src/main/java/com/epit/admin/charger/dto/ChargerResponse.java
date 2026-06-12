package com.epit.admin.charger.dto;

import com.epit.admin.charger.entity.Charger;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ChargerResponse(
        Long id,
        Long stationId,
        String stationName,
        String chargerCode,
        String connectorType,
        int maxPowerKw,
        String status,
        BigDecimal healthScore,
        LocalDateTime lastMaintenanceAt
) {
    public static ChargerResponse from(Charger c) {
        return new ChargerResponse(
                c.getId(),
                c.getStation().getId(),
                c.getStation().getName(),
                c.getChargerCode(),
                c.getConnectorType().name(),
                c.getMaxPowerKw(),
                c.getStatus().name(),
                c.getHealthScore(),
                c.getLastMaintenanceAt()
        );
    }
}
