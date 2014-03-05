package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import java.util.List;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link ProtocolCannotChangeWithExistingConfigurations} constraint against a {@link DeviceTypeImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-03 (13:56)
 */
public class ProtocolCannotChangeWithExistingConfigurationsValidator implements ConstraintValidator<ProtocolCannotChangeWithExistingConfigurations, DeviceTypeImpl> {

    private DeviceConfigurationService deviceConfigurationService;

    @Inject
    public ProtocolCannotChangeWithExistingConfigurationsValidator(DeviceConfigurationService deviceConfigurationService) {
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
            ServerDeviceConfigurationService deviceConfigurationService = (ServerDeviceConfigurationService) this.deviceConfigurationService;
            List<DeviceConfiguration> allConfigurations = deviceConfigurationService.findDeviceConfigurationsByDeviceType(deviceType);
            if (!allConfigurations.isEmpty()) {
                context.disableDefaultConstraintViolation();
                context
                    .buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.DEVICE_PROTOCOL_CANNOT_CHANGE_WITH_EXISTING_CONFIGURATIONS_KEY + "}")
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