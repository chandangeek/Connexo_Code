/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.common.pluggable.PluggableClass;
import com.energyict.mdc.common.tasks.ConnectionTask;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Models the constraint that a {@link ConnectionTask}
 * should have a valid {@link PluggableClass}.
 * This constraint is validated against the id to avoid that we need to load
 * the pluggable class when updating a ConnectionTask.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-07 (13:53)
 */
@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { ValidPluggableClassIdValidator.class })
public @interface ValidPluggableClassId {

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}