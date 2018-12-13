/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class HasValidGroupTaskValidator implements ConstraintValidator<HasValidGroup, EstimationTask> {

    public HasValidGroupTaskValidator() {
    }

    @Override
    public void initialize(HasValidGroup constraintAnnotation) {
    }

    @Override
    public boolean isValid(EstimationTask estimationTask, ConstraintValidatorContext context) {
        Optional<EndDeviceGroup> deviceGroup = estimationTask.getEndDeviceGroup();
        Optional<UsagePointGroup> usagePointGroup = estimationTask.getUsagePointGroup();


        context.disableDefaultConstraintViolation();
        switch (estimationTask.getQualityCodeSystem()) {
            case MDC: {
                if (!deviceGroup.isPresent()) {
                    context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
                            .addPropertyNode("deviceGroup").addConstraintViolation();
                    return false;
                }
                break;
            }
            case MDM: {
                if (!usagePointGroup.isPresent()) {
                    context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
                            .addPropertyNode("usagePointGroup").addConstraintViolation();
                    return false;
                }
                break;
            }
        }
        return true;
    }
}