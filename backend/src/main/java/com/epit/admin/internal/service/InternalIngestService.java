package com.epit.admin.internal.service;

import com.epit.admin.alert.dto.AlertCreateCommand;
import com.epit.admin.alert.entity.Alert;
import com.epit.admin.alert.service.AlertService;
import com.epit.admin.charger.entity.Charger;
import com.epit.admin.charger.repository.ChargerRepository;
import com.epit.admin.global.exception.EntityNotFoundException;
import com.epit.admin.global.exception.ErrorCode;
import com.epit.admin.global.websocket.WebSocketEventPublisher;
import com.epit.admin.internal.dto.PhmResultRequest;
import com.epit.admin.internal.dto.TrafficResultRequest;
import com.epit.admin.internal.dto.VisionResultRequest;
import com.epit.admin.phm.entity.EquipmentHealth;
import com.epit.admin.phm.entity.FailurePrediction;
import com.epit.admin.phm.repository.EquipmentHealthRepository;
import com.epit.admin.phm.repository.FailurePredictionRepository;
import com.epit.admin.station.entity.ChargingStation;
import com.epit.admin.station.repository.ChargingStationRepository;
import com.epit.admin.traffic.entity.RiskPrediction;
import com.epit.admin.traffic.entity.TrafficData;
import com.epit.admin.traffic.repository.RiskPredictionRepository;
import com.epit.admin.traffic.repository.TrafficDataRepository;
import com.epit.admin.vision.entity.ParkingViolation;
import com.epit.admin.vision.entity.VehicleDetection;
import com.epit.admin.vision.repository.ParkingViolationRepository;
import com.epit.admin.vision.repository.VehicleDetectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalIngestService {

    private final ChargingStationRepository stationRepository;
    private final ChargerRepository chargerRepository;
    private final VehicleDetectionRepository detectionRepository;
    private final ParkingViolationRepository violationRepository;
    private final EquipmentHealthRepository healthRepository;
    private final FailurePredictionRepository predictionRepository;
    private final TrafficDataRepository trafficDataRepository;
    private final RiskPredictionRepository riskPredictionRepository;
    private final AlertService alertService;
    private final WebSocketEventPublisher wsPublisher;

    @Transactional
    public void processVisionResult(VisionResultRequest req) {
        ChargingStation station = stationRepository.findById(req.getStationId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.STATION_NOT_FOUND));

        VehicleDetection detection = VehicleDetection.builder()
                .station(station)
                .detectedAt(req.getDetectedAt())
                .vehicleType(req.getVehicleType())
                .isElectric(req.getIsElectric())
                .plateNumber(req.getPlateNumber())
                .confidence(BigDecimal.valueOf(req.getConfidence()))
                .boundingBox(req.getBoundingBox())
                .imagePath(req.getImagePath())
                .build();
        VehicleDetection saved = detectionRepository.save(detection);

        // 비전기차가 충전소 자리 점유 → 위반
        if (!req.getIsElectric()) {
            ParkingViolation violation = ParkingViolation.builder()
                    .detection(saved)
                    .station(station)
                    .plateNumber(req.getPlateNumber() != null ? req.getPlateNumber() : "미인식")
                    .violationType(ParkingViolation.ViolationType.NON_EV_OCCUPANCY)
                    .status(ParkingViolation.ViolationStatus.DETECTED)
                    .occurredAt(req.getDetectedAt())
                    .evidenceImagePath(req.getImagePath())
                    .build();
            violationRepository.save(violation);

            alertService.create(AlertCreateCommand.builder()
                    .station(station)
                    .alertType(Alert.AlertType.PARKING_VIOLATION)
                    .severity(Alert.Severity.WARNING)
                    .title("[위반] 비전기차 충전 구역 점유")
                    .message(String.format("충전소 %s에서 비전기차(%s) 불법 주차 감지",
                            station.getName(), req.getPlateNumber() != null ? req.getPlateNumber() : "번호판 미인식"))
                    .referenceType("ParkingViolation")
                    .referenceId(violation.getId())
                    .build());

            wsPublisher.publish("/topic/stations/" + station.getId() + "/violations", saved);
        }
    }

    @Transactional
    public void processPhmResult(PhmResultRequest req) {
        Charger charger = chargerRepository.findById(req.getChargerId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CHARGER_NOT_FOUND));

        // 원시 센서 데이터 저장
        if (req.getRawSeries() != null) {
            req.getRawSeries().forEach(point -> healthRepository.save(
                    EquipmentHealth.builder()
                            .charger(charger)
                            .measuredAt(point.getMeasuredAt())
                            .voltage(BigDecimal.valueOf(point.getVoltage()))
                            .current(BigDecimal.valueOf(point.getCurrent()))
                            .temperature(BigDecimal.valueOf(point.getTemperature()))
                            .vibration(BigDecimal.valueOf(point.getVibration()))
                            .build()));
        }

        FailurePrediction prediction = FailurePrediction.builder()
                .charger(charger)
                .predictedAt(req.getPredictedAt())
                .failureProbability(BigDecimal.valueOf(req.getFailureProbability()))
                .remainingUsefulLifeHours(req.getRemainingUsefulLifeHours())
                .riskLevel(req.getRiskLevel())
                .predictedComponent(req.getPredictedComponent())
                .modelVersion(req.getModelVersion())
                .build();
        FailurePrediction saved = predictionRepository.save(prediction);

        // 헬스 스코어 갱신 (100 - failure_prob * 100)
        charger.updateHealthScore(BigDecimal.valueOf(
                Math.round((1.0 - req.getFailureProbability()) * 100.0 * 100.0) / 100.0));

        wsPublisher.publish("/topic/chargers/" + charger.getId() + "/prediction", saved);

        if (req.getRiskLevel() != FailurePrediction.RiskLevel.NORMAL) {
            Alert.Severity severity = req.getRiskLevel() == FailurePrediction.RiskLevel.CRITICAL
                    ? Alert.Severity.CRITICAL : Alert.Severity.WARNING;
            alertService.create(AlertCreateCommand.builder()
                    .station(charger.getStation())
                    .alertType(Alert.AlertType.EQUIPMENT_FAILURE)
                    .severity(severity)
                    .title(String.format("[PHM] 충전기 %s 고장 위험 %s", charger.getChargerCode(), req.getRiskLevel()))
                    .message(String.format("고장 확률 %.1f%%, 잔여 수명 %s시간. 예상 부품: %s",
                            req.getFailureProbability() * 100,
                            req.getRemainingUsefulLifeHours() != null ? req.getRemainingUsefulLifeHours() : "미상",
                            req.getPredictedComponent() != null ? req.getPredictedComponent() : "미상"))
                    .referenceType("FailurePrediction")
                    .referenceId(saved.getId())
                    .build());
        }
    }

    @Transactional
    public void processTrafficResult(TrafficResultRequest req) {
        ChargingStation station = stationRepository.findById(req.getStationId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.STATION_NOT_FOUND));

        TrafficResultRequest.TrafficDataPayload td = req.getTrafficData();
        TrafficData trafficData = TrafficData.builder()
                .station(station)
                .collectedAt(td.getCollectedAt() != null ? td.getCollectedAt() : LocalDateTime.now())
                .vehicleCount(td.getVehicleCount())
                .avgSpeedKmh(BigDecimal.valueOf(td.getAvgSpeedKmh()))
                .congestionLevel(td.getCongestionLevel())
                .weather(td.getWeather())
                .roadSurface(td.getRoadSurface())
                .build();
        TrafficData savedTraffic = trafficDataRepository.save(trafficData);

        TrafficResultRequest.RiskPayload rp = req.getRisk();
        RiskPrediction risk = RiskPrediction.builder()
                .trafficData(savedTraffic)
                .station(station)
                .predictedAt(LocalDateTime.now())
                .riskLevel(rp.getRiskLevel())
                .riskScore(BigDecimal.valueOf(rp.getRiskScore()))
                .contributingFactors(rp.getContributingFactors())
                .modelVersion(rp.getModelVersion())
                .build();
        RiskPrediction savedRisk = riskPredictionRepository.save(risk);

        wsPublisher.publish("/topic/stations/" + station.getId() + "/risk", savedRisk);

        if (rp.getRiskLevel() == RiskPrediction.RiskLevel.HIGH
                || rp.getRiskLevel() == RiskPrediction.RiskLevel.CRITICAL) {
            Alert.Severity severity = rp.getRiskLevel() == RiskPrediction.RiskLevel.CRITICAL
                    ? Alert.Severity.CRITICAL : Alert.Severity.WARNING;
            alertService.create(AlertCreateCommand.builder()
                    .station(station)
                    .alertType(Alert.AlertType.TRAFFIC_RISK)
                    .severity(severity)
                    .title(String.format("[교통] %s 충전소 인근 사고 위험 %s", station.getName(), rp.getRiskLevel()))
                    .message(String.format("위험도 점수 %.2f. 차량수: %d, 평균속도: %.1fkm/h",
                            rp.getRiskScore(), td.getVehicleCount(), td.getAvgSpeedKmh()))
                    .referenceType("RiskPrediction")
                    .referenceId(savedRisk.getId())
                    .build());
        }
    }
}
