/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl.constraints;

import com.elster.jupiter.fsm.MessageSeeds;
import com.elster.jupiter.fsm.StandardStateTransitionEventType;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.impl.StateTransitionImpl;
import com.elster.jupiter.util.Checks;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Validates the {@link Unique} constraint against a {@link StateTransition}
 * making sure that it is impossible to create two transitions between
 * the same source and target {@link State}s with the same {@link StandardStateTransitionEventType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-07 (11:56)
 */
public class UniqueStateTransitionValidator implements ConstraintValidator<Unique, StateTransitionImpl> {

    @Override
    public void initialize(Unique constraintAnnotation) {
        // No need to extract information from the annotation
    }

    @Override
    public boolean isValid(StateTransitionImpl transition, ConstraintValidatorContext context) {
        long numberOfOtherTransitions = transition
                .getFiniteStateMachine()
                .getTransitions()
                .stream()
                .map(StateTransitionImpl.class::cast)
                .filter(t -> t.duplicates(transition))
                .count();
        if (numberOfOtherTransitions == 0) {
            return true;
        }
        else {
            context.disableDefaultConstraintViolation();
            context
                .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("duplicate")
                .addConstraintViolation();
            return false;
        }
    }

}