package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.FirmwareManagementTask;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Copyrights EnergyICT
 * Date: 3/16/15
 * Time: 4:54 PM
 */
public class ComTaskMustBeFirmwareManagementValidation implements ConstraintValidator<ComTaskMustBeFirmwareManagement, FirmwareComTaskExecutionImpl> {

    @Override
    public void initialize(ComTaskMustBeFirmwareManagement comTaskMustBeFirmwareManagement) {

    }

    @Override
    public boolean isValid(FirmwareComTaskExecutionImpl firmwareComTaskExecution, ConstraintValidatorContext constraintValidatorContext) {
        ComTask firmwareComTask = firmwareComTaskExecution.getComTask();
        if(firmwareComTask.isUserComTask() || !containsFirmwareProtocolTask(firmwareComTask)){
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.
                    buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.FIRMWARE_COMTASKEXEC_NEEDS_FIRMAWARE_COMTASKENABLEMENT+ "}").
                    addPropertyNode("comTask").
                    addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean containsFirmwareProtocolTask(ComTask comTask) {
        return comTask.getProtocolTasks().stream().filter(protocolTask -> protocolTask instanceof FirmwareManagementTask).count() != 0;
    }
}
