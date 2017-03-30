/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MessageSeeds;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Models the constraint that the name of
 * a {@link MultiplierTypeImpl} should be unique.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-17 (10:06)
 */
@Target( { TYPE , ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = UniqueTranslationValidator.class)
@Documented
@interface UniqueTranslation {
    String message() default "{" + MessageSeeds.Constants.OBJECT_MUST_HAVE_UNIQUE_NAME + "}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}