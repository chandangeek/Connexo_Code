package com.elster.jupiter.fsm.impl.constraints;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.impl.StateImpl;
import com.elster.jupiter.util.Checks;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * Validates the {@link Unique} constraint against a {@link State}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (10:38)
 */
public class UniqueStateNameValidator implements ConstraintValidator<Unique, StateImpl> {

    @Override
    public void initialize(Unique constraintAnnotation) {
        // No need to extract information from the annotation
    }

    @Override
    public boolean isValid(StateImpl stateForValidation, ConstraintValidatorContext context) {
        if (stateForValidation.isObsolete()) {
            return true;
        }
        Optional<State> stateWithTheSameName = stateForValidation
                .getFiniteStateMachine()
                .getStates()
                .stream()
                .filter(state -> !Checks.is(state.getName()).empty()) // preserves states that have a name
                .filter(state -> state.getName().equals(stateForValidation.getName())) // preserves states that have the same name
                .filter(state -> state.getId() != stateForValidation.getId())
                .findFirst();
        if (stateWithTheSameName.isPresent()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("name")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

}