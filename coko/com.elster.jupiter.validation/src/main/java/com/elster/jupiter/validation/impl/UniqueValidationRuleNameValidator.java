package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

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
        if (rule == null) {
            return true;
        }
        Optional optional = validationService.getValidationRuleSet(rule.getRuleSet().getId());
        if (!optional.isPresent()) {
            return true;
        } else {
            ValidationRuleSet ruleSet = (ValidationRuleSet) optional.get();
            List<ValidationRule> rules = (List<ValidationRule>) ruleSet.getRules();
            for (ValidationRule existingRule : rules) {
                if (existingRule.getName().equals(rule.getName()) && (existingRule.getId() != rule.getId())) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
                    return false;
                }
            }

        }
        return true;
    }


}
