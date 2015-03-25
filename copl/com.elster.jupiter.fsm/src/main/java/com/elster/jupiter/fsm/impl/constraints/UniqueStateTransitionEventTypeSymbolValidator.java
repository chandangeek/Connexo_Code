package com.elster.jupiter.fsm.impl.constraints;

import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FinateStateMachineService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link Unique} constraint against a {@link CustomStateTransitionEventType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (10:34)
 */
public class UniqueStateTransitionEventTypeSymbolValidator implements ConstraintValidator<Unique, CustomStateTransitionEventType> {

    private final FinateStateMachineService service;

    @Inject
    public UniqueStateTransitionEventTypeSymbolValidator(FinateStateMachineService service) {
        super();
        this.service = service;
    }

    @Override
    public void initialize(Unique constraintAnnotation) {
        // No need to extract information from the annotation
    }

    @Override
    public boolean isValid(CustomStateTransitionEventType stateTransitionEventType, ConstraintValidatorContext context) {
        return !this.service.findCustomStateTransitionEventType(stateTransitionEventType.getSymbol()).isPresent();
    }

}