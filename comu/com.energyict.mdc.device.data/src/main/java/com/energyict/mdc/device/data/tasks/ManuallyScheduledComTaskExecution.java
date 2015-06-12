package com.energyict.mdc.device.data.tasks;

/**
 * Models a {@link ComTaskExecution} that executes a single
 * {@link com.energyict.mdc.tasks.ComTask} according
 * to a timed schedule.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-30 (10:59)
 */
public interface ManuallyScheduledComTaskExecution extends SingleComTaskComTaskExecution {

    @Override
    ManuallyScheduledComTaskExecutionUpdater getUpdater();

}