/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;

public class UniqueEstimationRuleNameValidator implements ConstraintValidator<UniqueName, EstimationRule> {

    private String message;

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(EstimationRule rule, ConstraintValidatorContext context) {
        return rule == null || !(hasEquallyNamedRule(rule.getRuleSet(), rule, context));
    }

    private boolean hasEquallyNamedRule(EstimationRuleSet ruleSet, EstimationRule rule, ConstraintValidatorContext context) {
        for (EstimationRule existingRule : ruleSet.getRules()) {
            if (!rule.isObsolete() && areDifferentWithSameName(rule, existingRule)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
                return true;
            }
        }
        return false;
    }

    private boolean areDifferentWithSameName(EstimationRule rule, EstimationRule existingRule) {
        return existingRule.getId() != rule.getId() && existingRule.getName().equals(rule.getName());
    }
}