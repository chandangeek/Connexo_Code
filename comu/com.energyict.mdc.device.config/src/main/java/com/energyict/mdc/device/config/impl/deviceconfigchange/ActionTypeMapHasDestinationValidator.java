/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.impl.MessageSeeds;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ActionTypeMapHasDestinationValidator implements ConstraintValidator<ActionTypeMapHasDestination, AbstractConflictSolution> {

    private String fieldName;

    @Override
    public void initialize(ActionTypeMapHasDestination actionTypeMapHasDestination) {
        fieldName = actionTypeMapHasDestination.getDestination();
    }

    @Override
    public boolean isValid(AbstractConflictSolution abstractConflictSolution, ConstraintValidatorContext constraintValidatorContext) {
        if (abstractConflictSolution.getConflictingMappingAction().equals(DeviceConfigConflictMapping.ConflictingMappingAction.MAP)
                && !abstractConflictSolution.getDestinationDataSourceReference().isPresent()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.DESTINATION_DATA_SOURCE_IS_EMPTY + "}")
                    .addPropertyNode(fieldName).addConstraintViolation();
            return false;
        }
        return true;
    }
}
