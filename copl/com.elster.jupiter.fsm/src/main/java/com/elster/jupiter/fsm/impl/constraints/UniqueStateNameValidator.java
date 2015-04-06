package com.elster.jupiter.fsm.impl.constraints;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.util.Checks;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.stream.Collectors;

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
    public boolean isValid(State stateForValidation, ConstraintValidatorContext context) {
        List<State> statesWithTheSameName = stateForValidation.getFiniteStateMachine().getStates().stream()
                .filter(state -> !Checks.is(state.getName()).empty()) // preserve states only with name
                .filter(state -> state.getName().equals(stateForValidation.getName())) // states only with the same name
                .collect(Collectors.toList());
        if (!statesWithTheSameName.isEmpty() && (statesWithTheSameName.size() > 1 || statesWithTheSameName.get(0).getId() != stateForValidation.getId())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("name").addConstraintViolation();
            return false;
        }
        return true;
    }
}