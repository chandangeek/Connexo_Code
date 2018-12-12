/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.records;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasUniqueNameValidator implements ConstraintValidator<HasUniqueName, UniqueNamed> {

    private boolean caseSensitive;

    @Override
    public void initialize(HasUniqueName constraintAnnotation) {
        this.caseSensitive = constraintAnnotation.caseSensitive();
    }

    @Override
    public boolean isValid(UniqueNamed namedObject, ConstraintValidatorContext context) {
        if (!namedObject.validateUniqueName(this.caseSensitive)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("name").addConstraintViolation();
            return false;
        } else {
            return true;
        }
    }

}