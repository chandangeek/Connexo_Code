package com.energyict.mdc.engine.status;

import org.joda.time.Duration;

/**
 * Models status information of a {@link com.energyict.mdc.engine.model.ComServer}
 * that is configured to run in this instance of the MDC application.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (10:47)
 */
public interface ComServerStatus {

    /**
     * The name of the {@link com.energyict.mdc.engine.model.ComServer}
     * for which status information is provided.
     *
     * @return The name of the ComServer for which status information is provided
     */
    public String getComServerName ();

    public ComServerType getComServerType ();

    /**
     * Tests if the {@link com.energyict.mdc.engine.model.ComServer} is actually running.
     *
     * @return A flag that indicates if the ComServer is actually running
     */
    public boolean isRunning ();

    /**
     * Tests if the {@link com.energyict.mdc.engine.model.ComServer} is blocked.
     * A ComServer is considered to be blocked if it has not checked for
     * pending tasks or modifications within the expected timeframe.
     * The timeframe to check for pending tasks is defined by
     * {@link com.energyict.mdc.engine.model.ComServer#getSchedulingInterPollDelay()}.
     * The timeframe to check for modifications tasks is defined by
     * {@link com.energyict.mdc.engine.model.ComServer#getChangesInterPollDelay()}.
     *
     * @return A flag that indicates if the ComServer is blocked
     */
    public boolean isBlocked ();

    /**
     * Returns the time that the {@link com.energyict.mdc.engine.model.ComServer} is already blocked.
     * Note that this will return null if the ComServer is not blocked,
     * i.e. the method {@link #isBlocked()} returned false.
     *
     * @return The time that the ComServer is already block
     *         or <code>null</code> if the ComServer is not blocked.
     */
    public Duration getBlockTime ();

}