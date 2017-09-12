/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.protocol.api.ConnectionFunction;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * Validates the {@link ConsumableConnectionFunctionMustBeSupportedByTheDeviceProtocol} constraint against a {@link ComTaskEnablementImpl}.
 *
 * @author Stijn Vanhoorelbeke
 * @since 2017-06-23 (11:36)
 */
public class ConsumableConnectionFunctionMustBeSupportedByTheDeviceProtocolValidator implements ConstraintValidator<ConsumableConnectionFunctionMustBeSupportedByTheDeviceProtocol, ComTaskEnablementImpl> {

    @Override
    public void initialize(ConsumableConnectionFunctionMustBeSupportedByTheDeviceProtocol constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(ComTaskEnablementImpl comTaskEnablement, ConstraintValidatorContext context) {
        Optional<ConnectionFunction> connectionFunction = comTaskEnablement.getConnectionFunction();
        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = comTaskEnablement.getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass();

        if (!supportedConnectionFunction(connectionFunction, deviceProtocolPluggableClass)) {
            context.disableDefaultConstraintViolation();
            context
                    .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(PartialConnectionTaskImpl.Fields.CONNECTION_FUNCTION.fieldName()).addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean supportedConnectionFunction(Optional<ConnectionFunction> connectionFunction, Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass) {
        if (deviceProtocolPluggableClass.isPresent() && connectionFunction.isPresent()) {
            return deviceProtocolPluggableClass.get().getConsumableConnectionFunctions()
                    .stream()
                    .anyMatch(cf -> cf.getId() == connectionFunction.get().getId());
        } else if (connectionFunction.isPresent()) { // Connection function specified, whilst deviceProtocolPluggableClass is not present
            return false;
        }
        return true;
    }
}