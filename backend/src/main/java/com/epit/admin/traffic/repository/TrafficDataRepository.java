package com.epit.admin.traffic.repository;

import com.epit.admin.traffic.entity.TrafficData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TrafficDataRepository extends JpaRepository<TrafficData, Long> {
    List<TrafficData> findByStationIdAndCollectedAtBetweenOrderByCollectedAtAsc(
            Long stationId, LocalDateTime from, LocalDateTime to);
}
