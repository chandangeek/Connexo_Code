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
 * Validates the {@link ProvidedConnectionFunctionMustBeSupportedByTheDeviceProtocol} constraint against a {@link PartialConnectionTaskImpl}.
 *
 * @author Stijn Vanhoorelbeke
 * @since 2017-06-23 (11:36)
 */
public class ProvidedConnectionFunctionMustBeSupportedByTheDeviceProtocolValidator implements ConstraintValidator<ProvidedConnectionFunctionMustBeSupportedByTheDeviceProtocol, PartialConnectionTaskImpl> {

    @Override
    public void initialize(ProvidedConnectionFunctionMustBeSupportedByTheDeviceProtocol constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(PartialConnectionTaskImpl partialConnectionTask, ConstraintValidatorContext context) {
        Optional<ConnectionFunction> connectionFunction = partialConnectionTask.getConnectionFunction();
        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = partialConnectionTask.getConfiguration().getDeviceType().getDeviceProtocolPluggableClass();

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
            return deviceProtocolPluggableClass.get().getProvidedConnectionFunctions()
                    .stream()
                    .anyMatch(cf -> cf.getId() == connectionFunction.get().getId());
        } else if (connectionFunction.isPresent()) { // Connection function specified, whilst deviceProtocolPluggableClass is not present
            return false;
        }
        return true;
    }
}