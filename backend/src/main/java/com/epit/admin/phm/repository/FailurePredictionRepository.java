package com.epit.admin.phm.repository;

import com.epit.admin.phm.entity.FailurePrediction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FailurePredictionRepository extends JpaRepository<FailurePrediction, Long> {

    Page<FailurePrediction> findByChargerIdOrderByPredictedAtDesc(Long chargerId, Pageable pageable);

    @Query("SELECT f FROM FailurePrediction f WHERE f.charger.id = :chargerId ORDER BY f.predictedAt DESC LIMIT 1")
    Optional<FailurePrediction> findLatestByChargerId(@Param("chargerId") Long chargerId);

    @Query("SELECT f FROM FailurePrediction f JOIN FETCH f.charger WHERE f.riskLevel IN ('CRITICAL', 'WARNING') ORDER BY f.failureProbability DESC, f.predictedAt DESC")
    List<FailurePrediction> findAllCritical();

    @Query("SELECT COUNT(DISTINCT f.charger.id) FROM FailurePrediction f WHERE f.riskLevel = 'CRITICAL'")
    long countCriticalChargers();
}
