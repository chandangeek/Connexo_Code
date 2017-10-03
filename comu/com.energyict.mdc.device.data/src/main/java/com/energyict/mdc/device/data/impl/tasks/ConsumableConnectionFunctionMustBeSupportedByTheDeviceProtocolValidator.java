/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.protocol.api.ConnectionFunction;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * Validates the {@link ConsumableConnectionFunctionMustBeSupportedByTheDeviceProtocol} constraint against a {@link ComTaskExecutionImpl}.
 *
 * @author Stijn Vanhoorelbeke
 * @since 2017-06-23 (11:36)
 */
public class ConsumableConnectionFunctionMustBeSupportedByTheDeviceProtocolValidator implements ConstraintValidator<ConsumableConnectionFunctionMustBeSupportedByTheDeviceProtocol, ComTaskExecutionImpl> {

    @Override
    public void initialize(ConsumableConnectionFunctionMustBeSupportedByTheDeviceProtocol constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(ComTaskExecutionImpl comTaskExecution, ConstraintValidatorContext context) {
        Optional<ConnectionFunction> connectionFunction = comTaskExecution.getConnectionFunction();
        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = comTaskExecution.getDevice().getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass();

        if (!supportedConnectionFunction(connectionFunction, deviceProtocolPluggableClass)) {
            context.disableDefaultConstraintViolation();
            context
                    .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("connectionFunction").addConstraintViolation();
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