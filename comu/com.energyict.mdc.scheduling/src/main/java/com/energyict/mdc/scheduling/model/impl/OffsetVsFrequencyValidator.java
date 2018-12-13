/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.scheduling.NextExecutionSpecs;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OffsetVsFrequencyValidator implements ConstraintValidator<OffsetNotGreaterThanFrequency, NextExecutionSpecs> {


        @Override
        public void initialize(OffsetNotGreaterThanFrequency constraintAnnotation) {
        }

        @Override
        public boolean isValid(NextExecutionSpecs value, ConstraintValidatorContext context) {
            if (value.getTemporalExpression() == null) {
                return true; // checked by a different validation
            }
            TimeDuration offset = value.getTemporalExpression().getOffset();
            TimeDuration every = value.getTemporalExpression().getEvery();
            return offset == null || every.getSeconds() >= offset.getSeconds();
        }
    }
