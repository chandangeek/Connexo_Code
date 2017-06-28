/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.protocol.api.ConnectionFunction;
import com.energyict.mdc.upl.DeviceProtocol;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Models the constraints that the provided {@link ConnectionFunction}
 * of a {@link PartialConnectionTask} must be supported
 * by the actual {@link DeviceProtocol}.
 *
 * @author Stijn Vanhoorelbeke
 * @since 2017-06-23 (11:36)
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ProvidedConnectionFunctionMustBeSupportedByTheDeviceProtocolValidator.class })
public @interface ProvidedConnectionFunctionMustBeSupportedByTheDeviceProtocol {

    String message() default "{" + MessageSeeds.Keys.CONNECTION_FUNCTION_NOT_SUPPORTED_BY_DEVICE_PROTOCOL + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}