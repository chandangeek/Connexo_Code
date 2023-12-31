/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.common.scheduling.NextExecutionSpecs;

import aQute.bnd.annotation.ConsumerType;

/**
 * Partial version of a OutboundConnectionTask.
 *
 * @author sva
 * @since 21/01/13 - 15:40
 */
@ConsumerType
public interface PartialOutboundConnectionTask extends ServerPartialConnectionTask {

    /**
     * Gets the {@link OutboundComPortPool} that is used
     * by preference for actual OutboundConnectionTasks.
     *
     * @return The ComPortPool
     */
    public OutboundComPortPool getComPortPool ();

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

    void setRescheduleRetryDelay(TimeDuration rescheduleRetryDelay);

    void setTemporalExpression(TemporalExpression temporalExpression);

    TemporalExpression getTemporalExpression();
}