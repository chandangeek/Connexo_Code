package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import com.elster.jupiter.orm.associations.Reference;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Copyrights EnergyICT
 * Date: 7/7/14
 * Time: 11:56 AM
 */
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
                        buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.DEVICE_CONFIGURATION_REQUIRED + "}").
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
