package com.epit.repository;

import com.epit.domain.monitoring.EquipmentFailurePrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EquipmentFailurePredictionRepository extends JpaRepository<EquipmentFailurePrediction, Long> {

    @Query("""
           SELECT p FROM EquipmentFailurePrediction p
           WHERE p.charger.id = :chargerId AND p.status = 'ACTIVE'
           ORDER BY p.predictedAt DESC
           """)
    Optional<EquipmentFailurePrediction> findLatestActiveByChargerId(@Param("chargerId") Long chargerId);

    @Query("""
           SELECT p FROM EquipmentFailurePrediction p
           WHERE p.charger.station.id = :stationId
             AND p.status = 'ACTIVE'
             AND p.failureProbability >= :threshold
           ORDER BY p.failureProbability DESC
           """)
    List<EquipmentFailurePrediction> findHighRiskByStation(@Param("stationId") Long stationId,
                                                           @Param("threshold") java.math.BigDecimal threshold);
}
