package com.prilepskiy_ae.notificationservice.controller;

import com.prilepskiy_ae.common.OperationType;
import com.prilepskiy_ae.notificationservice.dto.mail.EmailRequest;
import com.prilepskiy_ae.notificationservice.dto.notification.NotificationRequest;
import com.prilepskiy_ae.notificationservice.dto.notification.NotificationResponse;
import com.prilepskiy_ae.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
public class NotificationControllerImpl implements NotificationController {

    private final NotificationService notificationService;

    @Override
    public ResponseEntity<NotificationResponse> notificationEmail(NotificationRequest request) {
        String email = request.getEmail();
        String operationStr = request.getOperation();
        log.info("Получен запрос на отправку уведомления: email={}, операция={}", email, operationStr);

        try {
            OperationType operation = OperationType.valueOf(operationStr.toUpperCase());
            log.debug("Преобразовано OperationType={}", operation);

            notificationService.sendNotification(email, operation);
            log.info("Уведомление успешно инициировано для email={}, операция={}", email, operation);

            return ResponseEntity.ok(new NotificationResponse(operationStr));
        } catch (IllegalArgumentException e) {
            log.error("Недопустимое значение операции '{}' для email={}. Допустимые значения: {}",
                    operationStr, email, OperationType.values(), e);
            throw new IllegalArgumentException(
                    "Недопустимое значение operation: " + operationStr +
                            ". Допустимые: " + java.util.Arrays.toString(OperationType.values())
            );
        } catch (Exception e) {
            log.error("Ошибка при отправке уведомления для email={}, операция={}", email, operationStr, e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<NotificationResponse> sendMessage(EmailRequest request) {
        String email = request.getEmail();
        String subject = request.getSubject();
        String message = request.getMessage();

        log.info("Получен прямой запрос на отправку письма: email={}, subject={}, message={}", email, subject,message);

        try {
            notificationService.sendEmail(request);
            log.info("Письмо успешно инициировано: email={}, subject={}", email, subject);
            return ResponseEntity.ok(new NotificationResponse("Сообщение успешно отправлено"));
        } catch (Exception e) {
            log.error("Критическая ошибка при отправке письма: email={}, subject={}", email, subject, e);
            throw e;
        }
    }
}
