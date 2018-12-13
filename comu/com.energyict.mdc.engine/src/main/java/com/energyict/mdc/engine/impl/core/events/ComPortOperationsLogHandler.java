/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.events;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.events.EventPublishingLogHandler;
import com.energyict.mdc.engine.impl.events.logging.ComPortOperationsLoggingEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;

/**
 * Provides an implementation for the log Handler interface
 * that creates log messages that relate to the {@link ComPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (14:05)
 */
public class ComPortOperationsLogHandler extends EventPublishingLogHandler {

    private final ComPort comPort;

    public ComPortOperationsLogHandler(ComPort comPort, EventPublisher eventPublisher, AbstractComServerEventImpl.ServiceProvider serviceProvider) {
        super(eventPublisher, serviceProvider);
        this.comPort = comPort;
    }

    @Override
    protected ComServerEvent toEvent (AbstractComServerEventImpl.ServiceProvider serviceProvider, LogLevel level, String logMessage) {
        return new ComPortOperationsLoggingEvent(serviceProvider, this.comPort, level, logMessage);
    }

}