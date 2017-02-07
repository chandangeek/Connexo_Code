/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DeviceConfigIsPresentAndActiveValidator  implements ConstraintValidator<DeviceConfigurationIsPresentAndActive, Object> {

    @Override
    public void initialize(DeviceConfigurationIsPresentAndActive deviceConfigurationIsPresentAndActive) {

    }

    @Override
    public boolean isValid(Object reference, ConstraintValidatorContext constraintValidatorContext) {
        if (reference instanceof Reference) {
            Reference<DeviceConfiguration> deviceConfigReference = (Reference<DeviceConfiguration>) reference;
            if(!deviceConfigReference.isPresent()) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.
                        buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.FIELD_REQUIRED + "}").
                        addConstraintViolation();
                return false;
            } else if(!deviceConfigReference.get().isActive()){
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.
                        buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.DEVICE_CONFIGURATION_NOT_ACTIVE + "}").
                        addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
