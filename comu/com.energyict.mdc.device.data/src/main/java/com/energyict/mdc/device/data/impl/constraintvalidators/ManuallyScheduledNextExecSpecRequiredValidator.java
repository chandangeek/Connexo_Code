/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFields;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Checks if the manually scheduled comtaskexecution has a nextExecution spec
 */
public class ManuallyScheduledNextExecSpecRequiredValidator implements ConstraintValidator<ManuallyScheduledNextExecSpecRequired, ComTaskExecutionImpl> {

    @Override
    public void initialize(ManuallyScheduledNextExecSpecRequired manuallyScheduledNextExecSpecRequired) {

    }

    @Override
    public boolean isValid(ComTaskExecutionImpl comTaskExecution, ConstraintValidatorContext constraintValidatorContext) {
        if (comTaskExecution.isScheduledManually() && !comTaskExecution.getNextExecutionSpecs().isPresent()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(ComTaskExecutionFields.NEXTEXECUTIONSPEC.fieldName())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

}
