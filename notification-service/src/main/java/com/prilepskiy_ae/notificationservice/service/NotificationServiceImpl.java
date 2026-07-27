package com.prilepskiy_ae.notificationservice.service;

import com.prilepskiy_ae.common.OperationType;
import com.prilepskiy_ae.notificationservice.dto.mail.EmailRequest;
import com.prilepskiy_ae.notificationservice.dto.mail.MailProperties;
import com.prilepskiy_ae.notificationservice.exception.NotificationException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public NotificationServiceImpl(JavaMailSender mailSender, MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }
    @Override
    public void sendNotification(String email, OperationType operation) {
        String subject;
        String body;

        if (operation == OperationType.CREATED) {
            subject = "Ваш аккаунт успешно создан";
            body = "Здравствуйте! Ваш аккаунт на сайте " + mailProperties.getSiteUrl() + " был успешно создан.";
        } else {
            subject = "Аккаунт удалён";
            body = "Здравствуйте! Ваш аккаунт был удалён.";
        }

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setTo(email);
            helper.setFrom(mailProperties.getFromAddress());
            helper.setSubject(subject);
            helper.setText(body, false);
        } catch (MessagingException e) {
            throw new NotificationException("Failed to send email to " + email, e);
        }
        mailSender.send(message);
    }

    @Override
    public void sendEmail(EmailRequest emailRequest) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setTo(emailRequest.getEmail());
            helper.setFrom(mailProperties.getFromAddress());
            helper.setSubject(emailRequest.getSubject());
            helper.setText(emailRequest.getMessage(), false);
        } catch (MessagingException e) {
            throw new NotificationException("Failed to send email to " + emailRequest.getEmail(), e);
        }
        mailSender.send(message);
    }
}
