package com.epit.repository;

import com.epit.domain.traffic.TrafficData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrafficDataRepository extends JpaRepository<TrafficData, Long> {

    @Query("""
           SELECT t FROM TrafficData t
           WHERE t.station.id = :stationId
           ORDER BY t.recordedAt DESC
           """)
    List<TrafficData> findLatestByStationId(@Param("stationId") Long stationId, Pageable pageable);
}
