package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.scheduling.TemporalExpression;

/**
 * Builder that supports basic value setters for a {@link ManuallyScheduledComTaskExecution}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-30 (11:41)
 */
public interface ManuallyScheduledComTaskExecutionBuilder extends ComTaskExecutionBuilder<ManuallyScheduledComTaskExecutionBuilder, ManuallyScheduledComTaskExecution> {

    /**
     * Sets the specifications for the calculation of the next
     * execution timestamp from the {@link TemporalExpression}.
     *
     * @param temporalExpression The TemporalExpression
     */
    public ManuallyScheduledComTaskExecutionBuilder scheduleAccordingTo(TemporalExpression temporalExpression);

}