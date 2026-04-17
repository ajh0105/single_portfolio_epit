package com.epit.controller;

import com.epit.domain.alert.Alert;
import com.epit.domain.user.User;
import com.epit.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertRepository alertRepository;

    @GetMapping
    public ResponseEntity<Page<Alert>> getActiveAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                alertRepository.findByIsResolvedFalseOrderByCreatedAtDesc(
                        PageRequest.of(page, size)));
    }

    @GetMapping("/station/{stationId}")
    public ResponseEntity<Page<Alert>> getAlertsByStation(
            @PathVariable Long stationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                alertRepository.findByStationId(stationId,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<Alert> acknowledgeAlert(@PathVariable Long id,
                                                   @AuthenticationPrincipal User currentUser) {
        return alertRepository.findById(id)
                .map(alert -> {
                    alert.acknowledge(currentUser);
                    return ResponseEntity.ok(alertRepository.save(alert));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<Alert> resolveAlert(@PathVariable Long id) {
        return alertRepository.findById(id)
                .map(alert -> {
                    alert.resolve();
                    return ResponseEntity.ok(alertRepository.save(alert));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
