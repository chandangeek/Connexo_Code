/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.units;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class HasQuantityValueMinValidator implements ConstraintValidator<HasQuantityValueMin, Quantity> {

    private HasQuantityValueMin constraintAnnotation;

    @Override
    public void initialize(HasQuantityValueMin constraintAnnotation) {
        this.constraintAnnotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(Quantity value, ConstraintValidatorContext context) {
        return value == null
                || value.getValue().compareTo(BigDecimal.valueOf(constraintAnnotation.min())) >= 0;
    }

}
