package com.epit.service;

import com.epit.domain.alert.Alert;
import com.epit.domain.station.Charger;
import com.epit.domain.station.ChargingStation;
import com.epit.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ChargingStationRepository stationRepository;
    private final ChargerRepository chargerRepository;
    private final AlertRepository alertRepository;
    private final ParkingViolationRepository violationRepository;

    @Cacheable(value = "dashboard-kpi", key = "'global'")
    public DashboardKpi getGlobalKpi() {
        long totalStations = stationRepository.count();
        long activeStations = stationRepository.findByStatus(ChargingStation.StationStatus.ACTIVE).size();
        long faultChargers = chargerRepository.findByStatus(Charger.ChargerStatus.FAULT).size();
        long pendingAlerts = alertRepository.countByIsResolvedFalse();
        long criticalAlerts = alertRepository.countByIsResolvedFalseAndSeverity(Alert.Severity.CRITICAL);
        long pendingViolations = violationRepository.countPendingViolationsSince(
                LocalDateTime.now().minusHours(24));

        double availabilityRate = totalStations == 0 ? 0 :
                (double) activeStations / totalStations * 100;

        return new DashboardKpi(
                totalStations,
                activeStations,
                faultChargers,
                pendingAlerts,
                criticalAlerts,
                pendingViolations,
                availabilityRate
        );
    }

    public List<Alert> getRecentCriticalAlerts() {
        return alertRepository.findActiveBySeverity(Alert.Severity.CRITICAL);
    }

    public record DashboardKpi(
            long totalStations,
            long activeStations,
            long faultChargers,
            long pendingAlerts,
            long criticalAlerts,
            long pendingViolationsLast24h,
            double stationAvailabilityRate
    ) {}
}
