package com.epit.admin.charger.controller;

import com.epit.admin.charger.dto.ChargerCreateRequest;
import com.epit.admin.charger.dto.ChargerResponse;
import com.epit.admin.charger.dto.EquipmentHealthResponse;
import com.epit.admin.charger.entity.Charger;
import com.epit.admin.phm.repository.EquipmentHealthRepository;
import com.epit.admin.charger.service.ChargerService;
import com.epit.admin.global.common.ApiResponse;
import com.epit.admin.global.exception.BusinessException;
import com.epit.admin.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chargers")
@RequiredArgsConstructor
public class ChargerController {

    private final ChargerService chargerService;
    private final EquipmentHealthRepository healthRepository;

    @GetMapping
    @PreAuthorize("hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<Page<ChargerResponse>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(chargerService.findAll(pageable)));
    }

    @GetMapping("/station/{stationId}")
    @PreAuthorize("hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<List<ChargerResponse>>> byStation(@PathVariable Long stationId) {
        return ResponseEntity.ok(ApiResponse.ok(chargerService.findByStation(stationId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<ChargerResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(chargerService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ChargerResponse>> create(@Valid @RequestBody ChargerCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(chargerService.create(req)));
    }

    @GetMapping("/{id}/health/latest")
    @PreAuthorize("hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<EquipmentHealthResponse>> latestHealth(@PathVariable Long id) {
        EquipmentHealthResponse resp = healthRepository.findLatestByChargerId(id)
                .map(EquipmentHealthResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.ok(resp));
    }

    @GetMapping("/{id}/health")
    @PreAuthorize("hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<List<EquipmentHealthResponse>>> healthHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        LocalDateTime from = startDate != null ? startDate : LocalDateTime.now().minusHours(hours);
        LocalDateTime to = endDate != null ? endDate : LocalDateTime.now();
        List<EquipmentHealthResponse> list = healthRepository
                .findByChargerIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(id, from, to)
                .stream()
                .map(EquipmentHealthResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<ChargerResponse>> updateStatus(
            @PathVariable Long id, @RequestParam Charger.ChargerStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(chargerService.updateStatus(id, status)));
    }
}
