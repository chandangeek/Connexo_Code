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
 * Validates the {@link AtLeastOneState} constraint against a {@link FiniteStateMachine}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (11:08)
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