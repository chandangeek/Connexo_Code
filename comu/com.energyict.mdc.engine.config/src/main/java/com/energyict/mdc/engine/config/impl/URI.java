/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validate that the string is a valid URL.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-31 (11:33)
 */
@Target({ ElementType.FIELD })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { URIValidator.class })
public @interface URI {

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
