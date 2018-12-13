/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.PartialOutboundConnectionTask;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link DeviceConfigurationMustBeDirectlyAddressable} constraint
 * against a {@link PartialOutboundConnectionTask}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-02-13 (08:40)
 */
public class DeviceConfigurationMustBeDirectlyAddressableValidator implements ConstraintValidator<DeviceConfigurationMustBeDirectlyAddressable, PartialOutboundConnectionTask> {

    @Override
    public void initialize(DeviceConfigurationMustBeDirectlyAddressable constraintAnnotation) {
        // No need to extract any information from the annotation
    }

    @Override
    public boolean isValid(PartialOutboundConnectionTask value, ConstraintValidatorContext context) {
        return value.getConfiguration().isDirectlyAddressable();
    }

}