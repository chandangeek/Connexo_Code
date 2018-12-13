/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.metering.MeteringService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

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
