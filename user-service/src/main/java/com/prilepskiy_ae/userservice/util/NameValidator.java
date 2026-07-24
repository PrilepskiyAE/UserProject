package com.prilepskiy_ae.userservice.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static com.prilepskiy_ae.userservice.util.ValidationConstants.*;

public class NameValidator implements ConstraintValidator<ValidName, String> {

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        if (name == null) {
            return false;
        }

        String trimmed = name.trim();

        return trimmed.length() >= MIN_NAME_LEN
                && trimmed.length() <= MAX_NAME_LEN
                && NAME_PATTERN.matcher(trimmed).matches();
    }
}
