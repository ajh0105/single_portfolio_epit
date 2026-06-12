package com.epit.admin.station.service;

import com.epit.admin.global.exception.BusinessException;
import com.epit.admin.global.exception.EntityNotFoundException;
import com.epit.admin.global.exception.ErrorCode;
import com.epit.admin.station.dto.StationCreateRequest;
import com.epit.admin.station.dto.StationResponse;
import com.epit.admin.station.entity.ChargingStation;
import com.epit.admin.station.repository.ChargingStationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StationService {

    private final ChargingStationRepository stationRepository;

    @Transactional(readOnly = true)
    public Page<StationResponse> findAll(Pageable pageable) {
        return stationRepository.findAll(pageable).map(StationResponse::from);
    }

    @Transactional(readOnly = true)
    public StationResponse findById(Long id) {
        return StationResponse.from(getStation(id));
    }

    @Transactional
    public StationResponse create(StationCreateRequest req) {
        if (stationRepository.existsByStationCode(req.getStationCode())) {
            throw new BusinessException(ErrorCode.DUPLICATE_STATION_CODE);
        }
        ChargingStation station = ChargingStation.builder()
                .stationCode(req.getStationCode())
                .name(req.getName())
                .address(req.getAddress())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .totalChargers(0)
                .operationStatus(req.getOperationStatus())
                .cameraDeviceId(req.getCameraDeviceId())
                .build();
        return StationResponse.from(stationRepository.save(station));
    }

    @Transactional
    public StationResponse update(Long id, StationCreateRequest req) {
        ChargingStation station = getStation(id);
        station.update(req.getName(), req.getAddress(), req.getLatitude(), req.getLongitude(),
                req.getOperationStatus(), req.getCameraDeviceId());
        return StationResponse.from(station);
    }

    @Transactional
    public StationResponse updateStatus(Long id, ChargingStation.OperationStatus status) {
        ChargingStation station = getStation(id);
        station.updateOperationStatus(status);
        return StationResponse.from(station);
    }

    @Transactional
    public void delete(Long id) {
        stationRepository.delete(getStation(id));
    }

    public ChargingStation getStation(Long id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.STATION_NOT_FOUND));
    }
}
