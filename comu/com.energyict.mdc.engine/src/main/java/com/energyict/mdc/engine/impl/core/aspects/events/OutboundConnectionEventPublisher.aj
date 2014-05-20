package com.energyict.mdc.engine.impl.core.aspects.events;

import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.ScheduledJobImpl;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.impl.events.connection.CannotEstablishConnectionEvent;
import com.energyict.mdc.engine.impl.events.connection.CloseConnectionEvent;
import com.energyict.mdc.engine.impl.events.connection.EstablishConnectionEvent;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.protocol.api.ConnectionException;

/**
 * Defines pointcuts and advice that will publish events
 * that relate to outbound connections.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-06 (10:36)
 */
public aspect OutboundConnectionEventPublisher {

    private pointcut establishConnectionFor (ComPort comPort, ScheduledJobImpl scheduledJob):
            execution(boolean ScheduledJobImpl.establishConnectionFor(ComPort))
                    && target(scheduledJob)
                    && args(comPort);

    after (ComPort comPort, ScheduledJobImpl scheduledJob) returning (boolean succes) : establishConnectionFor(comPort, scheduledJob) {
        if (succes) {
            this.publish(new EstablishConnectionEvent(comPort, scheduledJob.getConnectionTask(), EventPublisherImpl.getInstance().serviceProvider()));
         }
    }

    private pointcut connectionFailed (ExecutionContext context, ConnectionException e, ConnectionTask connectionTask):
            execution(void JobExecution.ExecutionContext.connectionFailed(ConnectionException, ConnectionTask))
         && target(context)
         && args(e, connectionTask);

    after (ExecutionContext context, ConnectionException e, ConnectionTask connectionTask) : connectionFailed(context, e, connectionTask) {
        this.publish(new CannotEstablishConnectionEvent(context.getComPort(), connectionTask, e, EventPublisherImpl.getInstance().serviceProvider()));
    }

    private pointcut closeConnection (ExecutionContext executionContext):
            execution(public void JobExecution.ExecutionContext.close())
                    && target(executionContext);

    after (ExecutionContext executionContext): closeConnection(executionContext) {
        /* closeConnection is called from finally block
         * even when the connection was never established.
         * So first test if there was a connection. */
        if (this.isConnected(executionContext)) {
            this.publish(new CloseConnectionEvent(executionContext.getComPort(), executionContext.getConnectionTask(), EventPublisherImpl.getInstance().serviceProvider()));
        }
    }

    private void publish (ComServerEvent event) {
        EventPublisherImpl.getInstance().publish(event);
    }

    private boolean isConnected (ExecutionContext executionContext) {
        return executionContext.getComChannel() != null;
    }

}