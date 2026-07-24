package com.prilepskiy_ae.userservice.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = NameValidator.class)
@Target({FIELD})
@Retention(RUNTIME)
public @interface ValidName {

    String message() default "Имя должно быть от 2 до 100 символов и содержать только буквы, пробелы, '-' или апостроф";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
