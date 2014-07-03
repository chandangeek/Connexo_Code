package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueValidationRuleNameValidator implements ConstraintValidator<UniqueName, ValidationRule> {

    private String message;
    private ValidationService validationService;

    @Inject
    public UniqueValidationRuleNameValidator(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(ValidationRule rule, ConstraintValidatorContext context) {
        return rule == null || !(hasEquallyNamedRule(rule.getRuleSet(), rule, context));
    }

    private boolean hasEquallyNamedRule(ValidationRuleSet ruleSet, ValidationRule rule, ConstraintValidatorContext context) {
        for (ValidationRule existingRule : ruleSet.getRules()) {
            if (areDifferent(rule, existingRule)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
                return true;
            }
        }
        return false;
    }

    private boolean areDifferent(ValidationRule rule, ValidationRule existingRule) {
        return existingRule.getName().equals(rule.getName()) && (existingRule.getId() != rule.getId());
    }


}
