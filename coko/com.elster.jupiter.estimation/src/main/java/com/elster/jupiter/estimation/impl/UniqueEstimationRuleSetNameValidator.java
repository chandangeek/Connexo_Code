/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.util.conditions.Condition;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

public class  UniqueEstimationRuleSetNameValidator implements ConstraintValidator<UniqueName, EstimationRuleSet> {

    private String message;
    private EstimationService estimationService;

    @Inject
    public UniqueEstimationRuleSetNameValidator(EstimationService validationService) {
        this.estimationService = validationService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(EstimationRuleSet ruleSet, ConstraintValidatorContext context) {
        return ruleSet == null || checkValidity(ruleSet, context);
    }

    private boolean checkValidity(EstimationRuleSet ruleSet, ConstraintValidatorContext context) {
        Condition condition = where("name").isEqualTo(ruleSet.getName()).and(where("qualityCodeSystem").isEqualTo(ruleSet.getQualityCodeSystem())).and(where(EstimationRuleSetImpl.OBSOLETE_TIME_FIELD).isNull());
        Optional<? extends EstimationRuleSet> alreadyExisting = estimationService.getEstimationRuleSetQuery().select(condition).stream().findFirst();
        return !alreadyExisting.isPresent() || !checkExisting(ruleSet, alreadyExisting.get(), context);
    }

    private boolean checkExisting(EstimationRuleSet ruleSet, EstimationRuleSet alreadyExisting, ConstraintValidatorContext context) {
        if (areNotTheSame(ruleSet, alreadyExisting)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
            return true;
        }
        return false;
    }

    private boolean areNotTheSame(EstimationRuleSet ruleSet, EstimationRuleSet alreadyExisting) {
        return ruleSet.getId() != alreadyExisting.getId();
    }


}