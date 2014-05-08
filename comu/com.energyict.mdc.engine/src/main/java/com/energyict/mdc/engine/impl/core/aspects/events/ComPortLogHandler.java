package com.energyict.mdc.engine.impl.core.aspects.events;

import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.events.EventPublishingLogHandler;
import com.energyict.mdc.engine.impl.events.logging.ComPortOperationsLoggingEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.model.ComPort;

import java.util.Date;

/**
 * Provides an implementation for the log Handler interface
 * that creates log messages that relate to the {@link ComPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (14:05)
 */
public class ComPortLogHandler extends EventPublishingLogHandler {

    private ComPort comPort;

    public ComPortLogHandler (ComPort comPort) {
        super();
        this.comPort = comPort;
    }

    @Override
    protected ComServerEvent toEvent (Date eventOccurrenceTimestamp, LogLevel level, String logMessage) {
        return new ComPortOperationsLoggingEvent(eventOccurrenceTimestamp, this.comPort, level, logMessage);
    }

}