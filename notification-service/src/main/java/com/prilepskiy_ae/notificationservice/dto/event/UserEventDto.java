package com.prilepskiy_ae.notificationservice.dto.event;


import lombok.*;

@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserEventDto {
    private String email;
    private OperationType operation;
}
