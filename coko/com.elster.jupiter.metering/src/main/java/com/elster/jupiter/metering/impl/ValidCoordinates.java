/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by david on 4/28/2016.
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {ValidCoordinatesValidator.class})
public @interface ValidCoordinates {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

