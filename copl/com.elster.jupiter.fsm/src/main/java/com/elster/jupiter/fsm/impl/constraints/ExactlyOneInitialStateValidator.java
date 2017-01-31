/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl.constraints;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.impl.FiniteStateMachineImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link ExactlyOneInitialStateValidator} constraint against a {@link FiniteStateMachine}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-16 (11:50)
 */
public class ExactlyOneInitialStateValidator implements ConstraintValidator<ExactlyOneInitialState, FiniteStateMachine> {

    @Override
    public void initialize(ExactlyOneInitialState constraintAnnotation) {
        // No need to extract information from the annotation
    }

    @Override
    public boolean isValid(FiniteStateMachine stateMachine, ConstraintValidatorContext context) {
        try {
            stateMachine.getInitialState();
            return true;
        }
        catch (IllegalStateException e) {
            context
                .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(FiniteStateMachineImpl.Fields.STATES.fieldName()).addConstraintViolation()
                .disableDefaultConstraintViolation();
            return false;
        }
    }

}