/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl.constraints;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.StageSet;
import com.elster.jupiter.fsm.impl.StageSetImpl;
import com.elster.jupiter.util.HasName;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Validates the {@link AtLeastOneState} constraint against a {@link FiniteStateMachine}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (11:08)
 */
public class NoDuplicateStageNameValidator implements ConstraintValidator<NoDuplicateStageName, StageSet> {

    @Override
    public void initialize(NoDuplicateStageName constraintAnnotation) {
        // No need to extract information from the annotation
    }

    @Override
    public boolean isValid(StageSet stageSet, ConstraintValidatorContext context) {
        List<String> names = stageSet.getStages().stream()
                .map(HasName::getName)
                .collect(Collectors.toList());
        Optional<String> duplicateName = names.stream()
                .filter(name -> Collections.frequency(names, name) > 1)
                .findAny();
        if(duplicateName.isPresent()) {
            context
                    .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(StageSetImpl.Fields.STAGES.fieldName()).addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        } else {
            return true;
        }
    }

}