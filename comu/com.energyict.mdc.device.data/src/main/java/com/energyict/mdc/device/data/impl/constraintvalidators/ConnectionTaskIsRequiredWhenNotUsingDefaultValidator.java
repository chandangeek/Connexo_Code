package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.ComTaskExecutionFields;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link com.energyict.mdc.device.data.impl.constraintvalidators.ConnectionTaskIsRequiredWhenNotUsingDefault} constraint against a ComTaskExecutionImpl.
 * <p/>
 * Copyrights EnergyICT
 * Date: 16/04/14
 * Time: 09:47
 */
public class ConnectionTaskIsRequiredWhenNotUsingDefaultValidator implements ConstraintValidator<ConnectionTaskIsRequiredWhenNotUsingDefault, ComTaskExecutionImpl> {

    @Override
    public void initialize(ConnectionTaskIsRequiredWhenNotUsingDefault connectionTaskIsRequiredWhenNotUsingDefault) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(ComTaskExecutionImpl comTaskExecution, ConstraintValidatorContext context) {
        if (!comTaskExecution.getConnectionTask().isPresent() && !comTaskExecution.usesDefaultConnectionTask()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.CONNECTION_TASK_REQUIRED_WHEN_NOT_USING_DEFAULT + "}")
                    .addPropertyNode(ComTaskExecutionFields.CONNECTIONTASK.fieldName())
                    .addPropertyNode(ComTaskExecutionFields.USEDEFAULTCONNECTIONTASK.fieldName()).addConstraintViolation();
            return false;
        }
        return true;
    }
}
