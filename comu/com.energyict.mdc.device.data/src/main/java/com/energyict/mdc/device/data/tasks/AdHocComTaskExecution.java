package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.tasks.ComTask;

/**
 * Models a {@link ComTaskExecution} that is only executed on user demand
 * and not according to a timed schedule.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-30 (10:59)
 */
public interface AdHocComTaskExecution extends ComTaskExecution {

    /**
     * Gets the {@link ComTask} that specifies
     * the details of this ComTaskExecution.
     *
     * @return The ComTask
     */
    public ComTask getComTask ();

    /**
     * Gets the {@link ProtocolDialectConfigurationProperties}.
     */
    public ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties();

    @Override
    public AdHocComTaskExecutionUpdater getUpdater();

}