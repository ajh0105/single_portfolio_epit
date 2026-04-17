package com.epit.controller;

import com.epit.domain.station.ChargingStation;
import com.epit.repository.ChargingStationRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/stations")
@RequiredArgsConstructor
public class StationController {

    private final ChargingStationRepository stationRepository;

    @GetMapping
    public ResponseEntity<List<ChargingStation>> listStations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String region) {

        if (status != null) {
            return ResponseEntity.ok(
                    stationRepository.findByStatus(ChargingStation.StationStatus.valueOf(status.toUpperCase())));
        }
        return ResponseEntity.ok(stationRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChargingStation> getStation(@PathVariable Long id) {
        return stationRepository.findByIdWithChargers(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "dashboard-kpi", allEntries = true)
    public ResponseEntity<ChargingStation> createStation(@Valid @RequestBody CreateStationRequest req) {
        ChargingStation station = ChargingStation.builder()
                .name(req.name())
                .address(req.address())
                .latitude(req.latitude())
                .longitude(req.longitude())
                .region(req.region())
                .build();

        ChargingStation saved = stationRepository.save(station);
        return ResponseEntity.created(URI.create("/api/stations/" + saved.getId())).body(saved);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ChargingStation> updateStatus(@PathVariable Long id,
                                                        @RequestParam String status) {
        return stationRepository.findById(id)
                .map(station -> {
                    station.updateStatus(ChargingStation.StationStatus.valueOf(status.toUpperCase()));
                    return ResponseEntity.ok(stationRepository.save(station));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public record CreateStationRequest(
            @NotBlank String name,
            @NotBlank String address,
            @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
            @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude,
            String region
    ) {}
}
