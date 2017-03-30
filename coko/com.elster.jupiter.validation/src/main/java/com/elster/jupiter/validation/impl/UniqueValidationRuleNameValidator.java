/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueValidationRuleNameValidator implements ConstraintValidator<UniqueName, ValidationRule> {

    private String message;

    @Inject
    public UniqueValidationRuleNameValidator() {
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(ValidationRule rule, ConstraintValidatorContext context) {
        return rule == null || !(hasEquallyNamedRule(rule.getRuleSetVersion(), rule, context));
    }

    private boolean hasEquallyNamedRule(ValidationRuleSetVersion ruleSetVersion, ValidationRule rule, ConstraintValidatorContext context) {
        for (ValidationRule existingRule : ruleSetVersion.getRules()) {
            if (!rule.isObsolete() && areDifferentWithSameName(rule, existingRule)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
                return true;
            }
        }
        return false;
    }

    private boolean areDifferentWithSameName(ValidationRule rule, ValidationRule existingRule) {
        return existingRule.getName().equals(rule.getName()) && (existingRule.getId() != rule.getId());
    }


}
