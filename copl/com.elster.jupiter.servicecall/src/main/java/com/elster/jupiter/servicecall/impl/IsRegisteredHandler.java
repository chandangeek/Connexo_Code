/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by bvn on 2/25/16.
 */
@Target({ElementType.FIELD})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {IsRegisteredHandlerValidator.class})
public @interface IsRegisteredHandler {

    String message() default MessageSeeds.Constants.UNKNOWN_HANDLER;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
