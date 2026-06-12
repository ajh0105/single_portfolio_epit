package com.epit.admin.vision.repository;

import com.epit.admin.vision.entity.ParkingViolation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ParkingViolationRepository extends JpaRepository<ParkingViolation, Long> {

    Page<ParkingViolation> findByStationIdAndStatusAndOccurredAtBetween(
            Long stationId, ParkingViolation.ViolationStatus status,
            LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<ParkingViolation> findByStationIdAndOccurredAtBetween(
            Long stationId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    @Query("SELECT COUNT(p) FROM ParkingViolation p WHERE p.station.id = :stationId AND p.occurredAt >= :from")
    long countTodayViolations(@Param("stationId") Long stationId, @Param("from") LocalDateTime from);

    @Query("SELECT COUNT(p) FROM ParkingViolation p WHERE p.occurredAt >= :from")
    long countAllTodayViolations(@Param("from") LocalDateTime from);
}
