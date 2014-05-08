package com.energyict.mdc.engine.monitor;

import com.energyict.mdc.common.TimeDuration;

import java.util.Date;

/**
 * Models the operational statistics of a {@link com.energyict.mdc.engine.impl.core.ServerProcess}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (10:50)
 */
public interface OperationalStatistics {

    /**
     * Gets the timestamp on which the monitored
     * RunningComServer
     * was started.
     *
     * @return The timestamp on which the RunningComServer was started
     */
    public Date getStartTimestamp ();

    /**
     * Gets the {@link TimeDuration} that indicates
     * how long the RunningComServer
     * has been running for. This is a convenience method for client code
     * to not have to calculate the difference between current time
     * and the start timestamp.
     *
     * @return The TimeDuration that indicates how long the RunningComServer has been running for
     * @see #getStartTimestamp()
     */
    public TimeDuration getRunningTime ();

    /**
     * Gets the {@link TimeDuration} between each poll for changes
     * that were applied to all objects that relate to the
     * RunningComServer.
     *
     * @return The TimeDuration between polls to detect changes
     */
    public TimeDuration getChangesInterPollDelay ();

    /**
     * Gets the timestamp on which the
     * RunningComServer
     * last checked for changes.
     *
     * @return The timestamp on which the RunningComServer last checked for changes
     */
    public Date getLastCheckForChangesTimestamp ();

    /**
     * Sets the timestamp on which the
     * RunningComServer
     * last checked for changes.
     *
     * @param lastCheckForChangesTimestamp The timestamp on which the RunningComServer last checked for changes
     */
    public void setLastCheckForChangesTimestamp (Date lastCheckForChangesTimestamp);

}