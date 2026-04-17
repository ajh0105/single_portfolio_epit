package com.epit.repository;

import com.epit.domain.alert.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    Page<Alert> findByIsResolvedFalseOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
           SELECT a FROM Alert a
           WHERE a.station.id = :stationId
           ORDER BY a.createdAt DESC
           """)
    Page<Alert> findByStationId(@Param("stationId") Long stationId, Pageable pageable);

    @Query("""
           SELECT a FROM Alert a
           WHERE a.isResolved = false AND a.severity = :severity
           ORDER BY a.createdAt DESC
           """)
    List<Alert> findActiveBySeverity(@Param("severity") Alert.Severity severity);

    long countByIsResolvedFalse();

    long countByIsResolvedFalseAndSeverity(Alert.Severity severity);
}
