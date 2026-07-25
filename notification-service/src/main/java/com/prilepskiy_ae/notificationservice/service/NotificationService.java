package com.prilepskiy_ae.notificationservice.service;


import com.prilepskiy_ae.common.OperationType;

public interface NotificationService {
    void sendNotification(String email, OperationType operation);
}
