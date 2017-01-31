/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.constraintvalidators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validates the overruled attributes on all Registers and Channels
 */
@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {OverruledAttributesValidator.class})
public @interface ValidOverruledAttributes {
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
