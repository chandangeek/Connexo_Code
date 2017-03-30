/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.validation.DataValidationTask;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class HasValidGroupTaskValidator implements ConstraintValidator<HasValidGroup, DataValidationTask> {

    public HasValidGroupTaskValidator() {
    }

    @Override
    public void initialize(HasValidGroup constraintAnnotation) {
    }

    @Override
    public boolean isValid(DataValidationTask validationTask, ConstraintValidatorContext context) {
        Optional<EndDeviceGroup> deviceGroup = validationTask.getEndDeviceGroup();
        Optional<UsagePointGroup> usagePointGroup = validationTask.getUsagePointGroup();


        context.disableDefaultConstraintViolation();
        switch (validationTask.getQualityCodeSystem()) {
            case MDC: {
                if (!deviceGroup.isPresent()) {
                    context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
                            .addPropertyNode("deviceGroup").addConstraintViolation();
                    return false;
                } break;
            }
            case MDM: {
                if (!usagePointGroup.isPresent()) {
                    context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
                            .addPropertyNode("usagePointGroup").addConstraintViolation();
                    return false;
                } break;
            }
        }
        return true;
    }
}