package com.epit.admin.dashboard.service;

import com.epit.admin.alert.repository.AlertRepository;
import com.epit.admin.charger.repository.ChargerRepository;
import com.epit.admin.dashboard.dto.DashboardOverviewResponse;
import com.epit.admin.phm.repository.FailurePredictionRepository;
import com.epit.admin.station.repository.ChargingStationRepository;
import com.epit.admin.traffic.repository.RiskPredictionRepository;
import com.epit.admin.vision.repository.ParkingViolationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ChargingStationRepository stationRepository;
    private final ChargerRepository chargerRepository;
    private final ParkingViolationRepository violationRepository;
    private final FailurePredictionRepository predictionRepository;
    private final RiskPredictionRepository riskRepository;
    private final AlertRepository alertRepository;

    @Transactional(readOnly = true)
    public DashboardOverviewResponse getOverview() {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        return DashboardOverviewResponse.builder()
                .totalStations(stationRepository.count())
                .activeChargers(chargerRepository.count())
                .faultChargers(0L) // 실제로는 쿼리 필요
                .todayViolations(violationRepository.countAllTodayViolations(todayStart))
                .criticalPredictions(predictionRepository.countCriticalChargers())
                .highRiskStations(riskRepository.countHighRiskStations())
                .unreadAlerts(alertRepository.countByIsRead(false))
                .build();
    }
}
