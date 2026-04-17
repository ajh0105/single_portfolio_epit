package com.epit.repository;

import com.epit.domain.station.Charger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChargerRepository extends JpaRepository<Charger, Long> {

    List<Charger> findByStationId(Long stationId);

    List<Charger> findByStatus(Charger.ChargerStatus status);

    @Query("SELECT c FROM Charger c WHERE c.station.id = :stationId AND c.status = :status")
    List<Charger> findByStationIdAndStatus(@Param("stationId") Long stationId,
                                           @Param("status") Charger.ChargerStatus status);

    @Query("SELECT COUNT(c) FROM Charger c WHERE c.station.id = :stationId AND c.status = 'FAULT'")
    long countFaultByStationId(@Param("stationId") Long stationId);
}
