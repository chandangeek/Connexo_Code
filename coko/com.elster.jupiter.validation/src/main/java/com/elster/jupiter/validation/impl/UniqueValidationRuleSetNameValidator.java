/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class  UniqueValidationRuleSetNameValidator implements ConstraintValidator<UniqueName, ValidationRuleSet> {

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
        return ruleSet == null || checkValidity(ruleSet, context);
    }

    private boolean checkValidity(ValidationRuleSet ruleSet, ConstraintValidatorContext context) {
        Optional<ValidationRuleSet> alreadyExisting = validationService.getValidationRuleSet(ruleSet.getName());
        return !alreadyExisting.isPresent() || !checkExisting(ruleSet, alreadyExisting.get(), context);
    }

    private boolean checkExisting(ValidationRuleSet ruleSet, ValidationRuleSet alreadyExisting, ConstraintValidatorContext context) {
        if (areNotTheSame(ruleSet, alreadyExisting)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
            return true;
        }
        return false;
    }

    private boolean areNotTheSame(ValidationRuleSet ruleSet, ValidationRuleSet alreadyExisting) {
        return ruleSet.getId() != alreadyExisting.getId();
    }


}