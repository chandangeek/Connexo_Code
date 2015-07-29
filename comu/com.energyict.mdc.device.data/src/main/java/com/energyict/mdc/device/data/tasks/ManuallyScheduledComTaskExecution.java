package com.energyict.mdc.device.data.tasks;

import aQute.bnd.annotation.ProviderType;

/**
 * Models a {@link ComTaskExecution} that executes a single
 * {@link com.energyict.mdc.tasks.ComTask} according
 * to a timed schedule.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-30 (10:59)
 */
@ProviderType
public interface ManuallyScheduledComTaskExecution extends SingleComTaskComTaskExecution {

    @Override
    ManuallyScheduledComTaskExecutionUpdater getUpdater();

}