package com.epit.admin.internal.dto;

import com.epit.admin.vision.entity.VehicleDetection;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class VisionResultRequest {

    @NotNull
    private Long stationId;

    @NotNull
    private LocalDateTime detectedAt;

    @NotNull
    private VehicleDetection.VehicleType vehicleType;

    @NotNull
    private Boolean isElectric;

    private String plateNumber;

    @NotNull
    private Double confidence;

    private String boundingBox;
    private String imagePath;
}
