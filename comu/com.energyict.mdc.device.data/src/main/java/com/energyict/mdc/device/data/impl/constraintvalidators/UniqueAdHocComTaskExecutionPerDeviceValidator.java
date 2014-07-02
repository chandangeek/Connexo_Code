package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.ComTaskExecutionFields;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.impl.tasks.AdHocComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.ComTask;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link UniqueAdHocComTaskExecutionPerDevice} constraint.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-01 (16:47)
 */
public class UniqueAdHocComTaskExecutionPerDeviceValidator implements ConstraintValidator<UniqueAdHocComTaskExecutionPerDevice, AdHocComTaskExecutionImpl> {

    @Override
    public void initialize(UniqueAdHocComTaskExecutionPerDevice annotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(AdHocComTaskExecutionImpl adHocComTaskExecution, ConstraintValidatorContext context) {
        ComTask comTask = adHocComTaskExecution.getComTasks().get(0);
        for (ComTaskExecution other : adHocComTaskExecution.getDevice().getComTaskExecutions()) {
            if (other.getId() != adHocComTaskExecution.getId()) {
                ComTaskExecutionImpl serverComTaskExecution = (ComTaskExecutionImpl) other;
                if (serverComTaskExecution.isAdHoc() && serverComTaskExecution.usesComTask(comTask)) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.UNIQUE_COMTASKS_PER_DEVICE + "}")
                            .addPropertyNode(ComTaskExecutionFields.COMTASK.fieldName())
                            .addPropertyNode(ComTaskExecutionFields.DEVICE.fieldName()).addConstraintViolation();
                    return false;
                }
            }
        }
        return true;
    }

}