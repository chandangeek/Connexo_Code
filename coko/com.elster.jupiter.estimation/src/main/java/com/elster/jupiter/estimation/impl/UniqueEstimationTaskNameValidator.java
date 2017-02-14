/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.util.conditions.Condition;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static com.elster.jupiter.util.conditions.Where.where;

public class UniqueEstimationTaskNameValidator implements ConstraintValidator<UniqueName, EstimationTask> {

    private String message;
    private EstimationService estimationService;

    @Inject
    public UniqueEstimationTaskNameValidator(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(EstimationTask task, ConstraintValidatorContext context) {
        Condition condition = where("id").isNotEqual(task.getId()).and(where("name").isEqualTo(task.getName()));
        if (!estimationService.getEstimationTaskQuery().select(condition).isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
            return false;
        }
        return true;
    }
}