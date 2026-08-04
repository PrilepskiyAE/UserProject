package com.prilepskiy_ae.userservice.dto.user;

import com.prilepskiy_ae.userservice.entity.UserEntity;
import com.prilepskiy_ae.userservice.util.ValidName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.prilepskiy_ae.userservice.util.ValidationConstants.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        description = "Запрос для создания или обновления пользователя. " +
                "Все поля обязательны. Имя должно соответствовать правилам валидации.",
        requiredMode = Schema.RequiredMode.REQUIRED
)
public class UserRequest {

    @Schema(
            description = "Имя пользователя. Должно содержать только буквы, без спецсимволов и цифр. Длина: 2–50 символов.",
            example = "Alex",
            minLength = MIN_NAME_LEN,
            maxLength = MAX_NAME_LEN,
            pattern = "^[A-Za-zA-Яа-я\\s]+$",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @ValidName
    private String name;

    @Schema(
            description = "Email пользователя. Должен быть в корректном формате email.",
            example = "alex@aston.com",
            format = "email",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Email обязателен")
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный email")
    private String email;

    @Schema(
            description = "Возраст пользователя",
            example = "25",
            minimum = "0",
            maximum = "100",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Возраст обязателен")
    @Min(value = MIN_AGE_LEN , message = "Возраст не может быть отрицательным")
    @Max(value = MAX_AGE_LEN, message = "Возраст не может быть больше 100")
    private Integer age;

    public UserEntity toEntity() {
        return new UserEntity(this.name, this.email, this.age);
    }
}
