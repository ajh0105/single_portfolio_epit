package com.epit.admin.charger.repository;

import com.epit.admin.charger.entity.Charger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChargerRepository extends JpaRepository<Charger, Long> {
    List<Charger> findByStationId(Long stationId);
    boolean existsByChargerCode(String chargerCode);

    @Query("SELECT COUNT(c) FROM Charger c WHERE c.station.id = :stationId AND c.status = 'AVAILABLE'")
    long countAvailableByStationId(@Param("stationId") Long stationId);

    @Query("SELECT COUNT(c) FROM Charger c WHERE c.station.id = :stationId AND c.status = 'FAULT'")
    long countFaultByStationId(@Param("stationId") Long stationId);
}
