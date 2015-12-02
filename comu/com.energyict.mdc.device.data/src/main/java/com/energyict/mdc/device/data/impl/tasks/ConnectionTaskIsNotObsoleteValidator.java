package com.energyict.mdc.device.data.impl.tasks;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link NotObsolete} constraint against a {@link ConnectionTaskImpl}.
 *
 */
public class ConnectionTaskIsNotObsoleteValidator implements ConstraintValidator<NotObsolete, ConnectionTaskImpl> {

    @Override
    public void initialize(NotObsolete constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(ConnectionTaskImpl connectionTask, ConstraintValidatorContext context) {
        return !connectionTask.isObsolete();
    }

}