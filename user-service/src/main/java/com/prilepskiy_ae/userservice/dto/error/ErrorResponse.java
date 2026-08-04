package com.prilepskiy_ae.userservice.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@Schema(description = "Ответа об ошибке", name = "ErrorResponse")
public class ErrorResponse {
    @Schema(
            description = "Код ошибки для программной обработки",
            example = "VALIDATION_ERROR",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final String errorCode;
    @Schema(
            description = "описание ошибки. Для валидационных ошибок содержит детали по полям.",
            example = "Поле 'name' обязательно для заполнения",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final String message;
    @Schema(
            description = "Дата и время возникновения ошибки (",
            example = "2026-06-15T10:30:00",
            format = "date-time",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final LocalDateTime timestamp;

    @Schema(
            description = "Correlation ID для трассировки запроса через сервисы и логи",
            example = "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8",
            pattern = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final String correlationId;
}
