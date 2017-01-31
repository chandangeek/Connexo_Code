/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DeviceProtocolPluggableClassValidator implements ConstraintValidator<DeviceProtocolPluggableClassValidation, DeviceTypeImpl> {
    @Override
    public void initialize(DeviceProtocolPluggableClassValidation deviceProtocolPluggableClassValidation) {

    }

    @Override
    public boolean isValid(DeviceTypeImpl deviceType, ConstraintValidatorContext constraintValidatorContext) {
        if (!deviceType.isDataloggerSlave() && !deviceType.getDeviceProtocolPluggableClass().isPresent()) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
                    .addPropertyNode(DeviceTypeImpl.Fields.DEVICE_PROTOCOL_PLUGGABLE_CLASS.fieldName())
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }
        return true;
    }
}
