/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.energyict.mdc.firmware.FirmwareManagementOptions;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class FirmwareManagementOptionHasAtLeastOneOptionValidator  implements ConstraintValidator<FirmwareManagementOptionHasAtLeastOneOption, FirmwareManagementOptions> {

    @Override
    public void initialize(FirmwareManagementOptionHasAtLeastOneOption constraintAnnotation) {
        // nothing
    }

    @Override
    public boolean isValid(FirmwareManagementOptions value, ConstraintValidatorContext context) {
        if (value.getOptions().isEmpty()){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("allowedOptions")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
