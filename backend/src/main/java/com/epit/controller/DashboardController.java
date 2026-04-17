package com.epit.controller;

import com.epit.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/kpi")
    public ResponseEntity<DashboardService.DashboardKpi> getKpi() {
        return ResponseEntity.ok(dashboardService.getGlobalKpi());
    }

    @GetMapping("/critical-alerts")
    public ResponseEntity<?> getCriticalAlerts() {
        return ResponseEntity.ok(dashboardService.getRecentCriticalAlerts());
    }
}
