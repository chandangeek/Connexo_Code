package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointTransition;

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
                        && transition.getFrom().equals(value.getTo()))) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(UsagePointTransitionImpl.Fields.NAME.fieldName())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
