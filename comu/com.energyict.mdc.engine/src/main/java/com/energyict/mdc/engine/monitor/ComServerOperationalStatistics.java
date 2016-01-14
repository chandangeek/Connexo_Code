package com.energyict.mdc.engine.monitor;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.monitor.ServerOperationalStatistics;

/**
 * Models the operational statistics that are gathered by the process
 * that monitors a RunningComServer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (10:50)
 */
@ProviderType
public interface ComServerOperationalStatistics extends ServerOperationalStatistics {

    /**
     * Gets the LogLevel that is used by the
     * RunningComServer
     * for global server processes.
     *
     * @return The LogLevel that is used for global server processes.
     */
     ComServer.LogLevel getServerLogLevel ();

    /**
     * Gets the LogLevel that is used by the
     * RunningComServer
     * for processes that focus on communication with devices.
     *
     * @return The LogLevel that is used for communication.
     */
     ComServer.LogLevel getCommunicationLogLevel ();

}