/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.logging;

import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.events.LoggingEvent;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * Provides an implementation for the {@link LoggingEvent} interface
 * for events that relate to operational aspects of the high priority task scheduler.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-10-14 (13:56)
 */
public class HighPriorityTaskSchedulerOperationsLoggingEvent extends AbstractComServerEventImpl implements LoggingEvent {

    private LogLevel logLevel;
    private String logMessage;

    public HighPriorityTaskSchedulerOperationsLoggingEvent(ServiceProvider serviceProvider, LogLevel logLevel, String logMessage) {
        super(serviceProvider);
        this.logLevel = logLevel;
        this.logMessage = logMessage;
    }

    @Override
    public Category getCategory() {
        return Category.LOGGING;
    }

    @Override
    public boolean isLoggingRelated() {
        return true;
    }

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    @Override
    public String getLogMessage() {
        return logMessage;
    }

    @Override
    protected void toString(JSONWriter writer) throws JSONException {
        super.toString(writer);
        writer.
                key("log-level").
                value(String.valueOf(this.getLogLevel())).
                key("message").
                value(this.getLogMessage());
    }

}
