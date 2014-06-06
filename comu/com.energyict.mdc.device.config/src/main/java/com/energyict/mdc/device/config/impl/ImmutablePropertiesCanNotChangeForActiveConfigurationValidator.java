package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * CanActAsGateWay and IsDirectlyAddressable are not allowed to
 */
public class ImmutablePropertiesCanNotChangeForActiveConfigurationValidator implements ConstraintValidator<ImmutablePropertiesCanNotChangeForActiveConfiguration, DeviceConfiguration> {

    private final DeviceConfigurationService deviceConfigurationService;
    private String message;

    @Inject
    public ImmutablePropertiesCanNotChangeForActiveConfigurationValidator(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public void initialize(ImmutablePropertiesCanNotChangeForActiveConfiguration constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(DeviceConfiguration deviceConfiguration, ConstraintValidatorContext context) {
        if (deviceConfiguration==null) {
            return true;
        }
        DeviceConfiguration originalConfiguration = deviceConfigurationService.findDeviceConfiguration(deviceConfiguration.getId());
        if (originalConfiguration==null) {
            return true;
        }
        boolean valid=true;
        if (deviceConfiguration.isActive()) {
            if (deviceConfiguration.canActAsGateway()!=originalConfiguration.canActAsGateway()) {
                context.buildConstraintViolationWithTemplate(message).addPropertyNode(DeviceConfigurationImpl.Fields.CAN_ACT_AS_GATEWAY.fieldName()).addConstraintViolation().disableDefaultConstraintViolation();
                valid=false;
            }
            if (deviceConfiguration.canBeDirectlyAddressable()!=originalConfiguration.canBeDirectlyAddressable()) {
                context.buildConstraintViolationWithTemplate(message).addPropertyNode(DeviceConfigurationImpl.Fields.IS_DIRECTLY_ADDRESSABLE.fieldName()).addConstraintViolation().disableDefaultConstraintViolation();
                valid=false;
            }
        }
        return valid;
    }
}
