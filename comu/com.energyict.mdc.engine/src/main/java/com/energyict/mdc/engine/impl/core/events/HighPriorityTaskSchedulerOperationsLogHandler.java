/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.events;

import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.events.EventPublishingLogHandler;
import com.energyict.mdc.engine.impl.events.logging.HighPriorityTaskSchedulerOperationsLoggingEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;

/**
 * Provides an implementation for the log Handler interface
 * that creates log messages that relate to the {@link HighPriorityTaskScheduler}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-10-14 (13:53)
 */
public class HighPriorityTaskSchedulerOperationsLogHandler extends EventPublishingLogHandler {

    public HighPriorityTaskSchedulerOperationsLogHandler(EventPublisher eventPublisher, AbstractComServerEventImpl.ServiceProvider serviceProvider) {
        super(eventPublisher, serviceProvider);
    }

    @Override
    protected ComServerEvent toEvent(AbstractComServerEventImpl.ServiceProvider serviceProvider, LogLevel level, String logMessage) {
        return new HighPriorityTaskSchedulerOperationsLoggingEvent(serviceProvider, level, logMessage);
    }

}