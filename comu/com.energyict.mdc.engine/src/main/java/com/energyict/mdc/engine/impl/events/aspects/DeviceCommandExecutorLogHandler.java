package com.energyict.mdc.engine.impl.events.aspects;

import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.events.EventPublishingLogHandler;
import com.energyict.mdc.engine.impl.events.logging.UnrelatedLoggingEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;

import java.util.Date;

/**
 * Provides an implementation for the log Handler interface
 * that creates simple log messages that do not relate
 * to any ComServer objects as that information is not available.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-08 (16:04)
 */
public class DeviceCommandExecutorLogHandler extends EventPublishingLogHandler {

    @Override
    protected ComServerEvent toEvent (Date eventOccurrenceTimestamp, LogLevel level, String logMessage) {
        return new UnrelatedLoggingEvent(eventOccurrenceTimestamp, level, logMessage);
    }

}