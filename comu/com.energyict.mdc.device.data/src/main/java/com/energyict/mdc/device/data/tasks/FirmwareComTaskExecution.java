package com.energyict.mdc.device.data.tasks;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.tasks.ComTask;

/**
 * Models the ComTaskExecution that is used for a FirmwareUpgrade.
 * This ComTaskExecution can only serve the 'Firmware Management' ComTask.
 */
@ProviderType
public interface FirmwareComTaskExecution extends ComTaskExecution {

    /**
     * Gets the {@link com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties}.
     */
    ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties();

    @Override
    FirmwareComTaskExecutionUpdater getUpdater();

    ComTask getComTask();
}
