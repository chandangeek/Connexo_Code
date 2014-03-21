package com.energyict.mdc.task.impl;

import com.energyict.mdc.task.ClockTask;
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
            case SETCLOCK: {
                // both should be filled in
                if (value.getMinimumClockDifference()==null) {
                    fail(context, Constants.TSK_CAN_NOT_BE_EMPTY, ClockTaskImpl.Fields.MINIMUMCLOCKDIFF.getName());
                    valid=false;
                }
                if (value.getMaximumClockDifference()==null) {
                    fail(context, Constants.TSK_CAN_NOT_BE_EMPTY, ClockTaskImpl.Fields.MAXIMUMCLOCKDIFF.getName());
                    valid=false;
                }
                switch (value.getMinimumClockDifference().compareTo(value.getMaximumClockDifference())) {
                    case 0: {  // both can not be the same
                        fail(context, Constants.TSK_MIN_EQUALS_MAX, ClockTaskImpl.Fields.MINIMUMCLOCKDIFF.getName());
                        fail(context, Constants.TSK_MIN_EQUALS_MAX, ClockTaskImpl.Fields.MAXIMUMCLOCKDIFF.getName());
                        valid=false;
                    }
                    case 1: {  // max. should be greater then min.
                        fail(context, Constants.TSK_MIN_MUST_BE_BELOW_MAX, ClockTaskImpl.Fields.MINIMUMCLOCKDIFF.getName());
                        fail(context, Constants.TSK_MIN_MUST_BE_BELOW_MAX, ClockTaskImpl.Fields.MAXIMUMCLOCKDIFF.getName());
                        valid=false;
                    }
                }
                if (value.getMaximumClockDifference().getCount()<=0) {
                    fail(context, Constants.TSK_TIMEDURATION_IS_ZERO, ClockTaskImpl.Fields.MAXIMUMCLOCKDIFF.getName());
                    valid=false;
                }
            }
            break;
            case SYNCHRONIZECLOCK: {
                if (value.getMaximumClockShift()==null) {
                    fail(context, Constants.TSK_CAN_NOT_BE_EMPTY, ClockTaskImpl.Fields.MAXIMUMCLOCKSHIFT.getName());
                    valid=false;
                }
                if (value.getMinimumClockDifference()==null) {
                    if (value.getMinimumClockDifference()==null) {
                        fail(context, Constants.TSK_CAN_NOT_BE_EMPTY, ClockTaskImpl.Fields.MINIMUMCLOCKDIFF.getName());
                        valid=false;
                    }
                }
                if (value.getMaximumClockShift().getCount()<=0) {
                    fail(context, Constants.TSK_TIMEDURATION_IS_ZERO, ClockTaskImpl.Fields.MAXIMUMCLOCKSHIFT.getName());
                    valid=false;
                }
            }
        }

        return valid;
    }

    private void fail(ConstraintValidatorContext context, String msg, String fieldName) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("{"+ msg +"}").addPropertyNode(fieldName);
    }
}
