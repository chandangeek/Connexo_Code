package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceCommunicationFunction;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DeviceFunctionsAreSupportedByProtocolValidator implements ConstraintValidator<DeviceFunctionsAreSupportedByProtocol, DeviceConfigurationImpl> {

    @Override
    public void initialize(DeviceFunctionsAreSupportedByProtocol constraintAnnotation) {
    }

    @Override
    public boolean isValid(DeviceConfigurationImpl value, ConstraintValidatorContext context) {
        Set<DeviceCommunicationFunction> communicationFunctions = value.getCommunicationFunctions();
        List<DeviceProtocolCapabilities> deviceProtocolCapabilities = value.getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getDeviceProtocolCapabilities();
        boolean valid=true;
        if (communicationFunctions.contains(DeviceCommunicationFunction.GATEWAY) && !deviceProtocolCapabilities.contains(DeviceProtocolCapabilities.PROTOCOL_MASTER)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{"+MessageSeeds.DEVICE_CONFIGURATION_CAN_NOT_BE_GATEWAY.getKey()+"}").addPropertyNode("isGateway").addConstraintViolation();
            valid=false;
        }
        if (communicationFunctions.contains(DeviceCommunicationFunction.PROTOCOL_SESSION) && !deviceProtocolCapabilities.contains(DeviceProtocolCapabilities.PROTOCOL_SESSION)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{"+MessageSeeds.DEVICE_CONFIGURATION_CAN_NOT_BE_DIRECTLY_ADDRESSED.getKey()+"}").addPropertyNode("isDirectlyAddressable").addConstraintViolation();
            valid=false;
        }

        return valid;
    }
}
