/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.scheduling.NextExecutionSpecs;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Models the constraint that a {@link ScheduledConnectionTaskImpl}
 * must have {@link NextExecutionSpecs}
 * when the {@link ConnectionStrategy}
 * is {@link ConnectionStrategy#MINIMIZE_CONNECTIONS minimize connections}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-13 (14:47)
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { NextExecutionSpecsWithMinimizeConnectionsStrategyValidator.class })
public @interface ValidNextExecutionSpecsWithMinimizeConnectionsStrategy {

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}