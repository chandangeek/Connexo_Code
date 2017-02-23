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
 * Models the contraint that the {@link com.elster.jupiter.calendar.Event}s of the
 * {@link com.elster.jupiter.calendar.Calendar} should support all the events
 * of all the event sets of the {@link com.elster.jupiter.metering.config.MetrologyConfiguration}s
 * that are effective during the same interval as the Calendar is effective.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-23 (13:30)
 */
@Target({ElementType.TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {SupportsEventsFromEffectiveMetrologyConfigurationsValidator.class})
public @interface SupportsEventsFromEffectiveMetrologyConfigurations {
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}