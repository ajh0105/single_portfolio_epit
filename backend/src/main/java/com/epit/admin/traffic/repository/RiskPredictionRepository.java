package com.epit.admin.traffic.repository;

import com.epit.admin.traffic.entity.RiskPrediction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RiskPredictionRepository extends JpaRepository<RiskPrediction, Long> {

    @Query("SELECT r FROM RiskPrediction r JOIN FETCH r.station JOIN FETCH r.trafficData WHERE r.station.id = :stationId ORDER BY r.predictedAt DESC LIMIT 1")
    Optional<RiskPrediction> findLatestByStationId(@Param("stationId") Long stationId);

    Page<RiskPrediction> findByStationIdOrderByPredictedAtDesc(Long stationId, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT r.station.id) FROM RiskPrediction r WHERE r.riskLevel IN ('HIGH','CRITICAL')")
    long countHighRiskStations();
}
