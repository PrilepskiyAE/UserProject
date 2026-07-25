package com.prilepskiy_ae.notificationservice.listener;

import com.prilepskiy_ae.common.UserEventDto;
import com.prilepskiy_ae.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "${app.kafka.topic.user-events}", groupId = "notification-group")
    public void handleUserEvent(UserEventDto event) {
        log.info(">>> [KAFKA] Получено событие: email={}, operation={}",
                event.email(), event.operation());

        try {
            notificationService.sendNotification(event.email(), event.operation());
            log.debug("Notification sent successfully for email={}", event.email());
        } catch (Exception e) {
            log.error("Failed to send notification for email={}, operation={}",
                    event.email(), event.operation(), e);
            throw new RuntimeException("Notification failed", e);
        }
    }
}