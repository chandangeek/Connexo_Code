package com.elster.jupiter.fsm.impl.constraints;

import com.elster.jupiter.fsm.FinateStateMachineService;
import com.elster.jupiter.fsm.State;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link UniqueName} constraint against a {@link State}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (10:38)
 */
public class UniqueStateNameValidator implements ConstraintValidator<UniqueName, State> {

    private final FinateStateMachineService service;

    @Inject
    public UniqueStateNameValidator(FinateStateMachineService service) {
        super();
        this.service = service;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        // No need to extract information from the annotation
    }

    @Override
    public boolean isValid(State state, ConstraintValidatorContext context) {
        return state.getFinateStateMachine()
                    .getStates()
                    .stream()
                    .map(State::getName)
                    .filter(this::isNotNull)
                    .filter(stateName -> stateName.equals(state.getName()))
                    .count() <= 1;
    }

    private boolean isNotNull(String mistery) {
        return mistery != null && !mistery.isEmpty();
    }

}