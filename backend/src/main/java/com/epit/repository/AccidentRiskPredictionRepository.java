package com.epit.repository;

import com.epit.domain.traffic.AccidentRiskPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AccidentRiskPredictionRepository extends JpaRepository<AccidentRiskPrediction, Long> {

    @Query("""
           SELECT p FROM AccidentRiskPrediction p
           WHERE p.station.id = :stationId
             AND (p.validUntil IS NULL OR p.validUntil > :now)
           ORDER BY p.predictedAt DESC
           """)
    Optional<AccidentRiskPrediction> findLatestValidByStationId(@Param("stationId") Long stationId,
                                                                 @Param("now") LocalDateTime now);
}
