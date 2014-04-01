package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueValidationRuleSetNameValidator implements ConstraintValidator<UniqueName, ValidationRuleSet> {

    private String message;
    private ValidationService validationService;

    @Inject
    public UniqueValidationRuleSetNameValidator(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(ValidationRuleSet ruleSet, ConstraintValidatorContext context) {
        if (ruleSet == null) {
            return true;
        }
        Optional optional = validationService.getValidationRuleSet(ruleSet.getName());
        if (optional.isPresent()) {
            ValidationRuleSet ruleSetWithTheSameName = (ValidationRuleSet) optional.get();
            if (ruleSetWithTheSameName != null && ruleSet.getId() != ruleSetWithTheSameName.getId()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
                return false;
            }
        }
        return true;
    }


}