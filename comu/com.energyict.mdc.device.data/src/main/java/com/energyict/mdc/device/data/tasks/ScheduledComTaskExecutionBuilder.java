package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.scheduling.model.ComSchedule;

/**
 * Builder that supports basic value setters for a {@link ScheduledComTaskExecution}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-30 (15:02)
*/
public interface ScheduledComTaskExecutionBuilder extends ComTaskExecutionBuilder<ScheduledComTaskExecutionBuilder, ScheduledComTaskExecution> {

    public ScheduledComTaskExecutionBuilder comSchedule(ComSchedule comSchedule);

}