/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl.constraints;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StageSet;
import com.elster.jupiter.fsm.impl.StageSetImpl;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * Validates the {@link Unique} constraint against a {@link com.elster.jupiter.fsm.StageSet}.
 */
public class UniqueStageSetNameValidator implements ConstraintValidator<Unique, StageSet> {

    private final FiniteStateMachineService service;

    @Inject
    public UniqueStageSetNameValidator(FiniteStateMachineService service) {
        super();
        this.service = service;
    }

    @Override
    public void initialize(Unique constraintAnnotation) {
        // No need to extract information from the annotation
    }

    @Override
    public boolean isValid(StageSet stageSet, ConstraintValidatorContext context) {
        Optional<StageSet> set = this.service.findStageSetByName(stageSet.getName());
        if (set.isPresent() && set.get().getId() != stageSet.getId()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(StageSetImpl.Fields.NAME.fieldName()).addConstraintViolation();
            return false;
        }
        return true;
    }

}