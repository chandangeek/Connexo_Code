/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueNameValidator implements ConstraintValidator<UniqueName, HasUniqueName> {

    private boolean caseSensitive;

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        this.caseSensitive = constraintAnnotation.caseSensitive();
    }

    @Override
    public boolean isValid(HasUniqueName value, ConstraintValidatorContext context) {
        if (!value.isValidName(this.caseSensitive)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(FirmwareCampaignImpl.Fields.NAME.fieldName())
                    .addConstraintViolation();
            return false;
        } else {
            return true;
        }
    }
}
