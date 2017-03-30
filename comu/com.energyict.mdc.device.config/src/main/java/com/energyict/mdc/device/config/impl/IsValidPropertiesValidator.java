/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IsValidPropertiesValidator implements ConstraintValidator<IsValidProperty, DeviceProtocolConfigurationProperty> {
    @Override
    public void initialize(IsValidProperty constraintAnnotation) {
    }

    @Override
    public boolean isValid(DeviceProtocolConfigurationProperty value, ConstraintValidatorContext context) {
        return value.validate(context);
    }
}
