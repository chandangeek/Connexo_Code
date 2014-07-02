package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.tasks.ComTask;

/**
 * Models a {@link ComTaskExecution} that executes a single
 * {@link com.energyict.mdc.tasks.ComTask} according
 * to a timed schedule.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-30 (10:59)
 */
public interface ManuallyScheduledComTaskExecution extends ComTaskExecution {

    /**
     * Gets the {@link ComTask} that specifies
     * the details of this ComTaskExecution.
     *
     * @return The ComTask
     */
    public ComTask getComTask();

    /**
     * Gets the {@link ProtocolDialectConfigurationProperties}.
     */
    public ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties();

    @Override
    public ManuallyScheduledComTaskExecutionUpdater getUpdater();

}