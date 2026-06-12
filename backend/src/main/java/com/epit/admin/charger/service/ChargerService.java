package com.epit.admin.charger.service;

import com.epit.admin.charger.dto.ChargerCreateRequest;
import com.epit.admin.charger.dto.ChargerResponse;
import com.epit.admin.charger.entity.Charger;
import com.epit.admin.charger.repository.ChargerRepository;
import com.epit.admin.global.exception.BusinessException;
import com.epit.admin.global.exception.EntityNotFoundException;
import com.epit.admin.global.exception.ErrorCode;
import com.epit.admin.station.entity.ChargingStation;
import com.epit.admin.station.repository.ChargingStationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChargerService {

    private final ChargerRepository chargerRepository;
    private final ChargingStationRepository stationRepository;

    @Transactional(readOnly = true)
    public Page<ChargerResponse> findAll(Pageable pageable) {
        return chargerRepository.findAll(pageable).map(ChargerResponse::from);
    }

    @Transactional(readOnly = true)
    public List<ChargerResponse> findByStation(Long stationId) {
        return chargerRepository.findByStationId(stationId)
                .stream().map(ChargerResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ChargerResponse findById(Long id) {
        return ChargerResponse.from(getCharger(id));
    }

    @Transactional
    public ChargerResponse create(ChargerCreateRequest req) {
        if (chargerRepository.existsByChargerCode(req.getChargerCode())) {
            throw new BusinessException(ErrorCode.DUPLICATE_CHARGER_CODE);
        }
        ChargingStation station = stationRepository.findById(req.getStationId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.STATION_NOT_FOUND));

        Charger charger = Charger.builder()
                .station(station)
                .chargerCode(req.getChargerCode())
                .connectorType(req.getConnectorType())
                .maxPowerKw(req.getMaxPowerKw())
                .status(req.getStatus() != null ? req.getStatus() : Charger.ChargerStatus.AVAILABLE)
                .build();
        station.incrementTotalChargers();
        return ChargerResponse.from(chargerRepository.save(charger));
    }

    @Transactional
    public ChargerResponse updateStatus(Long id, Charger.ChargerStatus status) {
        Charger charger = getCharger(id);
        charger.updateStatus(status);
        return ChargerResponse.from(charger);
    }

    private Charger getCharger(Long id) {
        return chargerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CHARGER_NOT_FOUND));
    }
}
