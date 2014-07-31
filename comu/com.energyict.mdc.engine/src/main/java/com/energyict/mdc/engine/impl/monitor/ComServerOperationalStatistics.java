package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.model.ComServer;

/**
 * Models the operational statistics that are gathered by the process
 * that monitors a RunningComServer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (10:50)
 */
public interface ComServerOperationalStatistics extends OperationalStatistics {

    /**
     * Gets the LogLevel that is used by the
     * RunningComServer
     * for global server processes.
     *
     * @return The LogLevel that is used for global server processes.
     */
    public ComServer.LogLevel getServerLogLevel ();

    /**
     * Gets the LogLevel that is used by the
     * RunningComServer
     * for processes that focus on communication with devices.
     *
     * @return The LogLevel that is used for communication.
     */
    public ComServer.LogLevel getCommunicationLogLevel ();

}