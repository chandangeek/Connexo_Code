/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { ValidateRelativeDateRangeValidator.class })
public @interface ValidateRelativeDateRange {
    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    String[] fields();
}
