/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.model.impl;

import com.energyict.mdc.common.scheduling.NextExecutionSpecs;

import org.joda.time.DateTimeConstants;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates that the nextExecutionSpec only contains regular frequencies.
 * This means that the 'next' timestamps on each occurrence are predictable, independent of the time of creation.
 */
public class OnlyRegularFrequenciesValidator implements ConstraintValidator<OnlyRegularFrequencies, NextExecutionSpecs> {

    private static final int MONTHS_PER_YEAR = 12;
    private static final int DAYS_PER_MONTH = 31;
    private static final int WEEKS_PER_YEAR = 52;

    @Override
    public void initialize(OnlyRegularFrequencies onlyRegularFrequencies) {

    }

    @Override
    public boolean isValid(NextExecutionSpecs nextExecutionSpecs, ConstraintValidatorContext constraintValidatorContext) {
        if(nextExecutionSpecs.getTemporalExpression() != null){
            switch (nextExecutionSpecs.getTemporalExpression().getEvery().getTimeUnit()) {
                case SECONDS: {
                    return isValidFrequency(DateTimeConstants.SECONDS_PER_MINUTE, nextExecutionSpecs, constraintValidatorContext);
                }
                case MINUTES: {
                    return isValidFrequency(DateTimeConstants.MINUTES_PER_HOUR, nextExecutionSpecs, constraintValidatorContext);
                }
                case HOURS: {
                    return isValidFrequency(DateTimeConstants.HOURS_PER_DAY, nextExecutionSpecs, constraintValidatorContext);
                }
                case MONTHS: {
                    return isValidFrequency(MONTHS_PER_YEAR, nextExecutionSpecs, constraintValidatorContext);
                }
                case DAYS: {//  CONM-877 intentional fallthrough
                    int days = nextExecutionSpecs.getTemporalExpression().getEvery().getCount();
                    if(days<1 || days >31) {
                        return isValidFrequency(WEEKS_PER_YEAR, nextExecutionSpecs, constraintValidatorContext);
                    }
                    break;
                }
                case WEEKS: {// CONM-877 intentional fallthrough
                    int weeks = nextExecutionSpecs.getTemporalExpression().getEvery().getCount();
                    if (weeks<1 || weeks>52) {
                        return isValidFrequency(WEEKS_PER_YEAR, nextExecutionSpecs, constraintValidatorContext);
                    }
                    break;
                }
                case YEARS: {
                    if(nextExecutionSpecs.getTemporalExpression().getEvery().getCount() != 1){
                        addViolation(constraintValidatorContext);
                        return false;
                    } else {
                        return true;
                    }
                }
                default:
                    return false;
            }
        }
        return true;
    }

    private boolean isValidFrequency(int baseValue, NextExecutionSpecs nextExecutionSpecs, ConstraintValidatorContext constraintValidatorContext) {
        if(isIrregularFor(baseValue, nextExecutionSpecs)){
            addViolation(constraintValidatorContext);
            return false;
        } else {
            return true;
        }
    }

    private boolean isIrregularFor(int baseValue, NextExecutionSpecs nextExecutionSpecs) {
        int count = nextExecutionSpecs.getTemporalExpression().getEvery().getCount();
        return count != 0 && baseValue % count != 0;
    }

    private void addViolation(ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.
                buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.TEMPORAL_EXPRESSION_IS_NOT_REGULAR + "}").
                addPropertyNode(NextExecutionSpecsImpl.Fields.TEMPORAL_EXPRESSION.fieldName()).
                addConstraintViolation().disableDefaultConstraintViolation();
    }
}
