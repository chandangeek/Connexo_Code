package com.energyict.mdc.engine.config.impl;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validates that field {@code dependFieldName} is not null if
 * field {@code fieldName} has value {@code fieldValue}.
 **/
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = NotEmptyFilePathAndPasswordsValidator.class)
@Documented
public @interface NotEmptyFilePathAndPasswords {

    String message() default "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY_IF_HTTPS+"}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};


}