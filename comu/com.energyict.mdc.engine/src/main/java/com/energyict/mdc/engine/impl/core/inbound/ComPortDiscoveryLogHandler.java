/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.events.EventPublishingLogHandler;
import com.energyict.mdc.engine.impl.logging.LogLevel;

/**
 * Provides an implementation for the log Handler interface
 * that creates simple log messages that relate
 * to the {@link com.energyict.mdc.engine.config.ComPort}
 * and the {@link com.energyict.mdc.device.data.tasks.ConnectionTask}
 * of inbound communication discovery.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-08 (10:33)
 */
class ComPortDiscoveryLogHandler extends EventPublishingLogHandler {

    private final InboundCommunicationHandler inboundCommunicationHandler;

    ComPortDiscoveryLogHandler(InboundCommunicationHandler inboundCommunicationHandler, EventPublisher eventPublisher, AbstractComServerEventImpl.ServiceProvider serviceProvider) {
        super(eventPublisher, serviceProvider);
        this.inboundCommunicationHandler = inboundCommunicationHandler;
    }

    @Override
    protected ComServerEvent toEvent(AbstractComServerEventImpl.ServiceProvider serviceProvider, LogLevel level, String logMessage) {
        InboundComPort comPort = this.inboundCommunicationHandler.getComPort();
        InboundConnectionTask connectionTask = this.inboundCommunicationHandler.getConnectionTask();
        return new ComPortDiscoveryLoggingEvent(serviceProvider, connectionTask, comPort, level, logMessage);
    }

}