package com.prilepskiy_ae.userservice.dto.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor

public class ErrorResponse {
    private final String errorCode;
    private final String message;
    private final LocalDateTime timestamp;
    private final String correlationId;
}
