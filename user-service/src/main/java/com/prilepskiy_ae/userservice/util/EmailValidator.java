package com.prilepskiy_ae.userservice.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static com.prilepskiy_ae.userservice.util.ValidationConstants.*;

public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) {
            return false;
        }

        String trimmed = email.trim();

        if (trimmed.length() < MIN_EMAIL_LEN || trimmed.length() > MAX_EMAIL_LEN) {
            return false;
        }

        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            return false;
        }

        if (trimmed.contains("..")) {
            return false;
        }

        int atIndex = trimmed.indexOf('@');
        if (atIndex <= FIRST_INDEX || atIndex != trimmed.lastIndexOf('@')) {
            return false;
        }

        String localPart = trimmed.substring(FIRST_INDEX, atIndex);
        String domainPart = trimmed.substring(atIndex + SHIFT_INDEX);

        if (localPart.startsWith(".") || localPart.endsWith(".")) {
            return false;
        }

        if (!domainPart.contains(".")) {
            return false;
        }

        String[] labels = domainPart.split("\\.");
        for (String label : labels) {
            if (label.isEmpty()) {
                return false;
            }

            if (label.startsWith("-") || label.endsWith("-")) {
                return false;
            }
        }

        return true;
    }
}