/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceCommunicationFunction;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DeviceFunctionsAreSupportedByProtocolValidator implements ConstraintValidator<DeviceFunctionsAreSupportedByProtocol, DeviceConfigurationImpl> {

    @Override
    public void initialize(DeviceFunctionsAreSupportedByProtocol constraintAnnotation) {
    }

    @Override
    public boolean isValid(DeviceConfigurationImpl value, ConstraintValidatorContext context) {
        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = value.getDeviceType().getDeviceProtocolPluggableClass();
        if (!deviceProtocolPluggableClass.isPresent()) {
            return true;
        }
        Set<DeviceCommunicationFunction> communicationFunctions = value.getCommunicationFunctions();
        List<DeviceProtocolCapabilities> deviceProtocolCapabilities = deviceProtocolPluggableClass.get().getDeviceProtocol().getDeviceProtocolCapabilities();
        boolean valid=true;
        if (communicationFunctions.contains(DeviceCommunicationFunction.GATEWAY) && !deviceProtocolCapabilities.contains(DeviceProtocolCapabilities.PROTOCOL_MASTER)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{"+ MessageSeeds.Keys.DEVICE_CONFIG_GATEWAY_NOT_ALLOWED+"}")
                    .addPropertyNode(DeviceConfigurationImpl.Fields.CAN_ACT_AS_GATEWAY.fieldName()).addConstraintViolation();
            valid=false;
        }
        if (communicationFunctions.contains(DeviceCommunicationFunction.PROTOCOL_SESSION) && !deviceProtocolCapabilities.contains(DeviceProtocolCapabilities.PROTOCOL_SESSION)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{"+ MessageSeeds.Keys.DEVICE_CONFIG_DIRECT_ADDRESS_NOT_ALLOWED+"}")
                    .addPropertyNode(DeviceConfigurationImpl.Fields.IS_DIRECTLY_ADDRESSABLE.fieldName()).addConstraintViolation();
            valid=false;
        }

        return valid;
    }
}
