package com.energyict.mdc.engine.events;

import com.energyict.mdc.engine.impl.logging.LogLevel;

/**
 * Models an event that relates to a log message
 * being emitted by a ComServer component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-07 (16:41)
 */
public interface LoggingEvent extends ComServerEvent {

    /**
     * Gets the LogLevel at which the message was emitted.
     *
     * @return The LogLevel
     */
    public LogLevel getLogLevel ();

    /**
     * Gets the human readable message that was logged
     * by a ComServer component.
     *
     * @return The message
     */
    public String getLogMessage ();

}