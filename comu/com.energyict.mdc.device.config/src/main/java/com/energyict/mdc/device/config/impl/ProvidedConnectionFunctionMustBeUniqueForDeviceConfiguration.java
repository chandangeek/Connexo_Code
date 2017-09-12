/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.protocol.api.ConnectionFunction;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Models the constraints that the provided {@link ConnectionFunction}
 * of a {@link PartialConnectionTask} must be unique for the {@link DeviceConfiguration} <br/>
 * Or in other words: each {@link ConnectionFunction} can only be used once in a given {@link DeviceConfiguration}.
 *
 * @author Stijn Vanhoorelbeke
 * @since 2017-06-23 (11:36)
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ProvidedConnectionFunctionMustBeUniqueForDeviceConfigurationValidator.class })
public @interface ProvidedConnectionFunctionMustBeUniqueForDeviceConfiguration {

    String message() default "{" + MessageSeeds.Keys.CONNECTION_FUNCTION_UNIQUE + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}