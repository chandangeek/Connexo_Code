/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasDifferentStatesValidator implements ConstraintValidator<HasDifferentStates, UsagePointTransition> {
    @Override
    public void initialize(HasDifferentStates constraintAnnotation) {
    }

    @Override
    public boolean isValid(UsagePointTransition value, ConstraintValidatorContext context) {
        if (value.getFrom().equals(value.getTo())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("toState")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
