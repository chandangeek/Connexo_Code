/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.protocol.api.ConnectionFunction;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * Validates the {@link ProvidedConnectionFunctionMustBeUniqueForDeviceConfiguration} constraint against a {@link PartialConnectionTaskImpl}.
 *
 * @author Stijn Vanhoorelbeke
 * @since 2017-06-23 (11:36)
 */
public class ProvidedConnectionFunctionMustBeUniqueForDeviceConfigurationValidator implements ConstraintValidator<ProvidedConnectionFunctionMustBeUniqueForDeviceConfiguration, PartialConnectionTaskImpl> {

    @Override
    public void initialize(ProvidedConnectionFunctionMustBeUniqueForDeviceConfiguration constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(PartialConnectionTaskImpl partialConnectionTask, ConstraintValidatorContext context) {
        Optional<ConnectionFunction> connectionFunction = partialConnectionTask.getConnectionFunction();
        DeviceConfiguration deviceConfiguration = partialConnectionTask.getConfiguration();

        if (connectionFunctionAlreadyInUseByOtherPartialConnectionTask(partialConnectionTask, connectionFunction, deviceConfiguration)) {
            context.disableDefaultConstraintViolation();
            context
                    .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(PartialConnectionTaskImpl.Fields.CONNECTION_FUNCTION.fieldName()).addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean connectionFunctionAlreadyInUseByOtherPartialConnectionTask(PartialConnectionTaskImpl partialConnectionTask, Optional<ConnectionFunction> connectionFunction, DeviceConfiguration deviceConfiguration) {
        return connectionFunction.isPresent() && deviceConfiguration.getPartialConnectionTasks()
                .stream()
                .filter(ct -> ct.getConnectionFunction().isPresent() && ct.getConnectionFunction().get().getId() == connectionFunction.get().getId())
                .anyMatch(ct -> ct.getId() != partialConnectionTask.getId());
    }
}