package com.prilepskiy_ae.notificationservice.configuration;


import com.prilepskiy_ae.common.UserEventDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserEventDto> kafkaListenerContainerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.key-deserializer}") String deserializer,
            @Value("${spring.kafka.consumer.value-deserializer}") String jacksonJsonDeserializer,
            @Value("${spring.kafka.consumer.properties.spring.json.value.default.type}") String type
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, deserializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, jacksonJsonDeserializer);
        props.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE,type);
        ConcurrentKafkaListenerContainerFactory<String, UserEventDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(props));

        return factory;
    }

}

