/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.time.TimeDuration;

import java.time.Instant;

public interface HighPriorityTaskScheduler extends ServerProcess {

    /**
     * Receives notification from the ComServer that the
     * scheduling interpoll delay changed.
     *
     * @param schedulingInterpollDelay The new scheduling interpoll delay
     * @see com.energyict.mdc.servers.ComServer#getSchedulingInterPollDelay()
     */
    void schedulingInterpollDelayChanged(TimeDuration schedulingInterpollDelay);

    /**
     * Gets the point in time when this HighPriorityTaskScheduler checked for work the last time
     * @return the last time this HighPriorityTaskScheduler checked for work
     */
    Instant getLastSchedulePollDate();
}
