/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {FormatterCompliesWithDestinationsValidator.class})
public @interface FormatterCompliesWithDestinations {
    String message() default "{" + MessageSeeds.Keys.NULL_FORMATTER_WITH_FILE_DESTINATIONS + "}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
