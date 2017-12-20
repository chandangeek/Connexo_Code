/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import aQute.bnd.annotation.ProviderType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@ProviderType
public class UniqueNameValidator implements ConstraintValidator<UniqueName, ShouldHaveUniqueName> {
    private String message;

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(ShouldHaveUniqueName value, ConstraintValidatorContext context) {
        if (!value.hasUniqueName()) { // delegate validation
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
            return false;
        }
        return true;
    }
}
