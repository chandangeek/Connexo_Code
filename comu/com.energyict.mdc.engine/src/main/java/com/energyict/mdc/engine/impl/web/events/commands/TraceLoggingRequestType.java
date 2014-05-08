package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.engine.impl.logging.LogLevel;

/**
 * Provides an implementation for the {@link RequestType} interface
 * for event registration requests for trace log messages.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (16:45)
 */
public class TraceLoggingRequestType extends LoggingRequestType {

    @Override
    protected String getLogLevelName () {
        return "tracing";
    }

    @Override
    protected LogLevel getLogLevel () {
        return LogLevel.TRACE;
    }

}