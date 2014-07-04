package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.ValidatorNotFoundException;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ExistingValidatorValidator implements ConstraintValidator<ExistingValidator, String> {

    private ValidationService validationService;

    @Inject
    public ExistingValidatorValidator(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public void initialize(ExistingValidator constraintAnnotation) {
        // nothing atm
    }

    @Override
    public boolean isValid(String implementation, ConstraintValidatorContext context) {
        try {
            validationService.getValidator(implementation);
            return true;
        } catch (ValidatorNotFoundException e) {
            context.disableDefaultConstraintViolation();

            context.buildConstraintViolationWithTemplate(
                    "{"+ MessageSeeds.NO_SUCH_VALIDATOR.getKey()+"}").addConstraintViolation();
            return false;
        }
    }
}
