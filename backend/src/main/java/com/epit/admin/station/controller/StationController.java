package com.epit.admin.station.controller;

import com.epit.admin.global.common.ApiResponse;
import com.epit.admin.station.dto.StationCreateRequest;
import com.epit.admin.station.dto.StationResponse;
import com.epit.admin.station.service.StationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @GetMapping
    @PreAuthorize("hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<Page<StationResponse>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(stationService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<StationResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(stationService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StationResponse>> create(@Valid @RequestBody StationCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(stationService.create(req)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StationResponse>> update(
            @PathVariable Long id, @Valid @RequestBody StationCreateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(stationService.update(id, req)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<StationResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam com.epit.admin.station.entity.ChargingStation.OperationStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(stationService.updateStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        stationService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
