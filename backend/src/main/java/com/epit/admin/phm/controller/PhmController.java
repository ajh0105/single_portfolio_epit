package com.epit.admin.phm.controller;

import com.epit.admin.global.common.ApiResponse;
import com.epit.admin.phm.dto.FailurePredictionResponse;
import com.epit.admin.phm.repository.FailurePredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/predictions")
@RequiredArgsConstructor
public class PhmController {

    private final FailurePredictionRepository predictionRepository;

    @GetMapping("/critical")
    @PreAuthorize("hasRole('VIEWER')")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<FailurePredictionResponse>>> critical() {
        List<FailurePredictionResponse> result = predictionRepository.findAllCritical()
                .stream()
                .map(FailurePredictionResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/charger/{chargerId}")
    @PreAuthorize("hasRole('VIEWER')")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<FailurePredictionResponse>> latestByCharger(@PathVariable Long chargerId) {
        return predictionRepository.findLatestByChargerId(chargerId)
                .map(p -> ResponseEntity.ok(ApiResponse.ok(FailurePredictionResponse.from(p))))
                .orElse(ResponseEntity.ok(ApiResponse.ok(null)));
    }
}
