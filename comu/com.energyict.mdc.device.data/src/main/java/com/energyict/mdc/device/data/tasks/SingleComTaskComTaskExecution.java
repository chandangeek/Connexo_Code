package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.tasks.ComTask;

import aQute.bnd.annotation.ProviderType;

/**
 * Copyrights EnergyICT
 * Date: 08/06/15
 * Time: 09:58
 */
@ProviderType
public interface SingleComTaskComTaskExecution extends ComTaskExecution {
    /**
     * Gets the {@link ComTask} that specifies
     * the details of this ComTaskExecution.
     *
     * @return The ComTask
     */
    ComTask getComTask();

    /**
     * Gets the {@link ProtocolDialectConfigurationProperties}.
     */
    ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties();
}
