/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl.constraints;

import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.StageSet;
import com.elster.jupiter.fsm.impl.StageSetImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Validates the {@link NoDuplicateStageName} constraint against a {@link StageSet}.
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
        Map<String, List<String>> names =
                stageSet
                    .getStages()
                    .stream()
                    .map(Stage::getName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(Function.identity()));
        Optional<String> duplicateName = names.keySet().stream()
                .filter(name -> names.get(name).size() > 1)
                .findAny();
        if (duplicateName.isPresent()) {
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