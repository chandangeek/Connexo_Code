package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.ComTaskExecutionFields;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates that the Device of the validated ComTaskExecution doesn't have multiple
 * ComTask of the same type
 *
 * Copyrights EnergyICT
 * Date: 18/04/14
 * Time: 09:01
 */
public class UniqueComTaskExecutionPerDeviceValidator implements ConstraintValidator<UniqueComTaskExecutionPerDevice, ComTaskExecutionImpl> {

    @Override
    public void initialize(UniqueComTaskExecutionPerDevice uniqueComTaskExecutionPerDevice) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(ComTaskExecutionImpl comTaskExecution, ConstraintValidatorContext context) {
        int count = 0;
        for (ComTaskExecution taskExecution : comTaskExecution.getDevice().getComTaskExecutions()) {
            if(taskExecution.getComTask().getId() == comTaskExecution.getComTask().getId()){
                count++;
            }
            if(count > 1){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.UNIQUE_COMTASKS_PER_DEVICE + "}")
                        .addPropertyNode(ComTaskExecutionFields.COMTASK.fieldName())
                        .addPropertyNode(ComTaskExecutionFields.DEVICE.fieldName()).addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}