package com.prilepskiy_ae.gateway_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayFallbackController {

    @RequestMapping(value = "/fallback/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> usersFallback() {
        return userServiceUnavailable();
    }

    @RequestMapping(value = "/fallback/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> notificationsFallback() {
        return notificationServiceUnavailable();
    }

    public ResponseEntity<String> userServiceUnavailable() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "errorCode": "USER_SERVICE_UNAVAILABLE",
                          "message": "User service is temporarily unavailable"
                        }
                        """);
    }

    public ResponseEntity<String> notificationServiceUnavailable() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "errorCode": "NOTIFICATION_SERVICE_UNAVAILABLE",
                          "message": "Notification service is temporarily unavailable"
                        }
                        """);
    }
}