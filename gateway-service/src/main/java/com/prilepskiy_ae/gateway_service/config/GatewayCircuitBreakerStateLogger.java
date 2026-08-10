package com.prilepskiy_ae.gateway_service.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayCircuitBreakerStateLogger {

    private static final Logger log =
            LoggerFactory.getLogger(GatewayCircuitBreakerStateLogger.class);

    @Bean
    ApplicationRunner circuitBreakerEventLogger(CircuitBreakerRegistry circuitBreakerRegistry) {
        return args -> {
            circuitBreakerRegistry.circuitBreaker("userServiceCircuitBreaker")
                    .getEventPublisher()
                    .onStateTransition(event ->
                            log.warn("USER-SERVICE CircuitBreaker transition: {}",
                                    event.getStateTransition())
                    )
                    .onError(event ->
                            log.warn("USER-SERVICE CircuitBreaker error: {}",
                                    event.getThrowable().toString())
                    )
                    .onCallNotPermitted(event ->
                            log.warn("USER-SERVICE CircuitBreaker call not permitted")
                    )
                    .onSuccess(event ->
                            log.info("USER-SERVICE CircuitBreaker success")
                    );

            circuitBreakerRegistry.circuitBreaker("notificationServiceCircuitBreaker")
                    .getEventPublisher()
                    .onStateTransition(event ->
                            log.warn("NOTIFICATION-SERVICE CircuitBreaker transition: {}",
                                    event.getStateTransition())
                    )
                    .onError(event ->
                            log.warn("NOTIFICATION-SERVICE CircuitBreaker error: {}",
                                    event.getThrowable().toString())
                    )
                    .onCallNotPermitted(event ->
                            log.warn("NOTIFICATION-SERVICE CircuitBreaker call not permitted")
                    )
                    .onSuccess(event ->
                            log.info("NOTIFICATION-SERVICE CircuitBreaker success")
                    );
        };
    }
}
