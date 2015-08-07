package com.elster.jupiter.metering.impl;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { UniqueUsagePointMRIDValidator.class, })
public @interface UniqueMRID {

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[]payload() default {};
}
