package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.NextExecutionSpecs;
import com.energyict.mdc.engine.model.OutboundComPortPool;

/**
 * Partial version of a ScheduledConnectionTask
 *
 * @author sva
 * @since 21/01/13 - 15:40
 */
public interface PartialScheduledConnectionTask extends PartialConnectionTask {

    /**
     * Gets the specifications for the calculation of the next
     * execution timestamp of the ScheduledConnectionTask
     *
     * @return The NextExecutionSpecs
     */
    public NextExecutionSpecs getNextExecutionSpecs();

      /**
     * Defines the delay before rescheduling this ConnectionTask after a fail
     *
     * @return the delay to wait before we may retry after a failing sessions
     */
    public TimeDuration getRescheduleDelay();


    void setNextExecutionSpecs(NextExecutionSpecs nextExecutionSpec);

    void setComportPool(OutboundComPortPool comPortPool);

    @Override
    OutboundComPortPool getComPortPool();
}
