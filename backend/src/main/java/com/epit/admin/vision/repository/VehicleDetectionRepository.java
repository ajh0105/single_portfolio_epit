package com.epit.admin.vision.repository;

import com.epit.admin.vision.entity.VehicleDetection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface VehicleDetectionRepository extends JpaRepository<VehicleDetection, Long> {

    Page<VehicleDetection> findByStationIdAndDetectedAtBetween(
            Long stationId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<VehicleDetection> findByVehicleTypeAndDetectedAtBetween(
            VehicleDetection.VehicleType type, LocalDateTime from, LocalDateTime to, Pageable pageable);

    @Query("SELECT COUNT(v) FROM VehicleDetection v WHERE v.station.id = :stationId AND v.detectedAt >= :from")
    long countByStationSince(@Param("stationId") Long stationId, @Param("from") LocalDateTime from);
}
