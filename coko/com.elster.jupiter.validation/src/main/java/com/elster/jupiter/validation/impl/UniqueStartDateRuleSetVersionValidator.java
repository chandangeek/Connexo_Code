/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueStartDateRuleSetVersionValidator implements ConstraintValidator<UniqueStartDate, ValidationRuleSetVersion> {

    private String message;
    private ValidationService validationService;

    @Inject
    public UniqueStartDateRuleSetVersionValidator(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public void initialize(UniqueStartDate constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(ValidationRuleSetVersion ruleSetVersion, ConstraintValidatorContext context) {
        return ruleSetVersion == null || !startOnSameDate(ruleSetVersion, context);
    }

    private boolean startOnSameDate(ValidationRuleSetVersion ruleSetVersion, ConstraintValidatorContext context) {
        if(!ruleSetVersion.isObsolete()) {
            if (ruleSetVersion
                    .getRuleSet()
                    .getRuleSetVersions()
                    .stream()
                    .filter(v -> !v.isObsolete())
                    .filter(v -> v.getId() != ruleSetVersion.getId())
                    .map(IValidationRuleSetVersion.class::cast)
                    .anyMatch(v -> v.getNotNullStartDate().equals(((IValidationRuleSetVersion) ruleSetVersion).getNotNullStartDate()))) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
                return true;
            }
        }
        return false;

    }


}