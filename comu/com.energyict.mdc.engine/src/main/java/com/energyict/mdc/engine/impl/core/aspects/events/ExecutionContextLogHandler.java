package com.energyict.mdc.engine.impl.core.aspects.events;

import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.events.EventPublishingLogHandler;
import com.energyict.mdc.engine.impl.events.logging.CommunicationLoggingEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.model.ComPort;

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

    private JobExecution.ExecutionContext executionContext;

    public ExecutionContextLogHandler (JobExecution.ExecutionContext executionContext) {
        super();
        this.executionContext = executionContext;
    }

    @Override
    protected ComServerEvent toEvent (AbstractComServerEventImpl.ServiceProvider serviceProvider, LogLevel level, String logMessage) {
        ConnectionTask connectionTask = this.executionContext.getConnectionTask();
        ComPort comPort = this.executionContext.getComPort();
        return new CommunicationLoggingEvent(connectionTask, comPort, level, logMessage, serviceProvider);
    }

}