/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.events;

import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.events.EventPublishingLogHandler;
import com.energyict.mdc.engine.impl.events.logging.CommunicationLoggingEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;

/**
 * Provides an implementation for the log Handler interface
 * that creates log messages that relate to the
 * {@link ComPort} and the {@link ConnectionTask}
 * of an communication session.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-08 (13:44)
 */
public class ExecutionContextLogHandler extends EventPublishingLogHandler {

    private final ExecutionContext executionContext;

    protected ExecutionContextLogHandler(EventPublisher eventPublisher, AbstractComServerEventImpl.ServiceProvider serviceProvider, ExecutionContext executionContext) {
        super(eventPublisher, serviceProvider);
        this.executionContext = executionContext;
    }

    @Override
    protected ComServerEvent toEvent (AbstractComServerEventImpl.ServiceProvider serviceProvider, LogLevel level, String logMessage) {
        ConnectionTask connectionTask = this.executionContext.getConnectionTask();
        ComPort comPort = this.executionContext.getComPort();
        return new CommunicationLoggingEvent(serviceProvider, connectionTask, comPort, level, logMessage);
    }

}