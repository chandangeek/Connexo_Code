/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.FirmwareManagementTask;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ComTaskMustBeFirmwareManagementValidation implements ConstraintValidator<ComTaskMustBeFirmwareManagement, ComTaskExecutionImpl> {

    @Override
    public void initialize(ComTaskMustBeFirmwareManagement comTaskMustBeFirmwareManagement) {

    }

    @Override
    public boolean isValid(ComTaskExecutionImpl firmwareComTaskExecution, ConstraintValidatorContext constraintValidatorContext) {
        if (firmwareComTaskExecution.isFirmware()) {
            ComTask firmwareComTask = firmwareComTaskExecution.getComTask();
            if(firmwareComTask.isUserComTask() || !containsFirmwareProtocolTask(firmwareComTask)){
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.
                        buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.FIRMWARE_COMTASKEXEC_NEEDS_FIRMAWARE_COMTASKENABLEMENT+ "}").
                        addPropertyNode("comTask").
                        addConstraintViolation();
                return false;
            }
        }
        return true;
    }

    private boolean containsFirmwareProtocolTask(ComTask comTask) {
        return comTask.getProtocolTasks().stream().filter(protocolTask -> protocolTask instanceof FirmwareManagementTask).count() != 0;
    }
}
