package com.prilepskiy_ae.notificationservice.utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.prilepskiy_ae.notificationservice.utils.OperationValidation.CREATED;
import static com.prilepskiy_ae.notificationservice.utils.OperationValidation.DELETED;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = OperationValidation.class)
@Target({FIELD})
@Retention(RUNTIME)
public @interface ValidOperation {
    String message() default "Недопустимое значение operation: должно быть" + CREATED + "или" + DELETED;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
