package com.energyict.mdc.device.data.tasks;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the ComTaskExecution that is used for a FirmwareUpgrade.
 * This ComTaskExecution can only serve the 'Firmware Management' ComTask.
 */
@ProviderType
public interface FirmwareComTaskExecution extends SingleComTaskComTaskExecution {

    @Override
    FirmwareComTaskExecutionUpdater getUpdater();
}
