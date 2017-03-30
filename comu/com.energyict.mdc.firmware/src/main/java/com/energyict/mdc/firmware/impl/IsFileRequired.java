/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target( { TYPE , ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = IsFileRequiredValidator.class)
@Documented

public @interface IsFileRequired {
    String message() default "{com.elster.jupiter.firmware.file.change.forbidden}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
