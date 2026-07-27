package com.prilepskiy_ae.userservice.dto.user;

import com.prilepskiy_ae.userservice.entity.UserEntity;
import com.prilepskiy_ae.userservice.util.ValidEmail;
import com.prilepskiy_ae.userservice.util.ValidName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.prilepskiy_ae.userservice.util.ValidationConstants.MAX_AGE_LEN;
import static com.prilepskiy_ae.userservice.util.ValidationConstants.MIN_AGE_LEN;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос для создания или обновления пользователя")
public class UserRequest {

    @Schema(
            description = "Имя пользователя",
            example = "Alex"
    )
    @ValidName
    private String name;

    @Schema(
            description = "Email пользователя",
            example = "alex@aston.com"
    )
    @ValidEmail
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
