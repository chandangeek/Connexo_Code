package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.scheduling.TemporalExpression;

/**
 * Models a {@link ComTaskExecutionUpdater} for a {@link ManuallyScheduledComTaskExecution}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-30 (11:41)
 */
public interface ManuallyScheduledComTaskExecutionUpdater extends ComTaskExecutionUpdater<ManuallyScheduledComTaskExecutionUpdater, ManuallyScheduledComTaskExecution> {

    public ManuallyScheduledComTaskExecutionUpdater protocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties);

    /**
     * Sets the specifications for the calculation of the next
     * execution timestamp from the {@link TemporalExpression}.
     *
     * @param temporalExpression The TemporalExpression
     */
    public ManuallyScheduledComTaskExecutionUpdater scheduleAccordingTo(TemporalExpression temporalExpression);

}