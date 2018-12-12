/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.config.OutboundComPort;

/**
 * Models the scheduling aspects of a {@link OutboundComPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-03 (10:05)
 */
public interface ScheduledComPort extends ComPortServerProcess {

    /**
     * Gets the {@link OutboundComPort} that is scheduled via this ScheduledComPort.
     *
     * @return The OutboundComPort
     */
    OutboundComPort getComPort();


    /**
     * @return the number of active thread
     */
    int getActiveThreadCount();

    /**
     * Receives notification from the ComServer that the
     * changes interpoll delay changed.
     *
     * @param changesInterpollDelay The new scheduling interpoll delay
     * @see com.energyict.mdc.engine.config.ComServer#getChangesInterPollDelay()
     */
    void changesInterpollDelayChanged(TimeDuration changesInterpollDelay);

    /**
     * Receives notification from the ComServer that the
     * scheduling interpoll delay changed.
     *
     * @param schedulingInterpollDelay The new scheduling interpoll delay
     * @see com.energyict.mdc.engine.config.ComServer#getSchedulingInterPollDelay()
     */
    void schedulingInterpollDelayChanged(TimeDuration schedulingInterpollDelay);

}