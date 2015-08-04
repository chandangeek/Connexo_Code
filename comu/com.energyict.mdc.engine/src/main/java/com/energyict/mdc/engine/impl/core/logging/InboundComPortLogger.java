package com.energyict.mdc.engine.impl.core.logging;

import com.energyict.mdc.engine.impl.logging.Configuration;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.config.InboundComPort;

/**
 * Defines all the log messages for {@link InboundComPort}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-14 (13:34)
 */
public interface InboundComPortLogger {

    /**
     * Logs that an {@link InboundComPort} has started.
     *
     * @param threadName The name of the thread that started
     */
    @Configuration(format = "Started {0} ...", logLevel = LogLevel.INFO)
    public void started (String threadName);

    /**
     * Logs that an {@link InboundComPort} has started the shutdown proces.
     *
     * @param threadName The name of the thread that is shutting down
     */
    @Configuration(format = "Shutting down {0}...", logLevel = LogLevel.INFO)
    public void shuttingDown (String threadName);

    /**
     * Logs that the specified thread is monitoring for incoming connections.
     *
     * @param threadName The name of the thread that is listening for incoming connections
     */
    @Configuration(format = "{0} is listening for incomming connections...", logLevel = LogLevel.INFO)
    public void listening (String threadName);

}