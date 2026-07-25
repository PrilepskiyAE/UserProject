package com.prilepskiy_ae.userservice.service.userEvent;

import com.prilepskiy_ae.userservice.dto.event.OperationType;
import com.prilepskiy_ae.userservice.dto.event.UserEventDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, UserEventDto> kafkaTemplate;
    private static final Logger log = LoggerFactory.getLogger(UserEventProducer.class);
    @Value("${kafka.topic.user-events}")
    private String topic;

    public void sendUserEvent(String email, OperationType operation) {
        log.info("[Producer] New '{}' event for user '{}' dispatched to topic '{}'", operation, email, topic);
        var event = new UserEventDto(email, operation);
        kafkaTemplate.send(topic, event);
    }
}
