/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import java.util.Optional;

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
        Optional<? extends EstimationRuleSet> alreadyExisting = estimationService.getEstimationRuleSet(ruleSet.getName());
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