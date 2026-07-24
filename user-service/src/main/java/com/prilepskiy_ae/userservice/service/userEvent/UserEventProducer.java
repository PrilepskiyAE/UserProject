package com.prilepskiy_ae.userservice.service.userEvent;

import com.prilepskiy_ae.userservice.dto.event.OperationType;
import com.prilepskiy_ae.userservice.dto.event.UserEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, UserEventDto> kafkaTemplate;

    @Value("${kafka.topic.user-events}")
    private String topic;

    public void sendUserEvent(String email, OperationType operation) {
        var event = new UserEventDto(email, operation);
        kafkaTemplate.send(topic, event);
    }
}
