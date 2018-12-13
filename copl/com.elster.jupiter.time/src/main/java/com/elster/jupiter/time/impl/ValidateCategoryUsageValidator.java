/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class ValidateCategoryUsageValidator  implements ConstraintValidator<ValidateCategoryUsage, RelativePeriodImpl> {
    private String[] fields;
    private String message;

    @Override
    public void initialize(ValidateCategoryUsage constraintAnnotation) {
        this.message = constraintAnnotation.message();
        this.fields =  constraintAnnotation.fields();
    }

    @Override
    public boolean isValid(RelativePeriodImpl relativePeriod, ConstraintValidatorContext context) {
        if(relativePeriod.getRelativePeriodCategories().isEmpty() && !relativePeriod.isCreatedByInstaller()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode(fields[0]).addConstraintViolation();
            return false;
        }
        return true;
    }
}
