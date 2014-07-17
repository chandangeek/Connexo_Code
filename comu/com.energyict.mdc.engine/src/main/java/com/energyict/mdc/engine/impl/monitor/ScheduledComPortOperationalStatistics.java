package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.common.TimeDuration;

import java.util.Date;
import java.util.List;

/**
 * Models the operational statistics that are gathered by the process
 * that monitors a ScheduledComPort.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (10:50)
 */
public interface ScheduledComPortOperationalStatistics extends OperationalStatistics {

    /**
     * Gets the {@link TimeDuration} between each poll for communication
     * work that needs to be done.
     *
     * @return The TimeDuration between polls to find communication tasks
     */
    public TimeDuration getSchedulingInterPollDelay ();

    /**
     * Gets the timestamp on which the ScheduledComPort last checked for work.
     *
     * @return The timestamp on which the ScheduledComPort last checked for work
     */
    public Date getLastCheckForWorkTimestamp ();

    /**
     * Sets the timestamp on which the ScheduledComPort last checked for work.
     *
     * @param lastCheckForWorkTimestamp The timestamp
     */
    public void setLastCheckForWorkTimestamp(Date lastCheckForWorkTimestamp);

}