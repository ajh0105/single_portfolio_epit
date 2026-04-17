package com.epit.repository;

import com.epit.domain.vehicle.ParkingViolation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ParkingViolationRepository extends JpaRepository<ParkingViolation, Long> {

    Page<ParkingViolation> findByStatus(ParkingViolation.ViolationStatus status, Pageable pageable);

    @Query("""
           SELECT v FROM ParkingViolation v
           JOIN FETCH v.charger c
           JOIN FETCH c.station s
           WHERE s.id = :stationId
           ORDER BY v.detectedAt DESC
           """)
    Page<ParkingViolation> findByStationId(@Param("stationId") Long stationId, Pageable pageable);

    @Query("SELECT COUNT(v) FROM ParkingViolation v WHERE v.status = 'PENDING' AND v.detectedAt >= :since")
    long countPendingViolationsSince(@Param("since") LocalDateTime since);

    List<ParkingViolation> findByChargerIdOrderByDetectedAtDesc(Long chargerId);
}
