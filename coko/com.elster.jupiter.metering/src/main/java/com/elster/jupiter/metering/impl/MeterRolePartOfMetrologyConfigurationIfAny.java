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
 * Models the contraint that the {@link com.elster.jupiter.metering.config.MeterRole} should be
 * part of all the affected {@link com.elster.jupiter.metering.config.MetrologyConfiguration}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-08-22 (13:00)
 */
@Target({ElementType.TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {MeterRolePartOfMetrologyConfigurationIfAnyValidator.class})
@interface MeterRolePartOfMetrologyConfigurationIfAny {
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}