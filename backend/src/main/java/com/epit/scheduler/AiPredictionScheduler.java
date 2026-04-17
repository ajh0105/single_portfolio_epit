package com.epit.scheduler;

import com.epit.domain.station.ChargingStation;
import com.epit.domain.traffic.TrafficData;
import com.epit.repository.ChargerRepository;
import com.epit.repository.ChargingStationRepository;
import com.epit.repository.TrafficDataRepository;
import com.epit.service.AiIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 주기적으로 FastAPI AI 서버에 예측 요청을 보내는 스케줄러.
 *
 * 실행 주기:
 *   - 장비 고장 예측: 매 30분 (이상 감지가 필요한 충전기 대상)
 *   - 교통 위험도 예측: 매 15분 (최신 교통 데이터 기반)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiPredictionScheduler {

    private final ChargerRepository chargerRepository;
    private final ChargingStationRepository stationRepository;
    private final TrafficDataRepository trafficDataRepository;
    private final AiIntegrationService aiIntegrationService;

    @Scheduled(fixedDelay = 1_800_000) // 30분
    public void scheduleEquipmentFailurePrediction() {
        log.info("[Scheduler] 장비 고장 예측 시작: {}", LocalDateTime.now());
        chargerRepository.findAll().forEach(charger ->
                aiIntegrationService.predictEquipmentFailureAsync(charger.getId()));
    }

    @Scheduled(fixedDelay = 900_000) // 15분
    public void scheduleAccidentRiskPrediction() {
        log.info("[Scheduler] 교통 위험도 예측 시작: {}", LocalDateTime.now());
        stationRepository.findByStatus(ChargingStation.StationStatus.ACTIVE).forEach(station -> {
            Optional<TrafficData> latestTraffic = trafficDataRepository
                    .findLatestByStationId(station.getId(), PageRequest.of(0, 1))
                    .stream().findFirst();

            latestTraffic.ifPresent(trafficData ->
                    aiIntegrationService.predictAccidentRisk(station.getId(), trafficData)
                            .subscribe(
                                    pred -> log.debug("위험도 예측 완료: station={}, level={}",
                                            station.getId(), pred.getRiskLevel()),
                                    err -> log.error("위험도 예측 실패: station={}, err={}",
                                            station.getId(), err.getMessage())
                            )
            );
        });
    }
}
