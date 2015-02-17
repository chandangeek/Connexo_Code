package com.energyict.mdc.masterdata.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { ReadingTypeIntervalOnlyForChannelValidator.class })
public @interface ReadingTypeInterval {

    String message() default "";

    String measurementType() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
