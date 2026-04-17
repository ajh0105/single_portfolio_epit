package com.epit.service;

import com.epit.domain.alert.Alert;
import com.epit.domain.monitoring.ChargerSensorData;
import com.epit.domain.monitoring.EquipmentFailurePrediction;
import com.epit.domain.station.Charger;
import com.epit.domain.traffic.AccidentRiskPrediction;
import com.epit.domain.traffic.TrafficData;
import com.epit.domain.vehicle.ParkingViolation;
import com.epit.domain.vehicle.Vehicle;
import com.epit.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Spring Boot ↔ FastAPI AI 서버 통합 서비스.
 *
 * ┌──────────────────────────────────────────────────────────┐
 * │  통신 흐름 (세 가지 패턴)                                  │
 * │                                                          │
 * │  1. [동기 REST] 단건 예측                                  │
 * │     Spring Boot ──POST /detect/parking──► FastAPI        │
 * │                  ◄── JSON 예측 결과 ──                    │
 * │                                                          │
 * │  2. [비동기 @Async] 배치 센서 분석                          │
 * │     Spring Boot ──POST /predict/equipment──► FastAPI     │
 * │                  (응답 기다리지 않고 WebSocket 브로드캐스트) │
 * │                                                          │
 * │  3. [스케줄러 + WebClient] 주기적 위험도 갱신               │
 * │     @Scheduled ──GET /predict/risk/{stationId}──► FastAPI│
 * │              DB 저장 후 WebSocket → 프론트                 │
 * └──────────────────────────────────────────────────────────┘
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiIntegrationService {

    @Qualifier("aiServerWebClient")
    private final WebClient aiServerWebClient;

    private final ChargerRepository chargerRepository;
    private final ChargerSensorDataRepository sensorDataRepository;
    private final EquipmentFailurePredictionRepository predictionRepository;
    private final AccidentRiskPredictionRepository riskPredictionRepository;
    private final AlertRepository alertRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ─────────────────────────────────────────────────────────────
    //  [패턴 1] 동기 REST 호출 — 번호판 인식 + 주차 위반 감지
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public ParkingDetectionResult detectParkingViolation(Long cameraId, byte[] imageBytes) {
        String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);

        ParkingDetectionResult result = aiServerWebClient.post()
                .uri("/detect/parking")
                .bodyValue(Map.of(
                        "camera_id", cameraId,
                        "image_base64", base64Image,
                        "timestamp", LocalDateTime.now().toString()
                ))
                .retrieve()
                .bodyToMono(ParkingDetectionResult.class)
                .block(); // 동기 호출: 즉각적인 감지 결과 필요

        if (result != null && result.isViolationDetected()) {
            broadcastViolationAlert(cameraId, result);
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────
    //  [패턴 2] 비동기 호출 — 센서 데이터 기반 장비 고장 예측
    // ─────────────────────────────────────────────────────────────

    @Async
    @Transactional
    public void predictEquipmentFailureAsync(Long chargerId) {
        Charger charger = chargerRepository.findById(chargerId)
                .orElseThrow(() -> new IllegalArgumentException("Charger not found: " + chargerId));

        List<ChargerSensorData> recentSensorData = sensorDataRepository
                .findLatestByChargerId(chargerId, org.springframework.data.domain.PageRequest.of(0, 100));

        aiServerWebClient.post()
                .uri("/predict/equipment")
                .bodyValue(Map.of(
                        "charger_id", chargerId,
                        "sensor_readings", recentSensorData.stream()
                                .map(this::toSensorPayload)
                                .toList()
                ))
                .retrieve()
                .bodyToMono(EquipmentPredictionResult.class)
                .subscribe(result -> handleEquipmentPrediction(charger, result),
                           err -> log.error("Equipment prediction failed for charger {}: {}", chargerId, err.getMessage()));
    }

    private void handleEquipmentPrediction(Charger charger, EquipmentPredictionResult result) {
        if (result == null) return;

        EquipmentFailurePrediction prediction = EquipmentFailurePrediction.builder()
                .charger(charger)
                .failureProbability(BigDecimal.valueOf(result.failureProbability()))
                .failureType(mapFailureType(result.failureType()))
                .predictedFailureAt(result.predictedFailureAt())
                .confidenceScore(BigDecimal.valueOf(result.confidenceScore()))
                .recommendation(result.recommendation())
                .modelVersion(result.modelVersion())
                .build();

        predictionRepository.save(prediction);

        if (result.failureProbability() >= 0.7) {
            Alert alert = Alert.builder()
                    .alertType(Alert.AlertType.EQUIPMENT_FAULT)
                    .severity(result.failureProbability() >= 0.9
                              ? Alert.Severity.CRITICAL : Alert.Severity.WARNING)
                    .title("장비 고장 위험 감지: 충전기 #" + charger.getChargerNumber())
                    .message(result.recommendation())
                    .entityType("CHARGER")
                    .entityId(charger.getId())
                    .station(charger.getStation())
                    .build();

            alertRepository.save(alert);
            messagingTemplate.convertAndSend("/topic/alerts", AlertPayload.from(alert));
            messagingTemplate.convertAndSend(
                    "/topic/stations/" + charger.getStation().getId(), alert);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  [패턴 3] WebClient Mono 체이닝 — 교통 위험도 예측
    // ─────────────────────────────────────────────────────────────

    public Mono<AccidentRiskPrediction> predictAccidentRisk(Long stationId, TrafficData trafficData) {
        return aiServerWebClient.post()
                .uri("/predict/accident-risk")
                .bodyValue(Map.of(
                        "station_id", stationId,
                        "traffic_volume", trafficData.getTrafficVolume(),
                        "avg_speed_kmh", trafficData.getAvgSpeedKmh(),
                        "congestion_level", trafficData.getCongestionLevel(),
                        "weather_condition", trafficData.getWeatherCondition(),
                        "visibility_m", trafficData.getVisibilityM(),
                        "road_surface_condition", trafficData.getRoadSurfaceCondition()
                ))
                .retrieve()
                .bodyToMono(RiskPredictionResult.class)
                .map(result -> AccidentRiskPrediction.builder()
                        .station(trafficData.getStation())
                        .riskLevel(AccidentRiskPrediction.RiskLevel.valueOf(result.riskLevel()))
                        .riskScore(BigDecimal.valueOf(result.riskScore()))
                        .primaryRiskFactor(result.primaryRiskFactor())
                        .riskFactors(result.riskFactorsJson())
                        .validUntil(LocalDateTime.now().plusHours(1))
                        .modelVersion(result.modelVersion())
                        .build())
                .doOnNext(prediction -> {
                    riskPredictionRepository.save(prediction);
                    if (prediction.getRiskLevel() == AccidentRiskPrediction.RiskLevel.CRITICAL
                            || prediction.getRiskLevel() == AccidentRiskPrediction.RiskLevel.HIGH) {
                        broadcastRiskAlert(prediction);
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────
    //  내부 헬퍼
    // ─────────────────────────────────────────────────────────────

    private void broadcastViolationAlert(Long cameraId, ParkingDetectionResult result) {
        messagingTemplate.convertAndSend("/topic/violations", Map.of(
                "cameraId", cameraId,
                "licensePlate", result.licensePlate(),
                "vehicleType", result.vehicleType(),
                "confidence", result.confidence(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    private void broadcastRiskAlert(AccidentRiskPrediction prediction) {
        Alert alert = Alert.builder()
                .alertType(Alert.AlertType.ACCIDENT_RISK)
                .severity(prediction.getRiskLevel() == AccidentRiskPrediction.RiskLevel.CRITICAL
                          ? Alert.Severity.CRITICAL : Alert.Severity.WARNING)
                .title("교통사고 위험 " + prediction.getRiskLevel() + ": " + prediction.getStation().getName())
                .message("위험도 점수: " + prediction.getRiskScore() + " — " + prediction.getPrimaryRiskFactor())
                .entityType("STATION")
                .entityId(prediction.getStation().getId())
                .station(prediction.getStation())
                .build();

        alertRepository.save(alert);
        messagingTemplate.convertAndSend("/topic/alerts", AlertPayload.from(alert));
    }

    private Map<String, Object> toSensorPayload(ChargerSensorData data) {
        return Map.of(
                "recorded_at", data.getRecordedAt().toString(),
                "voltage", data.getVoltage() != null ? data.getVoltage() : 0,
                "current_ampere", data.getCurrentAmpere() != null ? data.getCurrentAmpere() : 0,
                "temperature_celsius", data.getTemperatureCelsius() != null ? data.getTemperatureCelsius() : 0,
                "power_output_kw", data.getPowerOutputKw() != null ? data.getPowerOutputKw() : 0
        );
    }

    private EquipmentFailurePrediction.FailureType mapFailureType(String type) {
        try {
            return EquipmentFailurePrediction.FailureType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            return EquipmentFailurePrediction.FailureType.COMMUNICATION_ERROR;
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  내부 DTO 레코드 (FastAPI 응답 매핑용)
    // ─────────────────────────────────────────────────────────────

    public record ParkingDetectionResult(
            boolean violationDetected,
            String licensePlate,
            String vehicleType,
            double confidence,
            String imageUrl
    ) {
        boolean isViolationDetected() { return violationDetected && "NON_EV".equals(vehicleType); }
    }

    public record EquipmentPredictionResult(
            double failureProbability,
            String failureType,
            LocalDateTime predictedFailureAt,
            double confidenceScore,
            String recommendation,
            String modelVersion
    ) {}

    public record RiskPredictionResult(
            String riskLevel,
            double riskScore,
            String primaryRiskFactor,
            String riskFactorsJson,
            String modelVersion
    ) {}

    public record AlertPayload(
            Long id,
            String alertType,
            String severity,
            String title,
            String message,
            String createdAt
    ) {
        static AlertPayload from(Alert alert) {
            return new AlertPayload(
                    alert.getId(),
                    alert.getAlertType().name(),
                    alert.getSeverity().name(),
                    alert.getTitle(),
                    alert.getMessage(),
                    alert.getCreatedAt().toString()
            );
        }
    }
}
