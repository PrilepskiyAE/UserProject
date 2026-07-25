package com.prilepskiy_ae.common;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserEventDto {
    private final String email;
    private final OperationType operation;
}