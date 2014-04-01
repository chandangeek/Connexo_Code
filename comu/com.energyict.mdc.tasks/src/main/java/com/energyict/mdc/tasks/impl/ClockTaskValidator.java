package com.energyict.mdc.tasks.impl;

import com.energyict.mdc.tasks.ClockTask;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the ClockTask fields all at once
 */
public class ClockTaskValidator implements ConstraintValidator<ValidClockTask, ClockTask> {

    @Override
    public void initialize(ValidClockTask constraintAnnotation) {
    }

    /*
     * If type is equal to SETCLOCK, then both min. and max. difference should be filled in.
     * The min. should not be equal to the max. and max. should be larger then min.
     * <p/>
     * If type is equal to SYNCHRONIZECLOCK, then max. clock shift should be filled in
     * and have a value larger then 0s.
     * </p>
     */
    @Override
    public boolean isValid(ClockTask value, ConstraintValidatorContext context) {
        boolean valid = true;
        switch (value.getClockTaskType()) {
            case SETCLOCK:
                // both should be filled in
                if (value.getMinimumClockDifference()==null) {
                    fail(context, Constants.CAN_NOT_BE_EMPTY, ClockTaskImpl.Fields.MINIMUM_CLOCK_DIFF.fieldName());
                    valid=false;
                }
                if (value.getMaximumClockDifference()==null) {
                    fail(context, Constants.CAN_NOT_BE_EMPTY, ClockTaskImpl.Fields.MAXIMUM_CLOCK_DIFF.fieldName());
                    valid=false;
                } else {
                    if (value.getMaximumClockDifference().getCount()<=0) {
                        fail(context, Constants.TIMEDURATION_MUST_BE_POSITIVE, ClockTaskImpl.Fields.MAXIMUM_CLOCK_DIFF.fieldName());
                        valid=false;
                    }
                }
                if (value.getMinimumClockDifference()!=null && value.getMaximumClockDifference()!=null) {
                    switch (value.getMinimumClockDifference().compareTo(value.getMaximumClockDifference())) {
                        case 0:   // both can not be the same
                            fail(context, Constants.MIN_EQUALS_MAX, ClockTaskImpl.Fields.MINIMUM_CLOCK_DIFF.fieldName());
                            fail(context, Constants.MIN_EQUALS_MAX, ClockTaskImpl.Fields.MAXIMUM_CLOCK_DIFF.fieldName());
                            valid=false;
                            break;
                        case 1:   // max. should be greater then min.
                            fail(context, Constants.MIN_MUST_BE_BELOW_MAX, ClockTaskImpl.Fields.MINIMUM_CLOCK_DIFF.fieldName());
                            fail(context, Constants.MIN_MUST_BE_BELOW_MAX, ClockTaskImpl.Fields.MAXIMUM_CLOCK_DIFF.fieldName());
                            valid=false;
                            break;
                    }
                }
                break;
            case SYNCHRONIZECLOCK:
                // max. clock shift should be filled in
                if (value.getMaximumClockShift()==null) {
                    fail(context, Constants.CAN_NOT_BE_EMPTY, ClockTaskImpl.Fields.MAXIMUM_CLOCK_SHIFT.fieldName());
                    valid=false;
                } else {
                     // max. clock shift should be greater then zero
                    if (value.getMaximumClockShift().getCount()<=0) {
                        fail(context, Constants.TIMEDURATION_MUST_BE_POSITIVE, ClockTaskImpl.Fields.MAXIMUM_CLOCK_SHIFT.fieldName());
                        valid=false;
                    }
                }
                // there should be a minimum defined before synchronizing the clock (this may be zero)
                if (value.getMinimumClockDifference()==null) {
                    fail(context, Constants.CAN_NOT_BE_EMPTY, ClockTaskImpl.Fields.MINIMUM_CLOCK_DIFF.fieldName());
                    valid=false;
                }
                break;
        }

        return valid;
    }

    private void fail(ConstraintValidatorContext context, String msg, String fieldName) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("{"+ msg +"}").addPropertyNode(fieldName).addConstraintViolation();
    }
}
