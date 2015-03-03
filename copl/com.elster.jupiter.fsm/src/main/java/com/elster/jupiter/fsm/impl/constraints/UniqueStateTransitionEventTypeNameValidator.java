package com.elster.jupiter.fsm.impl.constraints;

import com.elster.jupiter.fsm.FinateStateMachineService;
import com.elster.jupiter.fsm.StateTransitionEventType;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link UniqueName} constraint against a {@link StateTransitionEventType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (10:34)
 */
public class UniqueStateTransitionEventTypeNameValidator implements ConstraintValidator<UniqueName, StateTransitionEventType> {

    private final FinateStateMachineService service;

    @Inject
    public UniqueStateTransitionEventTypeNameValidator(FinateStateMachineService service) {
        super();
        this.service = service;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        // No need to extract information from the annotation
    }

    @Override
    public boolean isValid(StateTransitionEventType stateTransitionEventType, ConstraintValidatorContext context) {
        return !this.service.findStateTransitionEventTypeBySymbol(stateTransitionEventType.getSymbol()).isPresent();
    }

}