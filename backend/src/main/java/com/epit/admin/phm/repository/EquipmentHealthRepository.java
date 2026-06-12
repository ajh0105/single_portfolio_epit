package com.epit.admin.phm.repository;

import com.epit.admin.phm.entity.EquipmentHealth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EquipmentHealthRepository extends JpaRepository<EquipmentHealth, Long> {

    List<EquipmentHealth> findByChargerIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
            Long chargerId, LocalDateTime from, LocalDateTime to);

    @Query("SELECT e FROM EquipmentHealth e WHERE e.charger.id = :chargerId ORDER BY e.measuredAt DESC LIMIT 1")
    Optional<EquipmentHealth> findLatestByChargerId(@Param("chargerId") Long chargerId);
}
