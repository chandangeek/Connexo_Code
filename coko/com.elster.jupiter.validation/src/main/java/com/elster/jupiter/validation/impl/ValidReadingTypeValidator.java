package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.MeteringService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Copyrights EnergyICT
 * Date: 1/07/2014
 * Time: 15:12
 */
public class ValidReadingTypeValidator implements ConstraintValidator<ValidReadingType, String> {

    private MeteringService meteringService;

    @Inject
    public ValidReadingTypeValidator(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public void initialize(ValidReadingType constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return meteringService.getReadingType(value).isPresent();
    }
}
