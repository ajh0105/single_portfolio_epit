package com.epit.admin.alert.dto;

import com.epit.admin.alert.entity.Alert;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AlertResponse {
    private Long id;
    private Long stationId;
    private String stationName;
    private Alert.AlertType alertType;
    private Alert.Severity severity;
    private String title;
    private String message;
    private String referenceType;
    private Long referenceId;
    @JsonProperty("isRead")
    private boolean isRead;
    private LocalDateTime createdAt;

    public static AlertResponse from(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .stationId(alert.getStation() != null ? alert.getStation().getId() : null)
                .stationName(alert.getStation() != null ? alert.getStation().getName() : null)
                .alertType(alert.getAlertType())
                .severity(alert.getSeverity())
                .title(alert.getTitle())
                .message(alert.getMessage())
                .referenceType(alert.getReferenceType())
                .referenceId(alert.getReferenceId())
                .isRead(alert.isRead())
                .createdAt(alert.getCreatedAt())
                .build();
    }
}
