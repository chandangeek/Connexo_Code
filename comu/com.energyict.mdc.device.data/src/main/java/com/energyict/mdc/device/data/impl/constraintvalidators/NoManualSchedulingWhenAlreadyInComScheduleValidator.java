package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.ComTaskExecutionFields;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ManuallyScheduledComTaskExecutionImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.ComTask;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link NoManualSchedulingWhenAlreadyInComSchedule} constraint.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-01 (16:47)
 */
public class NoManualSchedulingWhenAlreadyInComScheduleValidator implements ConstraintValidator<NoManualSchedulingWhenAlreadyInComSchedule, ManuallyScheduledComTaskExecutionImpl> {

    @Override
    public void initialize(NoManualSchedulingWhenAlreadyInComSchedule annotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(ManuallyScheduledComTaskExecutionImpl manuallyScheduledComTaskExecution, ConstraintValidatorContext context) {
        ComTask comTask = manuallyScheduledComTaskExecution.getComTasks().get(0);
        for (ComTaskExecution other : manuallyScheduledComTaskExecution.getDevice().getComTaskExecutions()) {
            if (other.getId() != manuallyScheduledComTaskExecution.getId()) {
                ComTaskExecutionImpl serverComTaskExecution = (ComTaskExecutionImpl) other;
                if (serverComTaskExecution.isScheduled() && serverComTaskExecution.usesComTask(comTask)) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.NO_MANUAL_SCHEDULING_FOR_COMTASKS_IN_COMSCHEDULE + "}")
                            .addPropertyNode(ComTaskExecutionFields.COMTASK.fieldName())
                            .addPropertyNode(ComTaskExecutionFields.DEVICE.fieldName()).addConstraintViolation();
                    return false;
                }
            }
        }
        return true;
    }

}