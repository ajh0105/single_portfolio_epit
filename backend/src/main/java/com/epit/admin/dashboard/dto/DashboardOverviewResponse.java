package com.epit.admin.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardOverviewResponse {
    private long totalStations;
    private long activeChargers;
    private long faultChargers;
    private long todayViolations;
    private long criticalPredictions;
    private long highRiskStations;
    private long unreadAlerts;
}
