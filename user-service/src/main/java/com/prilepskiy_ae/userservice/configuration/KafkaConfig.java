package com.prilepskiy_ae.userservice.configuration;

import com.prilepskiy_ae.common.UserEventDto;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;


import java.util.Map;

import java.util.HashMap;

@Configuration
public class KafkaConfig {

    @Bean
    public KafkaTemplate<String, UserEventDto> kafkaTemplate(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.producer.key-serializer}") String deserializer,
            @Value("${spring.kafka.producer.value-serializer}") String jacksonJsonDeserializer,
            @Value("${spring.kafka.producer.properties.spring.json.value.default.type}") String type
    ) {

        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, deserializer);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, jacksonJsonDeserializer);
        props.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, type);
        ProducerFactory<String, UserEventDto> producerFactory = new DefaultKafkaProducerFactory<>(props);

        return new KafkaTemplate<>(producerFactory);
    }

}

