/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.units;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ ElementType.FIELD})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { HasQuantityMultiplierValidator.class })
public @interface HasQuantityMultiplier {

    long min();

    long max();

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[]payload() default {

    };

}
