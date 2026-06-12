package com.epit.admin.alert.repository;

import com.epit.admin.alert.entity.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    Page<Alert> findByIsReadOrderByCreatedAtDesc(boolean isRead, Pageable pageable);

    Page<Alert> findByAlertTypeOrderByCreatedAtDesc(Alert.AlertType alertType, Pageable pageable);

    Page<Alert> findByStationIdOrderByCreatedAtDesc(Long stationId, Pageable pageable);

    long countByIsRead(boolean isRead);

    @Modifying
    @Query("UPDATE Alert a SET a.isRead = true WHERE a.id = :id")
    int markReadById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Alert a SET a.isRead = true WHERE a.isRead = false")
    int markAllRead();

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.station.id = :stationId AND a.isRead = false")
    long countUnreadByStation(@Param("stationId") Long stationId);
}
