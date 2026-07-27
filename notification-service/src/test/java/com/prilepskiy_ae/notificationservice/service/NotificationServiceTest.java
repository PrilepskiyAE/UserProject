package com.prilepskiy_ae.notificationservice.service;

import com.prilepskiy_ae.notificationservice.dto.mail.EmailRequest;
import io.qameta.allure.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@Story("Проверка корректности EmailRequest")
@Epic("Service Layer Tests")
@Feature("NotificationServiceImpl Business Logic")
@Owner("Prilepskiy Alex")
public class NotificationServiceTest {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    @Test
    @DisplayName("Валидный EmailRequest должен проходить валидацию")
    @Description("Убеждаемся, что корректный запрос не вызывает ошибок")
    void test() {
        var request = new EmailRequest("alex@aston.com", "Тема письма", "Текст сообщения");
        var violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}