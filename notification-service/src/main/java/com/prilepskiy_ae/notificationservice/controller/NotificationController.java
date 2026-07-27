package com.prilepskiy_ae.notificationservice.controller;

import com.prilepskiy_ae.notificationservice.dto.mail.EmailRequest;
import com.prilepskiy_ae.notificationservice.dto.notification.NotificationRequest;
import com.prilepskiy_ae.notificationservice.dto.notification.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Validated
@Tag(name = "Notification", description = "API для рассылки увидомлений")
@RequestMapping("/api/notification")
public interface NotificationController {
    @Operation(summary = "Отправить увидомление")
    @PostMapping("sendNotification")
    ResponseEntity<NotificationResponse> notificationEmail(@Valid @RequestBody NotificationRequest request);

    @Operation(summary = "Отправить письмо")
    @PostMapping("sendEmail")
    ResponseEntity<NotificationResponse> sendMessage(@Valid @RequestBody EmailRequest request);
}
