package com.epit.admin.vision.service;

import com.epit.admin.vision.dto.ViolationResponse;
import com.epit.admin.vision.entity.ParkingViolation;
import com.epit.admin.vision.repository.ParkingViolationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ViolationService {

    private final ParkingViolationRepository violationRepository;

    public Page<ViolationResponse> findAll(Pageable pageable) {
        return violationRepository.findAll(pageable).map(ViolationResponse::from);
    }

    @Transactional
    public ViolationResponse resolve(Long id) {
        ParkingViolation v = violationRepository.findById(id)
                .orElseThrow(() -> new com.epit.admin.global.exception.BusinessException(
                        com.epit.admin.global.exception.ErrorCode.ENTITY_NOT_FOUND));
        v.updateStatus(ParkingViolation.ViolationStatus.RESOLVED);
        return ViolationResponse.from(v);
    }
}
