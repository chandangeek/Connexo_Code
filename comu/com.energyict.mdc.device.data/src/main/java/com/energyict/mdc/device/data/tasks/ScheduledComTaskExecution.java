package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.scheduling.model.ComSchedule;

import aQute.bnd.annotation.ProviderType;

/**
 * Models a {@link ComTaskExecution} that executes
 * all {@link com.energyict.mdc.tasks.ComTask}s
 * of a {@link ComSchedule} and according to the timed
 * scheduled defined in the ComSchedule.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-30 (10:59)
 */
@ProviderType
public interface ScheduledComTaskExecution extends ComTaskExecution {

    /**
     * Returns the {@link ComSchedule}.
     *
     * @return The ComSchedule
     */
    public ComSchedule getComSchedule();

    @Override
    public ScheduledComTaskExecutionUpdater getUpdater();

}