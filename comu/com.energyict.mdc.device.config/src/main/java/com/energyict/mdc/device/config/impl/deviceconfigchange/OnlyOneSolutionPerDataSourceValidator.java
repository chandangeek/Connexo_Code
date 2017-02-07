/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.energyict.mdc.device.config.ConflictingSolution;
import com.energyict.mdc.device.config.impl.MessageSeeds;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;

public class OnlyOneSolutionPerDataSourceValidator implements ConstraintValidator<OnlyOneSolutionPerDataSource, DeviceConfigConflictMappingImpl> {

    @Override
    public void initialize(OnlyOneSolutionPerDataSource onlyOneSolutionPerDataSource) {

    }

    @Override
    public boolean isValid(DeviceConfigConflictMappingImpl deviceConfigConflictMapping, ConstraintValidatorContext constraintValidatorContext) {
        if (multipleSecuritySetSolutionsPerDataSource(deviceConfigConflictMapping) || multipleConnectionMethodSolutionsPerDataSource(deviceConfigConflictMapping)) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.MULTIPLE_SOLUTIONS_FOR_SAME_CONFLICT + "}").addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean multipleSecuritySetSolutionsPerDataSource(DeviceConfigConflictMappingImpl deviceConfigConflictMapping) {
        return !deviceConfigConflictMapping.getConflictingSecuritySetSolutions().stream().map(ConflictingSolution::getOriginDataSource).allMatch(new HashSet<>()::add);
    }

    private boolean multipleConnectionMethodSolutionsPerDataSource(DeviceConfigConflictMappingImpl deviceConfigConflictMapping) {
        return !deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().stream().map(ConflictingSolution::getOriginDataSource).allMatch(new HashSet<>()::add);
    }
}
