package com.prilepskiy_ae.notificationservice.dto.mail;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@Getter
public class EmailRequest {
    @NotBlank
    @Email
    @Schema(
            description = "Адрес электронной почты получателя",
            example = "alex@aston.com"
    )
    String email;
    @NotBlank(message = "Тема письма обязательна")
    @Schema(
            description = "Тема (subject) отправляемого письма",
            example = "Уведомление о создании аккаунта"
    )
    @NotBlank String subject;
    @NotBlank(message = "Текст сообщения обязателен")
    @Schema(
            description = "Основное содержимое письма",
            example = "Ваш аккаунт успешно создан. Добро пожаловать!"
    )
    @NotBlank String message;
}
