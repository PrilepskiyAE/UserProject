package com.prilepskiy_ae.userservice.dto.event;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@AllArgsConstructor
public class UserEventDto {
    private final String email;
    private final OperationType operation;
}