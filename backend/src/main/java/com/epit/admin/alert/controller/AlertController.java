package com.epit.admin.alert.controller;

import com.epit.admin.alert.dto.AlertResponse;
import com.epit.admin.alert.service.AlertService;
import com.epit.admin.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @PreAuthorize("hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<Page<AlertResponse>>> list(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AlertResponse> result = unreadOnly
                ? alertService.findUnread(pageable)
                : alertService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> unreadCount() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of("count", alertService.countUnread())));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<AlertResponse>> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(alertService.markRead(id)));
    }

    @PatchMapping("/read-all")
    @PreAuthorize("hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllRead() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of("updated", alertService.markAllRead())));
    }
}
