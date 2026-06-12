package com.epit.admin.traffic.controller;

import com.epit.admin.global.common.ApiResponse;
import com.epit.admin.traffic.dto.RiskPredictionResponse;
import com.epit.admin.traffic.repository.RiskPredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stations")
@RequiredArgsConstructor
public class TrafficController {

    private final RiskPredictionRepository riskPredictionRepository;

    @GetMapping("/{stationId}/risk/latest")
    @PreAuthorize("hasRole('VIEWER')")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<RiskPredictionResponse>> latestRisk(@PathVariable Long stationId) {
        return riskPredictionRepository.findLatestByStationId(stationId)
                .map(r -> ResponseEntity.ok(ApiResponse.ok(RiskPredictionResponse.from(r))))
                .orElse(ResponseEntity.ok(ApiResponse.ok()));
    }
}
