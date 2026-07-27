package com.prilepskiy_ae.notificationservice.dto.notification;

import com.prilepskiy_ae.notificationservice.utils.ValidOperation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.prilepskiy_ae.notificationservice.utils.OperationValidation.CREATED;
import static com.prilepskiy_ae.notificationservice.utils.OperationValidation.DELETED;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter

public class NotificationRequest {
    @Schema(
            description = "Email пользователя",
            example = "alex@aston.com"
    )
    @Email(message = "Некорректный email")
    private String email;

    @Schema(
            description = "Значение operation: должно быть " + CREATED + " или " + DELETED,
            example = CREATED
    )
    @ValidOperation
    private String operation;
}
