/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.filtering;

import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.events.LoggingEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;

/**
 * Provides an implementation for the {@link EventFilterCriterion} interface
 * that will filter {@link LoggingEvent}s when they are emitted
 * at a {@link LogLevel} strictly higher then the one specified.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-16 (12:03)
 */
public class LogLevelFilter implements EventFilterCriterion {

    private LogLevel logLevel;

    public LogLevelFilter (LogLevel logLevel) {
        super();
        this.logLevel = logLevel;
    }

    @Override
    public boolean matches (ComServerEvent event) {
        if (event.isLoggingRelated()) {
            LoggingEvent loggingEvent = (LoggingEvent) event;
            return loggingEvent.getLogLevel().compareTo(this.logLevel) > 0;
        }
        else {
            return false;
        }
    }

    public LogLevel getLogLevel () {
        return logLevel;
    }

    public void setLogLevel (LogLevel logLevel) {
        this.logLevel = logLevel;
    }

}