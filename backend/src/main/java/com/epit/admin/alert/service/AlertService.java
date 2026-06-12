package com.epit.admin.alert.service;

import com.epit.admin.alert.dto.AlertCreateCommand;
import com.epit.admin.alert.dto.AlertResponse;
import com.epit.admin.alert.entity.Alert;
import com.epit.admin.alert.repository.AlertRepository;
import com.epit.admin.global.exception.EntityNotFoundException;
import com.epit.admin.global.exception.ErrorCode;
import com.epit.admin.global.websocket.WebSocketEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private static final String TOPIC_ALERTS = "/topic/alerts";

    private final AlertRepository alertRepository;
    private final WebSocketEventPublisher wsPublisher;

    @Transactional
    public AlertResponse create(AlertCreateCommand command) {
        Alert alert = Alert.builder()
                .station(command.getStation())
                .alertType(command.getAlertType())
                .severity(command.getSeverity())
                .title(command.getTitle())
                .message(command.getMessage())
                .referenceType(command.getReferenceType())
                .referenceId(command.getReferenceId())
                .isRead(false)
                .build();

        Alert saved = alertRepository.save(alert);
        AlertResponse response = AlertResponse.from(saved);

        // WebSocket 실시간 푸시 (저장 후 즉시)
        wsPublisher.publish(TOPIC_ALERTS, response);
        if (command.getStation() != null) {
            wsPublisher.publish("/topic/stations/" + command.getStation().getId() + "/alerts", response);
        }

        log.info("Alert created: type={}, severity={}, stationId={}",
                command.getAlertType(), command.getSeverity(),
                command.getStation() != null ? command.getStation().getId() : "global");
        return response;
    }

    @Transactional(readOnly = true)
    public Page<AlertResponse> findAll(Pageable pageable) {
        return alertRepository.findAll(pageable).map(AlertResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<AlertResponse> findUnread(Pageable pageable) {
        return alertRepository.findByIsReadOrderByCreatedAtDesc(false, pageable).map(AlertResponse::from);
    }

    @Transactional(readOnly = true)
    public long countUnread() {
        return alertRepository.countByIsRead(false);
    }

    @Transactional
    public AlertResponse markRead(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND));
        alertRepository.markReadById(id);
        alert.markRead();
        return AlertResponse.from(alert);
    }

    @Transactional
    public int markAllRead() {
        return alertRepository.markAllRead();
    }
}
