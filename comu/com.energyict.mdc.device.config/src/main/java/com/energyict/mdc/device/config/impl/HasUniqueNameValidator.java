/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link HasUniqueName} constraint against a {@link PersistentNamedObject}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-14 (14:43)
 */
public class HasUniqueNameValidator implements ConstraintValidator<HasUniqueName, PersistentNamedObject> {

    @Override
    public void initialize(HasUniqueName constraintAnnotation) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(PersistentNamedObject namedObject, ConstraintValidatorContext context) {
        if (!namedObject.validateUniqueName()) {
            context.disableDefaultConstraintViolation();
            context
                .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("name").addConstraintViolation();
            return false;
        }
        else {
            return true;
        }
    }

}