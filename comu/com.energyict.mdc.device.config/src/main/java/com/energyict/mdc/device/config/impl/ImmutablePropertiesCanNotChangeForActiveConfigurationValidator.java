package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

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
        Optional<DeviceConfiguration> originalConfiguration = deviceConfigurationService.findDeviceConfiguration(deviceConfiguration.getId());
        if (!originalConfiguration.isPresent()) {
            return true;
        }
        boolean valid=true;
        if (deviceConfiguration.isActive()) {
            if (deviceConfiguration.canActAsGateway()!=originalConfiguration.get().canActAsGateway()) {
                context.buildConstraintViolationWithTemplate(message).addPropertyNode(DeviceConfigurationImpl.Fields.CAN_ACT_AS_GATEWAY.fieldName()).addConstraintViolation().disableDefaultConstraintViolation();
                valid=false;
            }
            if (deviceConfiguration.getGatewayType() != originalConfiguration.get().getGatewayType()){
                context.buildConstraintViolationWithTemplate(message).addPropertyNode(DeviceConfigurationImpl.Fields.GATEWAY_TYPE.fieldName()).addConstraintViolation().disableDefaultConstraintViolation();
                valid=false;
            }
            if (deviceConfiguration.isDirectlyAddressable()!=originalConfiguration.get().isDirectlyAddressable()) {
                context.buildConstraintViolationWithTemplate(message).addPropertyNode(DeviceConfigurationImpl.Fields.IS_DIRECTLY_ADDRESSABLE.fieldName()).addConstraintViolation().disableDefaultConstraintViolation();
                valid=false;
            }
        }
        return valid;
    }
}
