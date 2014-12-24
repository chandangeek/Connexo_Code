package com.energyict.mdc.engine.config.impl;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { UniquePortNumberValidator.class })
public @interface UniquePortNumber {

	String message() default "{"+ MessageSeeds.Keys.MDC_DUPLICATE_COM_PORT_PER_COM_SERVER+"}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
