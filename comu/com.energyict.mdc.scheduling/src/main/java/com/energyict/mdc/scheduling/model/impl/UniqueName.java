package com.energyict.mdc.scheduling.model.impl;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { UniqueComSchedulingNameValidator.class })
public @interface UniqueName {

	String message() default "{"+ Constants.NOT_UNIQUE +"}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
