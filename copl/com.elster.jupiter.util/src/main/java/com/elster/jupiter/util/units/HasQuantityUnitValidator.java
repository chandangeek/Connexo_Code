/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.units;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class HasQuantityUnitValidator implements ConstraintValidator<HasQuantityUnit, Quantity> {

    private HasQuantityUnit constraintAnnotation;

    @Override
    public void initialize(HasQuantityUnit constraintAnnotation) {
        this.constraintAnnotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(Quantity value, ConstraintValidatorContext context) {
        return value == null || Arrays.asList(constraintAnnotation.units()).contains(value.getUnit());
    }
}
