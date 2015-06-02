package com.elster.jupiter.export.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.FIELD})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {ExistingFormatterValidator.class})
public @interface IsExistingFormatter {

    String message() default "{" + MessageSeeds.Keys.NO_SUCH_FORMATTER + "}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}