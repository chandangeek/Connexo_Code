package com.elster.jupiter.fsm.impl.constraints;

import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.FinateStateMachineService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link Unique} constraint against a {@link FinateStateMachine}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (10:38)
 */
public class UniqueFinateStateMachineNameValidator implements ConstraintValidator<Unique, FinateStateMachine> {

    private final FinateStateMachineService service;

    @Inject
    public UniqueFinateStateMachineNameValidator(FinateStateMachineService service) {
        super();
        this.service = service;
    }

    @Override
    public void initialize(Unique constraintAnnotation) {
        // No need to extract information from the annotation
    }

    @Override
    public boolean isValid(FinateStateMachine finateStateMachine, ConstraintValidatorContext context) {
        return !this.service.findFinateStateMachineByName(finateStateMachine.getName()).isPresent();
    }

}