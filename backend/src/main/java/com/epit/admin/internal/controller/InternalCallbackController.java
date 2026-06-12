package com.epit.admin.internal.controller;

import com.epit.admin.global.common.ApiResponse;
import com.epit.admin.internal.dto.PhmResultRequest;
import com.epit.admin.internal.dto.TrafficResultRequest;
import com.epit.admin.internal.dto.VisionResultRequest;
import com.epit.admin.internal.service.InternalIngestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
@PreAuthorize("hasRole('INTERNAL')")
public class InternalCallbackController {

    private final InternalIngestService ingestService;

    @PostMapping("/vision/result")
    public ResponseEntity<ApiResponse<Void>> visionResult(@Valid @RequestBody VisionResultRequest req) {
        ingestService.processVisionResult(req);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/phm/result")
    public ResponseEntity<ApiResponse<Void>> phmResult(@Valid @RequestBody PhmResultRequest req) {
        ingestService.processPhmResult(req);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/traffic/result")
    public ResponseEntity<ApiResponse<Void>> trafficResult(@Valid @RequestBody TrafficResultRequest req) {
        ingestService.processTrafficResult(req);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
