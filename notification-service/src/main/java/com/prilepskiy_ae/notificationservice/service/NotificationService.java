package com.prilepskiy_ae.notificationservice.service;


import com.prilepskiy_ae.common.OperationType;
import com.prilepskiy_ae.notificationservice.dto.mail.EmailRequest;

public interface NotificationService {
    void sendNotification(String email, OperationType operation);
    void sendEmail(EmailRequest emailRequest);
}
