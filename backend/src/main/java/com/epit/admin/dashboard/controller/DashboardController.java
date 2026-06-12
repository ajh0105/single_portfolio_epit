package com.epit.admin.dashboard.controller;

import com.epit.admin.dashboard.dto.DashboardOverviewResponse;
import com.epit.admin.dashboard.service.DashboardService;
import com.epit.admin.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<DashboardOverviewResponse>> overview() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getOverview()));
    }
}
