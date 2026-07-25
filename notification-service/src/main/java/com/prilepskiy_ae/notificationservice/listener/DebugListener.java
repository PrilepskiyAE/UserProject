package com.prilepskiy_ae.notificationservice.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DebugListener {
    private static final Logger log = LoggerFactory.getLogger(DebugListener.class);

    @KafkaListener(topics = "user-events", groupId = "debug-group")
    public void handleRaw(String msg) {
        log.info("DEBUG LISTENER: received raw message: {}", msg);
    }
}
