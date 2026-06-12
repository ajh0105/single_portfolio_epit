package com.epit.admin.charger.dto;

import com.epit.admin.phm.entity.EquipmentHealth;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EquipmentHealthResponse(
        Long id,
        Long chargerId,
        LocalDateTime measuredAt,
        BigDecimal voltage,
        BigDecimal current,
        BigDecimal temperature,
        BigDecimal vibration
) {
    public static EquipmentHealthResponse from(EquipmentHealth eh) {
        return new EquipmentHealthResponse(
                eh.getId(),
                eh.getCharger().getId(),
                eh.getMeasuredAt(),
                eh.getVoltage(),
                eh.getCurrent(),
                eh.getTemperature(),
                eh.getVibration()
        );
    }
}
