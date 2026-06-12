package com.epit.admin.alert.dto;

import com.epit.admin.alert.entity.Alert;
import com.epit.admin.station.entity.ChargingStation;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AlertCreateCommand {
    private ChargingStation station;
    private Alert.AlertType alertType;
    private Alert.Severity severity;
    private String title;
    private String message;
    private String referenceType;
    private Long referenceId;
}
