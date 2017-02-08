/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events;

import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.events.logging.UnrelatedLoggingEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;

/**
 * Provides an implementation for the log Handler interface
 * that creates simple log messages that do not relate
 * to any ComServer objects as that information is not available.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-08 (16:04)
 */
public class DeviceCommandExecutorLogHandler extends EventPublishingLogHandler {

    public DeviceCommandExecutorLogHandler(EventPublisher eventPublisher, AbstractComServerEventImpl.ServiceProvider serviceProvider) {
        super(eventPublisher, serviceProvider);
    }

    @Override
    protected ComServerEvent toEvent(AbstractComServerEventImpl.ServiceProvider serviceProvider, LogLevel level, String logMessage) {
        return new UnrelatedLoggingEvent(serviceProvider, level, logMessage);
    }

}