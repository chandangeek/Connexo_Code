package com.elster.jupiter.fsm.impl.constraints;

import com.elster.jupiter.fsm.FinateStateMachineService;
import com.elster.jupiter.fsm.StandardStateTransitionEventType;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link Unique} constraint against a {@link StandardStateTransitionEventType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (10:34)
 */
public class UniqueStateTransitionEventTypeValidator implements ConstraintValidator<Unique, StandardStateTransitionEventType> {

    private final FinateStateMachineService service;

    @Inject
    public UniqueStateTransitionEventTypeValidator(FinateStateMachineService service) {
        super();
        this.service = service;
    }

    @Override
    public void initialize(Unique constraintAnnotation) {
        // No need to extract information from the annotation
    }

    @Override
    public boolean isValid(StandardStateTransitionEventType stateTransitionEventType, ConstraintValidatorContext context) {
        return !this.service.findStandardStateTransitionEventType(stateTransitionEventType.getEventType()).isPresent();
    }

}