package com.elster.jupiter.fsm.impl.constraints;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.util.Checks;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link Unique} constraint against a {@link State}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (10:38)
 */
public class UniqueStateNameValidator implements ConstraintValidator<Unique, State> {

    @Override
    public void initialize(Unique constraintAnnotation) {
        // No need to extract information from the annotation
    }

    @Override
    public boolean isValid(State state, ConstraintValidatorContext context) {
        return state.getFiniteStateMachine()
                    .getStates()
                    .stream()
                    .map(State::getName)
                    .filter(stateName -> !Checks.is(stateName).empty())
                    .filter(stateName -> stateName.equals(state.getName()))
                    .count() <= 1;
    }

}