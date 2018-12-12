/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.protocol.api.ConnectionFunction;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Models the constraints that a {@link ComTaskEnablement}
 * cannot specify to use the connection task having a specific {@link ConnectionFunction} and specify a
 * {@link PartialConnectionTask} at the same time.
 *
 * @author Stijn Vanhoorelbeke
 * @since 2017-06-26 (11:36)
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {NoPartialConnectionTaskWhenConnectionFunctionIsUsedValidator.class})
public @interface NoPartialConnectionTaskWhenConnectionFunctionIsUsed {

    String message() default "{" + MessageSeeds.Keys.COM_TASK_ENABLEMENT_CANNOT_USE_CONNECTION_FUNCTION_AND_PARTIAL_CONNECTION_TASK + "}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}