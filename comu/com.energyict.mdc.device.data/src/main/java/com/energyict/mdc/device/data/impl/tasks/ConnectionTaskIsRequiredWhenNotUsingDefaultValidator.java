package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.exceptions.MessageSeeds;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link ConnectionTaskIsRequiredWhenNotUsingDefault} constraint against a ComTaskExecutionImpl.
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
        if (comTaskExecution.getConnectionTask() == null && !comTaskExecution.useDefaultConnectionTask()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.CONNECTION_TASK_REQUIRED_WHEN_NOT_USING_DEFAULT + "}")
                    .addPropertyNode("connectionTask")
                    .addPropertyNode("useDefaultConnectionTask").addConstraintViolation();
            return false;
        }
        return true;
    }
}
