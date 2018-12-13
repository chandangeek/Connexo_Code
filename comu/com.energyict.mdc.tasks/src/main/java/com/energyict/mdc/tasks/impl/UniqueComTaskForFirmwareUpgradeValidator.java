/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.stream.Stream;

/**
 * Validates if there is only one ComTask with the FirmwareUpgrade ProtocolTask
 */
public class UniqueComTaskForFirmwareUpgradeValidator implements ConstraintValidator<UniqueComTaskForFirmwareUpgrade, ComTask> {

    private final TaskService taskService;

    @Inject
    public UniqueComTaskForFirmwareUpgradeValidator(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void initialize(UniqueComTaskForFirmwareUpgrade uniqueComTaskForFirmwareUpgrade) {
    }

    @Override
    public boolean isValid(ComTask comTask, ConstraintValidatorContext constraintValidatorContext) {
        if (areThereMultipleComTasksWithFirmware(comTask)) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.
                    buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.ONLY_ONE_COMTASK_WITH_FIRMWARE_ALLOWED + "}").
                    addPropertyNode("name").
                    addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean areThereMultipleComTasksWithFirmware(ComTask comTask) {
        return Stream.concat(this.taskService.findAllComTasks().stream(), Stream.of(comTask))
                .filter(this::containsFirmwareProtocolTask).count() > 1;
    }

    private boolean containsFirmwareProtocolTask(ComTask comTask) {
        return comTask.getProtocolTasks().stream().filter(protocolTask -> ((ServerProtocolTask) protocolTask).isFirmwareUpgradeTask()).count() != 0;
    }
}
