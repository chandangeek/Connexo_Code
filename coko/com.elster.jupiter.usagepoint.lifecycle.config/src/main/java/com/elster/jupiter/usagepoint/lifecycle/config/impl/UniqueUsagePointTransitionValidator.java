/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueUsagePointTransitionValidator implements ConstraintValidator<Unique, UsagePointTransition> {
    @Override
    public void initialize(Unique constraintAnnotation) {
    }

    @Override
    public boolean isValid(UsagePointTransition value, ConstraintValidatorContext context) {
        if (value.getLifeCycle().getTransitions()
                .stream()
                .anyMatch(transition -> transition.getId() != value.getId()
                        && transition.getName().equals(value.getName())
                        && transition.getFrom().equals(value.getFrom()))) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(UsagePointTransitionImpl.Fields.NAME.fieldName())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
