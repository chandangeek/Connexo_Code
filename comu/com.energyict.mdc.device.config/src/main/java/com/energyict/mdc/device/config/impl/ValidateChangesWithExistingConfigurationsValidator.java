/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

/**
 * Validates the {@link ValidChangesWithExistingConfigurations} constraint against a {@link DeviceTypeImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-03 (13:56)
 */
public class ValidateChangesWithExistingConfigurationsValidator implements ConstraintValidator<ValidChangesWithExistingConfigurations, DeviceTypeImpl> {

    private ServerDeviceConfigurationService deviceConfigurationService;

    @Inject
    public ValidateChangesWithExistingConfigurationsValidator(ServerDeviceConfigurationService deviceConfigurationService) {
        super();
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public void initialize(ValidChangesWithExistingConfigurations constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(DeviceTypeImpl deviceType, ConstraintValidatorContext context) {
        List<DeviceConfiguration> allConfigurations = this.deviceConfigurationService.findDeviceConfigurationsByDeviceType(deviceType);
        if (!allConfigurations.isEmpty()) {
            boolean state = true;
            if (deviceType.deviceProtocolPluggableClassChanged()) {
                context.disableDefaultConstraintViolation();
                context
                        .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.DEVICE_PROTOCOL_CANNOT_CHANGE_WITH_EXISTING_CONFIGURATIONS + "}")
                        .addPropertyNode(DeviceTypeImpl.Fields.DEVICE_PROTOCOL_PLUGGABLE_CLASS.fieldName())
                        .addConstraintViolation();
                state = false;
            }
            if (deviceType.isDeviceTypePurposeChanged()) {
                context.disableDefaultConstraintViolation();
                context
                        .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.CANNOT_CHANGE_DEVICE_TYPE_PURPOSE_WHEN_CONFIGS + "}")
                        .addPropertyNode(DeviceTypeImpl.Fields.DEVICETYPEPURPOSE.fieldName())
                        .addConstraintViolation();
                state = false;
            }
            return state;
        }

        return true;
    }

}