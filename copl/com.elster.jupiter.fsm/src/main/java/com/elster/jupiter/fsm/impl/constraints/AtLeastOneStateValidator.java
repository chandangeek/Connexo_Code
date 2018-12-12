/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl.constraints;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.impl.FiniteStateMachineImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link AtLeastOneState} constraint against a {@link FiniteStateMachine}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (11:08)
 */
public class AtLeastOneStateValidator implements ConstraintValidator<AtLeastOneState, FiniteStateMachine> {

    @Override
    public void initialize(AtLeastOneState constraintAnnotation) {
        // No need to extract information from the annotation
    }

    @Override
    public boolean isValid(FiniteStateMachine stateMachine, ConstraintValidatorContext context) {
        if (stateMachine.getStates().isEmpty()) {
            context
                .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(FiniteStateMachineImpl.Fields.STATES.fieldName()).addConstraintViolation()
                .disableDefaultConstraintViolation();
            return false;
        }
        else {
            return true;
        }
    }

}