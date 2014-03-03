package com.energyict.mdc.engine.model.impl;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { UniqueComPortNameValidator.class })
public @interface UniqueComPortName {

	String message() default "{MDC.DuplicateComPort}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
