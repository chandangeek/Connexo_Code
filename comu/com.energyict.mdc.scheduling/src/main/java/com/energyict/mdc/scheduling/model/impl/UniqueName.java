/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.model.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { UniqueComSchedulingNameValidator.class })
public @interface UniqueName {

	String message() default "{"+ MessageSeeds.Keys.NOT_UNIQUE +"}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
