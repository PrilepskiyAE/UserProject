package com.prilepskiy_ae.notificationservice.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OperationValidation implements ConstraintValidator<ValidOperation, String> {
   public static final String CREATED = "CREATED";
   public static final String DELETED = "DELETED";
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return CREATED.equals(value) || DELETED.equals(value);
    }
}
