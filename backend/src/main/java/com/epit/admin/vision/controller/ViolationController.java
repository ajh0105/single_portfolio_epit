package com.epit.admin.vision.controller;

import com.epit.admin.global.common.ApiResponse;
import com.epit.admin.vision.dto.ViolationResponse;
import com.epit.admin.vision.service.ViolationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.epit.admin.vision.dto.ViolationResponse;

@RestController
@RequestMapping("/api/v1/violations")
@RequiredArgsConstructor
public class ViolationController {

    private final ViolationService violationService;

    @GetMapping
    @PreAuthorize("hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<Page<ViolationResponse>>> list(
            @PageableDefault(size = 20, sort = "occurredAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(violationService.findAll(pageable)));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<ViolationResponse>> resolve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(violationService.resolve(id)));
    }
}
