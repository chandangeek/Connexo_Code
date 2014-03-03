package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * Validates the {@link AllConfigurationsHaveUniqueName} constraint against a {@link DeviceTypeImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-28 (11:40)
 */
public class AllConfigurationsHaveUniqueNameValidator implements ConstraintValidator<AllConfigurationsHaveUniqueName, DeviceTypeImpl> {

    @Override
    public void initialize(AllConfigurationsHaveUniqueName constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(DeviceTypeImpl deviceType, ConstraintValidatorContext context) {
        Set<String> configurationNames = new HashSet<>();
        ServerDeviceConfigurationService deviceConfigurationService = (ServerDeviceConfigurationService) deviceType.getDeviceConfigurationService();
        for (DeviceConfiguration deviceConfiguration : deviceConfigurationService.findDeviceConfigurationsByDeviceType(deviceType)) {
            if (configurationNames.contains(deviceConfiguration.getName())) {
                context.disableDefaultConstraintViolation();
                context
                    .buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.DUPLICATE_DEVICE_CONFIGURATION_KEY + "}")
                    .addPropertyNode("configurations").addConstraintViolation();
                return false;
            }
            else {
                configurationNames.add(deviceConfiguration.getName());
            }
        }
        return true;
    }

}