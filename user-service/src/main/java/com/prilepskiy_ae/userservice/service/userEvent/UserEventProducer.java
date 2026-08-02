package com.prilepskiy_ae.userservice.service.userEvent;

import com.prilepskiy_ae.common.OperationType;
import com.prilepskiy_ae.common.UserEventDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, UserEventDto> kafkaTemplate;
    private static final Logger log = LoggerFactory.getLogger(UserEventProducer.class);
    @Value("${kafka.topic.user-events}")
    private String topic;

    public CompletableFuture<SendResult<String, UserEventDto>> sendUserEvent(
            String email,
            OperationType operation
    ) {
        UserEventDto event = new UserEventDto(
                email,
                operation
        );
        return kafkaTemplate.send(topic, event);
    }
}
