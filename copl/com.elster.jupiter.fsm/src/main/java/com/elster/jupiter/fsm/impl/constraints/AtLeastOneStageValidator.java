/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl.constraints;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.StageSet;
import com.elster.jupiter.fsm.impl.FiniteStateMachineImpl;
import com.elster.jupiter.fsm.impl.StageSetImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link AtLeastOneStage} constraint against a {@link StageSet}.
 */
public class AtLeastOneStageValidator implements ConstraintValidator<AtLeastOneStage, StageSet> {

    @Override
    public void initialize(AtLeastOneStage constraintAnnotation) {
        // No need to extract information from the annotation
    }

    @Override
    public boolean isValid(StageSet stageSet, ConstraintValidatorContext context) {
        if (stageSet.getStages().isEmpty()) {
            context
                .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(StageSetImpl.Fields.STAGES.fieldName()).addConstraintViolation()
                .disableDefaultConstraintViolation();
            return false;
        }
        else {
            return true;
        }
    }

}