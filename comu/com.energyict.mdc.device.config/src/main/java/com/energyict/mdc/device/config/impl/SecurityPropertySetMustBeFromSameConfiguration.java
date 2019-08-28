/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.SecurityPropertySet;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Models the constraints that the {@link SecurityPropertySet}
 * of a {@link ComTaskEnablement} must be from the same
 * {@link DeviceConfiguration}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-22 (14:52)
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {SecurityPropertySetMustBeFromSameConfigurationValidator.class })
public @interface SecurityPropertySetMustBeFromSameConfiguration {

    String message() default "{" + MessageSeeds.Keys.COM_TASK_ENABLEMENT_SECURITY_PROPERTY_SET_MUST_BE_FROM_SAME_CONFIGURATION + "}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
