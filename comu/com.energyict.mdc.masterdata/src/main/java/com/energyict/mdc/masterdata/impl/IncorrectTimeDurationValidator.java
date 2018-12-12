/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IncorrectTimeDurationValidator implements ConstraintValidator<IncorrectTimeDuration, TimeDuration> {

    @Override
    public void initialize(IncorrectTimeDuration constraintAnnotation) {
        // nothing atm
    }

    @Override
    public boolean isValid(TimeDuration interval, ConstraintValidatorContext context) {
        if (interval == null || interval.isEmpty()) {
            context.disableDefaultConstraintViolation();

            context.buildConstraintViolationWithTemplate(
                    "{" + MessageSeeds.FIELD_IS_REQUIRED.getKey() + "}").addConstraintViolation();
            return false;
        }
        if ((interval.getTimeUnit() == TimeDuration.TimeUnit.WEEKS)) {
            context.disableDefaultConstraintViolation();

            context.buildConstraintViolationWithTemplate(
                    "{" + MessageSeeds.LOAD_PROFILE_TYPE_INTERVAL_IN_WEEKS_IS_NOT_SUPPORTED.getKey() + "}").addConstraintViolation();
            return false;
        }
        if (countMustBeOneFor(interval) && interval.getCount() != 1) {
            context.disableDefaultConstraintViolation();

            switch (interval.getTimeUnit()) {
                case DAYS: {
                    context.buildConstraintViolationWithTemplate(
                            "{" + MessageSeeds.INTERVAL_IN_DAYS_MUST_BE_ONE.getKey() + "}").addConstraintViolation();
                    break;
                }
                case MONTHS: {
                    context.buildConstraintViolationWithTemplate(
                            "{" + MessageSeeds.INTERVAL_IN_MONTHS_MUST_BE_ONE.getKey() + "}").addConstraintViolation();
                    break;

                }
                case YEARS: {
                    context.buildConstraintViolationWithTemplate(
                            "{" + MessageSeeds.INTERVAL_IN_YEARS_MUST_BE_ONE.getKey() + "}").addConstraintViolation();
                    break;
                }
                default: {
                    assert false : "Unknown TimeDuration that is supposed not to support multiples";
                    break;
                }
            }
            return false;
        }
        if ((interval.getCount() <= 0)) {
            context.disableDefaultConstraintViolation();

            context.buildConstraintViolationWithTemplate(
                    "{" + MessageSeeds.INTERVAL_MUST_BE_STRICTLY_POSITIVE.getKey() + "}").addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean countMustBeOneFor(TimeDuration interval) {
        return interval.getTimeUnit() == TimeDuration.TimeUnit.DAYS || interval.getTimeUnit() == TimeDuration.TimeUnit.MONTHS || interval.getTimeUnit() == TimeDuration.TimeUnit.YEARS;
    }
}
