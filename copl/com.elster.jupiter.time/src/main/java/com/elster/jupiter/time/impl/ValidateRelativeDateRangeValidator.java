/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.impl;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.ZonedDateTime;

public class ValidateRelativeDateRangeValidator implements ConstraintValidator<ValidateRelativeDateRange, RelativePeriodImpl> {
    private String[] fields;
    private String message;

    @Override
    public void initialize(ValidateRelativeDateRange constraintAnnotation) {
        this.message = constraintAnnotation.message();
        this.fields =  constraintAnnotation.fields();
    }

    @Override
    public boolean isValid(RelativePeriodImpl relativePeriod, ConstraintValidatorContext context) {
        ZonedDateTime time = ZonedDateTime.now();
        if(relativePeriod.getRelativeDateFrom() != null && relativePeriod.getRelativeDateTo() != null
                && relativePeriod.getRelativeDateFrom().getRelativeDate(time).isAfter(relativePeriod.getRelativeDateTo().getRelativeDate(time))) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode(fields[0]).addConstraintViolation();
            return false;
        }
        return true;
    }
}
