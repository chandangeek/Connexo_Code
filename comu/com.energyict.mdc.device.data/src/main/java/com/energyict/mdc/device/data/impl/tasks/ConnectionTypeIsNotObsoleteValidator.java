package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.protocol.api.ConnectionType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link ConnectionTaskIsNotObsolete} constraint against a {@link ConnectionTaskImpl}.
 *
 */
public class ConnectionTypeIsNotObsoleteValidator implements ConstraintValidator<ConnectionTaskIsNotObsolete, ConnectionTaskImpl> {

    @Override
    public void initialize(ConnectionTaskIsNotObsolete constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(ConnectionTaskImpl connectionTask, ConstraintValidatorContext context) {
        return !connectionTask.isObsolete();
    }

}