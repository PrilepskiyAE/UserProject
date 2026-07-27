package com.prilepskiy_ae.notificationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.prilepskiy_ae.notificationservice.dto.mail.EmailRequest;
import com.prilepskiy_ae.notificationservice.dto.notification.NotificationResponse;
import io.qameta.allure.*;
import io.qameta.allure.junit5.AllureJunit5;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Epic("Notification API REST Assured")
@Feature("NotificationController")
@Owner("Prilepskiy Alex")
@ExtendWith(AllureJunit5.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NotificationControllerRestAssuredTest {

    @LocalServerPort
    private int port;

    private static final String BASE_PATH = "/api/notification";

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.basePath = BASE_PATH;
    }


    @Test
    @Story("Валидация входных данных")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("POST /sendEmail с некорректными данными должен вернуть 400 Bad Request")
    void test1() {
        EmailRequest invalidRequest = new EmailRequest("", "Тема", "Тело");

        given()
                .contentType(JSON)
                .accept(JSON)
                .body(invalidRequest)
                .when()
                .post("/sendEmail")
                .then()
                .statusCode(400)
                .extract()
                .response()
                .prettyPrint();
    }

    /**
     * ТЕСТ Отключил, Для корректной работы теста пропиши Environment variables свои MAIL_LOGIN MAIL_PASSWORD
     *  MAIL_PASSWORD это пароль для приложения
     */
    @Test
    @DisplayName("POST /sendEmail должен вернуть 200 (реальный сценарий)")
    @Disabled
    void test2() throws Exception {

        given()
                .contentType(JSON)
                .body(new EmailRequest("ingener.ambrella@gmail.com", "Subject", "Body"))
                .when()
                .post("/sendEmail")
                .then()
                .statusCode(200);
    }
}
