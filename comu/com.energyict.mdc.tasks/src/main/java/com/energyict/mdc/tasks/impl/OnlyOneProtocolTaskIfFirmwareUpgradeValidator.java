/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.energyict.mdc.tasks.ComTask;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates that when a ComTask is created with the FirmwareUpgrade protocolTask,
 * then that protocolTask should be the only task in the ComTask
 */
public class OnlyOneProtocolTaskIfFirmwareUpgradeValidator implements ConstraintValidator<OnlyOneProtocolTaskIfFirmwareUpgrade, ComTask> {

    @Override
    public void initialize(OnlyOneProtocolTaskIfFirmwareUpgrade onlyOneProtocolTaskIfFirmwareUpgrade) {

    }

    @Override
    public boolean isValid(ComTask comTask, ConstraintValidatorContext constraintValidatorContext) {
        if(multipleProtocolTasksWhenFirmwareUpgradeTask(comTask)){
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.
                    buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.ONLY_ONE_PROTOCOLTASK_WHEN_FIRMWARE_UPGRADE + "}").
                    addPropertyNode("protocolTasks").
                    addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean multipleProtocolTasksWhenFirmwareUpgradeTask(ComTask comTask) {
        return comTask.getProtocolTasks().stream()
                .filter(protocolTask -> ((ServerProtocolTask) protocolTask).isFirmwareUpgradeTask())
                .count() != 0
                && comTask.getProtocolTasks().size() > 1;
    }
}
