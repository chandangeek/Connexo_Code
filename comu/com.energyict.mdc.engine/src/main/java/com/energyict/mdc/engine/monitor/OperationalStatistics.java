/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.monitor;

import com.elster.jupiter.time.TimeDuration;

import aQute.bnd.annotation.ProviderType;

import java.util.Date;
import java.util.Optional;

/**
 * Models the operational statistics of a {@link com.energyict.mdc.engine.impl.core.ServerProcess}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (10:50)
 */
@ProviderType
public interface OperationalStatistics {

    /**
     * Gets the timestamp on which the monitored RunningComServer was started.
     *
     * @return The timestamp on which the RunningComServer was started
     */
     Date getStartTimestamp ();

    /**
     * Gets the {@link TimeDuration} that indicates how long the RunningComServer
     * has been running for. This is a convenience method for client code
     * to not have to calculate the difference between current time
     * and the start timestamp.
     *
     * @return The TimeDuration that indicates how long the RunningComServer has been running for
     * @see #getStartTimestamp()
     */
     TimeDuration getRunningTime ();

    /**
     * Gets the {@link TimeDuration} between each poll for changes
     * that were applied to all objects that relate to the
     * RunningComServer.
     *
     * @return The TimeDuration between polls to detect changes
     */
     TimeDuration getChangesInterPollDelay ();

    /**
     * Gets the timestamp on which the RunningComServer last checked for changes.
     *
     * @return The timestamp on which the RunningComServer last checked for changes
     */
     Optional<Date> getLastCheckForChangesTimestamp ();

}