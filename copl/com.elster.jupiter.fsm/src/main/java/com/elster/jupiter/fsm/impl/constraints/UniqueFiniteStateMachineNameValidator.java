/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl.constraints;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * Validates the {@link Unique} constraint against a {@link FiniteStateMachine}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (10:38)
 */
public class UniqueFiniteStateMachineNameValidator implements ConstraintValidator<Unique, FiniteStateMachine> {

    private final FiniteStateMachineService service;

    @Inject
    public UniqueFiniteStateMachineNameValidator(FiniteStateMachineService service) {
        super();
        this.service = service;
    }

    @Override
    public void initialize(Unique constraintAnnotation) {
        // No need to extract information from the annotation
    }

    @Override
    public boolean isValid(FiniteStateMachine finiteStateMachine, ConstraintValidatorContext context) {
        Optional<FiniteStateMachine> stateMachine = this.service.findFiniteStateMachineByName(finiteStateMachine.getName());
        if (stateMachine.isPresent() && stateMachine.get().getId() != finiteStateMachine.getId()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("name").addConstraintViolation();
            return false;
        }
        return true;
    }

}