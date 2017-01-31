/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Verify the reference is not null.
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { ComPortPoolTypeValidator.class, ComPortTypeValidator.class })
public @interface ComPortPoolTypeMatchesComPortType {

	String message() default "{"+ MessageSeeds.Keys.MDC_COM_PORT_TYPE_OF_COM_PORT_DOES_NOT_MATCH_WITH_COM_PORT_POOL+"}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
