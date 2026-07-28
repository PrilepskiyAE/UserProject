package com.prilepskiy_ae.notificationservice.controller;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.prilepskiy_ae.notificationservice.dto.mail.EmailRequest;
import com.prilepskiy_ae.notificationservice.service.NotificationService;
import io.qameta.allure.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertySource;

import static jakarta.mail.internet.MimeUtility.decodeText;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integrationtest")
@Epic("Notification Service")
@Feature("Email Sending")
@Story("Отправка письма через SMTP (GreenMail)")
@Owner("Prilepskiy Alex")
class NotificationControllerMailIntegrationTest {

    private static final int GREENMAIL_SMTP_PORT = ServerSetupTest.SMTP.getPort();
    private static GreenMail greenMail;

    @Autowired
    private NotificationService emailService;

    @BeforeAll
    static void startGreenMail() {
        greenMail = new GreenMail(new ServerSetup(GREENMAIL_SMTP_PORT, null, "smtp"));
        greenMail.start();
    }

    @AfterAll
    static void stopGreenMail() {
        if (greenMail != null) {
            greenMail.stop();
        }
    }

    @BeforeEach
    void cleanInbox() {
        greenMail.reset();
    }

    @DynamicPropertySource
    static void smtpProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> GREENMAIL_SMTP_PORT);
        registry.add("spring.mail.username", () -> "");
        registry.add("spring.mail.password", () -> "");
    }

    @Test
    @DisplayName("Тест для проверки отправки сообщения на почту")
    @Description("Проверка успешной отправки обычного текстового письма на локальный SMTP (GreenMail). " +
            "Тест должен убедиться, что письмо попадает в ящик, тема и тело совпадают.")
    void testSendMail() throws Exception {

        EmailRequest request = new EmailRequest(
                "recipient@example.com",
                "Тест интеграции",
                "Тело письма от интеграционного теста."
        );

        sendEmailStep(request);
        verifyEmailReceivedStep();
        verifyRecipientStep("recipient@example.com");
        verifySubjectStep("Тест интеграции");
        verifyBodyStep("Тело письма от интеграционного теста.");
    }

    @Step("Отправка письма через NotificationService")
    private void sendEmailStep(EmailRequest request) throws Exception {
        emailService.sendEmail(request);
    }

    @Step("Проверка, что письмо было получено GreenMail")
    private void verifyEmailReceivedStep() {
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length, "Ожидается ровно 1 полученное письмо");
    }

    @Step("Проверка получателя письма: {expectedRecipient}")
    private void verifyRecipientStep(String expectedRecipient) throws MessagingException {
        MimeMessage message = greenMail.getReceivedMessages()[0];
        String actualRecipient = message.getAllRecipients()[0].toString();
        assertEquals(expectedRecipient, actualRecipient, "Получатель не совпадает");
    }

    @Step("Проверка темы письма: {expectedSubject}")
    private void verifySubjectStep(String expectedSubject) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = greenMail.getReceivedMessages()[0];
        String subject = decodeText(message.getSubject());
        assertEquals(expectedSubject, subject, "Тема письма не совпадает");
    }

    @Step("Проверка тела письма: должно содержать '{expectedContent}'")
    private void verifyBodyStep(String expectedContent) throws MessagingException, IOException {
        MimeMessage message = greenMail.getReceivedMessages()[0];
        String content = message.getContent().toString();
        assertTrue(content.contains(expectedContent), "Тело письма не содержит ожидаемого текста");
    }
}