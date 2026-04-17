package com.epit.repository;

import com.epit.domain.station.ChargingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChargingStationRepository extends JpaRepository<ChargingStation, Long> {

    List<ChargingStation> findByStatus(ChargingStation.StationStatus status);

    @Query("SELECT s FROM ChargingStation s WHERE s.region = :region")
    List<ChargingStation> findByRegion(@Param("region") String region);

    @Query("SELECT s FROM ChargingStation s LEFT JOIN FETCH s.chargers WHERE s.id = :id")
    java.util.Optional<ChargingStation> findByIdWithChargers(@Param("id") Long id);
}
