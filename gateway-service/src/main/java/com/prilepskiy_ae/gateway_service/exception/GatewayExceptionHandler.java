package com.prilepskiy_ae.gateway_service.exception;


import com.prilepskiy_ae.gateway_service.controller.GatewayFallbackController;
import com.prilepskiy_ae.gateway_service.utils.Utils;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.concurrent.TimeUnit;

@RestControllerAdvice
public class GatewayExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GatewayExceptionHandler.class);

    private final GatewayFallbackController fallbackController;
    private final CircuitBreaker userServiceCircuitBreaker;
    private final CircuitBreaker notificationServiceCircuitBreaker;

    public GatewayExceptionHandler(
            GatewayFallbackController fallbackController,
            CircuitBreakerRegistry circuitBreakerRegistry
    ) {
        this.fallbackController = fallbackController;
        this.userServiceCircuitBreaker =
                circuitBreakerRegistry.circuitBreaker("userServiceCircuitBreaker");
        this.notificationServiceCircuitBreaker =
                circuitBreakerRegistry.circuitBreaker("notificationServiceCircuitBreaker");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(
            Exception exception,
            HttpServletRequest request
    ) {
        String path = request.getRequestURI();

        if (Utils.isUserPath(path)) {
            recordError(userServiceCircuitBreaker, exception);
            log.error("USER-SERVICE gateway failure. path={}, state={}",
                    path,
                    userServiceCircuitBreaker.getState(),
                    exception
            );
            return fallbackController.userServiceUnavailable();
        }

        if (Utils.isNotificationPath(path)) {
            recordError(notificationServiceCircuitBreaker, exception);
            log.error("NOTIFICATION-SERVICE gateway failure. path={}, state={}",
                    path,
                    notificationServiceCircuitBreaker.getState(),
                    exception
            );
            return fallbackController.notificationServiceUnavailable();
        }

        throw new RuntimeException(exception);
    }

    private void recordError(CircuitBreaker circuitBreaker, Exception exception) {
        if (isTechnicalError(exception)) {
            circuitBreaker.onError(0, TimeUnit.MILLISECONDS, exception);
        }
    }

    private boolean isTechnicalError(Throwable throwable) {
        Throwable current = throwable;

        while (current != null) {
            if (current instanceof HttpHostConnectException) {
                return true;
            }

            if (current instanceof java.net.ConnectException) {
                return true;
            }


            String message = current.getMessage();
            if (message != null && message.contains("Connection refused")) {
                return true;
            }

            if (message != null && message.contains("В соединении отказано")) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }


}
