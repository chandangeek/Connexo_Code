/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

final class SelfObjectDelegatedValidator implements ConstraintValidator<SelfValid, SelfObjectValidator> {

    @Override
    public void initialize(SelfValid constraintAnnotation) {
    }

    @Override
    public boolean isValid(SelfObjectValidator value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.validate(context);
    }
}
