package com.prilepskiy_ae.userservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prilepskiy_ae.common.OperationType;
import com.prilepskiy_ae.common.UserEventDto;
import com.prilepskiy_ae.userservice.BaseTest;
import com.prilepskiy_ae.userservice.UserServiceApplication;
import com.prilepskiy_ae.userservice.dto.user.UserRequest;
import io.qameta.allure.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.apache.kafka.common.serialization.StringSerializer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Epic("User API")
@Feature("Kafka Producer")
@Owner("Prilepskiy Alex")
@SpringBootTest(classes = UserServiceApplication.class)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Story("Kafka Producer Integration")
public class UserEventProducerIntegrationTest extends BaseTest {

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));


    @Autowired
    private WebApplicationContext context;

    @DynamicPropertySource
    static void overrideKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    private final String uniqueEmail = "test-" + System.nanoTime() + "@example.com";
    private ObjectMapper objectMapper;
    private MockMvc mockMvc;
    private KafkaTemplate<String, UserEventDto> kafkaTemplate;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);

        var producerFactory = new DefaultKafkaProducerFactory<String, UserEventDto>(props);
        kafkaTemplate = new KafkaTemplate<>(producerFactory);

    }

    @Test
    @Story("Создание пользователя")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("POST /api/users должен создать пользователя, вернуть 201 и отправить событие в Kafka")
    void test1() throws Exception {
        UserRequest userRequest = new UserRequest(UPDATED_USER_NAME, uniqueEmail, USER_AGE);
        String payload = objectMapper.writeValueAsString(userRequest);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        Thread.sleep(2000);

        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + System.currentTimeMillis());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);

        try (var consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(Collections.singletonList("user-events"));

            int attempts = 0;
            int maxAttempts = 30;
            boolean messageFound = false;
            ConsumerRecord<Object, Object> foundRecord = null;

            while (attempts < maxAttempts && !messageFound) {
                var records = consumer.poll(Duration.ofSeconds(1));
                if (!records.isEmpty()) {

                    for (var record : records) {
                        if ("user-events".equals(record.topic())) {
                                UserEventDto event = objectMapper.readValue((String) record.value(), UserEventDto.class);
                                if (uniqueEmail.equals(event.email())) {
                                    foundRecord = record;
                                    messageFound = true;
                                    break;
                                }
                        }
                    }
                }
                attempts++;
            }

            if (!messageFound) {
                throw new AssertionError("Сообщение не появилось");
            }


            assertThat(foundRecord).isNotNull();
            assertThat(foundRecord.topic()).isEqualTo("user-events");

            String jsonPayload = (String) foundRecord.value();
            UserEventDto receivedEvent = objectMapper.readValue(jsonPayload, UserEventDto.class);

            assertThat(receivedEvent.email()).isEqualTo(uniqueEmail);
            assertThat(receivedEvent.operation()).isEqualTo(OperationType.CREATED);


        }
    }

    @Test
    @Story("Kafka")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("kafkaTemplate должен успешно отправить UserEventDto в топик")
    void test2() throws Exception {
        String testKey = "user-123";
        UserEventDto testEvent = new UserEventDto("test@example.com", OperationType.CREATED);
        CompletableFuture<SendResult<String, UserEventDto>> future = kafkaTemplate.send("user-events", testKey, testEvent);
        SendResult<String, UserEventDto> result = future.get();
        assertThat(result.getRecordMetadata().topic()).isEqualTo("user-events");
        assertThat(result.getRecordMetadata().partition()).isGreaterThanOrEqualTo(0);
    }
}
