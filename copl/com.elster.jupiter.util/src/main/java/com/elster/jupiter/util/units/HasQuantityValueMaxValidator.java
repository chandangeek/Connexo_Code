/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.units;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class HasQuantityValueMaxValidator implements ConstraintValidator<HasQuantityValueMax, Quantity> {

    private HasQuantityValueMax constraintAnnotation;

    @Override
    public void initialize(HasQuantityValueMax constraintAnnotation) {
        this.constraintAnnotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(Quantity value, ConstraintValidatorContext context) {
        return value == null
                || value.getValue().compareTo(BigDecimal.valueOf(constraintAnnotation.max())) <= 0;
    }

}
