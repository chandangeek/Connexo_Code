package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

/**
 * Validates the {@link ProtocolCannotChangeWithExistingConfigurations} constraint against a {@link DeviceTypeImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-03 (13:56)
 */
public class ProtocolCannotChangeWithExistingConfigurationsValidator implements ConstraintValidator<ProtocolCannotChangeWithExistingConfigurations, DeviceTypeImpl> {

    private ServerDeviceConfigurationService deviceConfigurationService;

    @Inject
    public ProtocolCannotChangeWithExistingConfigurationsValidator(ServerDeviceConfigurationService deviceConfigurationService) {
        super();
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public void initialize(ProtocolCannotChangeWithExistingConfigurations constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(DeviceTypeImpl deviceType, ConstraintValidatorContext context) {
        if (deviceType.deviceProtocolPluggableClassChanged()) {
            List<DeviceConfiguration> allConfigurations = this.deviceConfigurationService.findDeviceConfigurationsByDeviceType(deviceType);
            if (!allConfigurations.isEmpty()) {
                context.disableDefaultConstraintViolation();
                context
                    .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.DEVICE_PROTOCOL_CANNOT_CHANGE_WITH_EXISTING_CONFIGURATIONS + "}")
                    .addPropertyNode("deviceProtocolPluggableClass").addConstraintViolation();
                return false;
            }
            else {
                return true;
            }
        }
        else {
            return true;
        }
    }

}