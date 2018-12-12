/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.accessors;

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
        message = maxTimeDuration.message();
        maxValue = maxTimeDuration.max();
    }

    @Override
    public boolean isValid(TimeDuration timeDuration, ConstraintValidatorContext constraintValidatorContext) {
        if (timeDuration != null && timeDuration.getCount() != 0) {
            if (timeDuration.getSeconds() > maxValue || timeDuration.getSeconds() < 0) {
                constraintValidatorContext.buildConstraintViolationWithTemplate(message)
                        .addConstraintViolation()
                        .disableDefaultConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
