package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledComTaskExecutionImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link UniqueComSchedulePerDevice} constraint.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-01 (16:47)
 */
public class UniqueComSchedulePerDeviceValidator implements ConstraintValidator<UniqueComSchedulePerDevice, ScheduledComTaskExecutionImpl> {

    @Override
    public void initialize(UniqueComSchedulePerDevice annotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(ScheduledComTaskExecutionImpl scheduledComTaskExecution, ConstraintValidatorContext context) {
        for (ComTaskExecution other : scheduledComTaskExecution.getDevice().getComTaskExecutions()) {
            if (other.getId() != scheduledComTaskExecution.getId()) {
                if (this.isScheduled(other) && other.executesComSchedule(scheduledComTaskExecution.getComSchedule())) {
                    context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.DUPLICATE_COMTASK_SCHEDULING + "}")
                            .addConstraintViolation()
                            .disableDefaultConstraintViolation();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isScheduled(ComTaskExecution comTaskExecution) {
        return comTaskExecution.usesSharedSchedule() && !comTaskExecution.isScheduledManually();
    }

}