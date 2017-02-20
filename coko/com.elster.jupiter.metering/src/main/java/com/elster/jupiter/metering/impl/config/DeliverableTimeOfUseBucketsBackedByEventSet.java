/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Models the constraint that all {@link com.elster.jupiter.metering.config.ReadingTypeDeliverable}s
 * on a {@link com.elster.jupiter.metering.config.MetrologyConfiguration}
 * that have a time of use bucket must be backed by an {@link com.elster.jupiter.calendar.EventSet}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-20 (12:54)
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {DeliverableTimeOfUseBucketsBackedByEventSetValidator.class })
public @interface DeliverableTimeOfUseBucketsBackedByEventSet {

    String message() default "{" + MessageSeeds.Constants.DELIVERABLE_TOU_NOT_BACKED_BY_EVENTSET + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}