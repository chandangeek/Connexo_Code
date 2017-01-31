/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LoadProfileTaskValidator implements ConstraintValidator<ValidLoadProfileTask, LoadProfilesTaskImpl> {

    @Override
    public void initialize(ValidLoadProfileTask constraintAnnotation) {
    }

    @Override
    public boolean isValid(LoadProfilesTaskImpl value, ConstraintValidatorContext context) {
        boolean valid = true;
        if (value.isMarkIntervalsAsBadTime()) {
            if (!value.getMinClockDiffBeforeBadTime().isPresent()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY +"}").addPropertyNode(LoadProfilesTaskImpl.Fields.MIN_CLOCK_DIFF_BEFORE_BAD_TIME.fieldName()).addConstraintViolation();
                valid = false;
            }
            else {
                if (value.getMinClockDiffBeforeBadTime().get().getCount() <= 0) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("{"+ MessageSeeds.Keys.TIMEDURATION_MUST_BE_POSITIVE +"}").addPropertyNode(LoadProfilesTaskImpl.Fields.MIN_CLOCK_DIFF_BEFORE_BAD_TIME.fieldName()).addConstraintViolation();
                    valid = false;
                }
            }
        }
        return valid;
    }

}