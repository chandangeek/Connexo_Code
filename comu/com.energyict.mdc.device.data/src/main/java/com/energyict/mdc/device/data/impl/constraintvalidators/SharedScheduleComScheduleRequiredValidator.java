package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFields;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Copyrights EnergyICT
 * Date: 18/11/16
 * Time: 13:17
 */
public class SharedScheduleComScheduleRequiredValidator implements ConstraintValidator<SharedScheduleComScheduleRequired, ComTaskExecutionImpl> {

    @Override
    public void initialize(SharedScheduleComScheduleRequired sharedScheduleComScheduleRequired) {

    }

    @Override
    public boolean isValid(ComTaskExecutionImpl comTaskExecution, ConstraintValidatorContext constraintValidatorContext) {
        if (comTaskExecution.usesSharedSchedule() && !comTaskExecution.getComSchedule().isPresent()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(ComTaskExecutionFields.COM_SCHEDULE.fieldName())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
