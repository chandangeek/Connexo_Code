/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueNameValidator implements ConstraintValidator<UniqueName, HasUniqueName> {
    private String message;

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(HasUniqueName value, ConstraintValidatorContext context) {
        if (!value.validateName()){ // delegate validation
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("name").addConstraintViolation();
            return false;
        }
        return true;
    }
}
