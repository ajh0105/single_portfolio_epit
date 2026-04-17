package com.epit.repository;

import com.epit.domain.monitoring.ChargerSensorData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChargerSensorDataRepository extends JpaRepository<ChargerSensorData, Long> {

    @Query("""
           SELECT d FROM ChargerSensorData d
           WHERE d.charger.id = :chargerId
             AND d.recordedAt BETWEEN :from AND :to
           ORDER BY d.recordedAt ASC
           """)
    List<ChargerSensorData> findByChargerIdAndTimeRange(@Param("chargerId") Long chargerId,
                                                        @Param("from") LocalDateTime from,
                                                        @Param("to") LocalDateTime to);

    @Query("""
           SELECT d FROM ChargerSensorData d
           WHERE d.charger.id = :chargerId
           ORDER BY d.recordedAt DESC
           """)
    List<ChargerSensorData> findLatestByChargerId(@Param("chargerId") Long chargerId,
                                                  Pageable pageable);
}
