package com.energyict.mdc.engine.impl.core.aspects.events;

import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.core.aspects.statistics.AbstractCommunicationStatisticsMonitor;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.impl.events.comtask.ComTaskExecutionCompletionEvent;
import com.energyict.mdc.engine.impl.events.comtask.ComTaskExecutionStartedEvent;
import com.energyict.mdc.engine.impl.events.connection.CloseConnectionEvent;
import com.energyict.mdc.engine.impl.events.connection.EstablishConnectionEvent;
import com.energyict.mdc.engine.impl.events.connection.UndiscoveredCloseConnectionEvent;
import com.energyict.mdc.engine.impl.events.connection.UndiscoveredEstablishConnectionEvent;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

/**
 * Defines pointcuts and advice that will publish events
 * that relate to inbound connections.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-07 (08:52)
 */
public privileged aspect InboundConnectionEventPublisher extends AbstractCommunicationStatisticsMonitor {

    /* Similar comment as in com.energyict.comserver.aspects.statistics.inbound.CommunicationStatisticsMonitor. */
    private pointcut handle (InboundCommunicationHandler handler, InboundDeviceProtocol inboundDeviceProtocol, InboundDiscoveryContext context):
            execution(void handle (InboundDeviceProtocol, InboundDiscoveryContext))
         && target(handler)
         && args(inboundDeviceProtocol, context);

    before (InboundCommunicationHandler handler, InboundDeviceProtocol inboundDeviceProtocol, InboundDiscoveryContext context) : handle(handler, inboundDeviceProtocol, context) {
        this.publish(new UndiscoveredEstablishConnectionEvent(handler.getComPort()));
    }

    private pointcut startDeviceSessionInContext (InboundCommunicationHandler handler):
           execution(void startDeviceSessionInContext())
        && target(handler);

    before (InboundCommunicationHandler handler): startDeviceSessionInContext(handler) {
        this.publish(new EstablishConnectionEvent(handler.getComPort(), handler.getConnectionTask()));
    }

    private pointcut processCollectedData (InboundCommunicationHandler handler, InboundDeviceProtocol inboundDeviceProtocol, DeviceCommandExecutionToken token, OfflineDevice offlineDevice):
           execution(void processCollectedData(InboundDeviceProtocol, DeviceCommandExecutionToken, OfflineDevice))
        && target(handler)
        && args(inboundDeviceProtocol, token, offlineDevice);

    before (InboundCommunicationHandler handler, InboundDeviceProtocol inboundDeviceProtocol, DeviceCommandExecutionToken token, OfflineDevice offlineDevice): processCollectedData(handler, inboundDeviceProtocol, token, offlineDevice) {
        for (ComTaskExecution comTaskExecution : handler.getDeviceComTaskExecutions()) {
            this.publish(new ComTaskExecutionStartedEvent(comTaskExecution, handler.getComPort(), handler.getConnectionTask(), EventPublisherImpl.getInstance().serviceProvider()));
        }
    }

    after (InboundCommunicationHandler handler, InboundDeviceProtocol inboundDeviceProtocol, DeviceCommandExecutionToken token, OfflineDevice offlineDevice): processCollectedData(handler, inboundDeviceProtocol, token, offlineDevice) {
        for (ComTaskExecution comTaskExecution : handler.getDeviceComTaskExecutions()) {
            this.publish(new ComTaskExecutionCompletionEvent(comTaskExecution, handler.getComPort(), handler.getConnectionTask()));
        }
    }

    private pointcut closeContext (InboundCommunicationHandler handler):
            execution(void closeContext())
         && target(handler);

    after (InboundCommunicationHandler handler): closeContext(handler) {
        if (handler.getConnectionTask() != null) {
            this.publish(new CloseConnectionEvent(handler.getComPort(), handler.getConnectionTask()));
        }
        else {
            this.publish(new UndiscoveredCloseConnectionEvent(handler.getComPort()));
        }
    }

    private void publish (ComServerEvent event) {
        EventPublisherImpl.getInstance().publish(event);
    }

}