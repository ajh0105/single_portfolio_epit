package com.epit.admin.vision.dto;

import com.epit.admin.vision.entity.ParkingViolation;

import java.time.LocalDateTime;

public record ViolationResponse(
        Long id,
        Long stationId,
        String stationName,
        String plateNumber,
        String violationType,
        String status,
        LocalDateTime occurredAt,
        LocalDateTime resolvedAt,
        String evidenceImagePath
) {
    public static ViolationResponse from(ParkingViolation v) {
        return new ViolationResponse(
                v.getId(),
                v.getStation().getId(),
                v.getStation().getName(),
                v.getPlateNumber(),
                v.getViolationType().name(),
                v.getStatus().name(),
                v.getOccurredAt(),
                v.getResolvedAt(),
                v.getEvidenceImagePath()
        );
    }
}
