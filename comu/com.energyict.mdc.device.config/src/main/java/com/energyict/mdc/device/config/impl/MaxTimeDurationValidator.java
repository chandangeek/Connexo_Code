/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.time.TimeDuration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates a time duration does not exceed a maximum amount, specified in seconds.
 */
public class MaxTimeDurationValidator implements ConstraintValidator<MaxTimeDuration, TimeDuration> {
    private String message;
    private long maxValue;

    @Override
    public void initialize(MaxTimeDuration maxTimeDuration) {
        this.message = maxTimeDuration.message();
        maxValue = maxTimeDuration.max();
    }

    @Override
    public boolean isValid(TimeDuration timeDuration, ConstraintValidatorContext constraintValidatorContext) {
        if (timeDuration!=null) {
            if (timeDuration.getSeconds()>maxValue) {
                constraintValidatorContext.buildConstraintViolationWithTemplate(message)
                        .addConstraintViolation()
                        .disableDefaultConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
