package com.energyict.mdc.engine.impl.core.inbound.aspects.events;

import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.events.EventPublishingLogHandler;
import com.energyict.mdc.engine.impl.events.logging.CommunicationLoggingEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;

/**
 * Provides an implementation for the log Handler interface
 * that creates simple log messages that relate
 * to the {@link com.energyict.mdc.engine.model.ComPort}
 * and the {@link com.energyict.mdc.device.data.tasks.ConnectionTask}
 * of inbound communication discovery.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-08 (10:33)
 */
public class ComPortDiscoveryLogHandler extends EventPublishingLogHandler {

    private InboundCommunicationHandler inboundCommunicationHandler;

    public ComPortDiscoveryLogHandler (InboundCommunicationHandler inboundCommunicationHandler) {
        super();
        this.inboundCommunicationHandler = inboundCommunicationHandler;
    }

    @Override
    protected ComServerEvent toEvent(AbstractComServerEventImpl.ServiceProvider serviceProvider, LogLevel level, String logMessage) {
        InboundComPort comPort = this.inboundCommunicationHandler.getComPort();
        InboundConnectionTask connectionTask = this.inboundCommunicationHandler.getConnectionTask();
        return new CommunicationLoggingEvent(serviceProvider, connectionTask, comPort, level, logMessage);
    }

}