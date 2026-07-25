package com.prilepskiy_ae.notificationservice.listener;

import com.prilepskiy_ae.notificationservice.dto.event.UserEventDto;
import com.prilepskiy_ae.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final NotificationService notificationService;

    // 👇 Топик прописан явно — это надёжно и прозрачно
    @KafkaListener(topics = "user-events", groupId = "notification-group")
    public void handleUserEvent(UserEventDto event) {
        // 👇 Этот лог — главный индикатор: если его нет, значит, сообщение не доходит
        log.info(">>> [KAFKA] Получено событие: email={}, operation={}",
                event.getEmail(), event.getOperation());

        try {
            notificationService.sendNotification(event.getEmail(), event.getOperation());
            log.debug("Notification sent successfully for email={}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send notification for email={}, operation={}",
                    event.getEmail(), event.getOperation(), e);
            throw new RuntimeException("Notification failed", e);
        }
    }
}