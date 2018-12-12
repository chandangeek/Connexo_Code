/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.events;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.events.EventPublishingLogHandler;
import com.energyict.mdc.engine.impl.events.logging.ComCommandLoggingEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;

/**
 * Provides an implementation for the log Handler interface
 * that creates simple log messages for a {@link ComTaskExecution comTaskExecution}
 * of a {@link ConnectionTask connectionTask} on a specific {@link ComPort comPort}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-07 (16:58)
 */
public class ComCommandLogHandler extends EventPublishingLogHandler {

    private final ComPort comPort;
    private final ConnectionTask connectionTask;
    private final ComTaskExecution comTaskExecution;

    public ComCommandLogHandler(EventPublisher eventPublisher, AbstractComServerEventImpl.ServiceProvider serviceProvider, ComPort comPort, ConnectionTask connectionTask, ComTaskExecution comTaskExecution) {
        super(eventPublisher, serviceProvider);
        this.comPort = comPort;
        this.connectionTask = connectionTask;
        this.comTaskExecution = comTaskExecution;
    }

    @Override
    protected ComServerEvent toEvent(AbstractComServerEventImpl.ServiceProvider serviceProvider, LogLevel level, String logMessage) {
        return new ComCommandLoggingEvent(serviceProvider, this.comPort, this.connectionTask, this.comTaskExecution, level, logMessage);
    }

}