package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointTransition;

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
