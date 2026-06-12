package com.epit.admin.station.repository;

import com.epit.admin.station.entity.ChargingStation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChargingStationRepository extends JpaRepository<ChargingStation, Long> {
    boolean existsByStationCode(String stationCode);
    Optional<ChargingStation> findByStationCode(String stationCode);
    Page<ChargingStation> findByOperationStatus(ChargingStation.OperationStatus status, Pageable pageable);
    Optional<ChargingStation> findByCameraDeviceId(String cameraDeviceId);
}
