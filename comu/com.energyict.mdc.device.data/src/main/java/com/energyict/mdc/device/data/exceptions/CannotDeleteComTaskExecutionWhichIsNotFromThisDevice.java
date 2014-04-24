package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

/**
 * Models the exceptional situation that occurs when an attempt was made to delete a
 * ComTaskExecution from a Device which was not owned by the Device
 * <p/>
 * Copyrights EnergyICT
 * Date: 15/04/14
 * Time: 13:57
 */
public class CannotDeleteComTaskExecutionWhichIsNotFromThisDevice extends LocalizedException {

    public CannotDeleteComTaskExecutionWhichIsNotFromThisDevice(Thesaurus thesaurus, ComTaskExecution comTaskExecution, Device device) {
        super(thesaurus, MessageSeeds.COM_TASK_EXECUTION_CANNOT_DELETE_IF_NOT_FROM_DEVICE, comTaskExecution.getComTask().getName(), device.getName());
        this.set("comTask", comTaskExecution.getComTask().getName());
        this.set("device", device.getName());
    }
}
