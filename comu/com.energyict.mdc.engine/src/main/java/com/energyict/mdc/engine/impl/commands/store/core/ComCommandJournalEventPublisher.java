package com.energyict.mdc.engine.impl.commands.store.core;

import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.impl.events.logging.ComCommandLoggingEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;

import java.time.Clock;

/**
 * Publishes the completion of {@link ComCommand}s as a {@link ComServerEvent}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-20 (09:05)
 */
class ComCommandJournalEventPublisher {

    void executionCompleted(ComCommand comCommand, ExecutionContext executionContext) {
        EventPublisherImpl.getInstance().publish(this.toEvent(executionContext, comCommand));
    }

    private ComServerEvent toEvent(ExecutionContext executionContext, ComCommand comCommand) {
        if (executionContext != null) {
            String logMessage = this.buildLogMessage(comCommand, this.getServerLogLevel(executionContext));
            return new ComCommandLoggingEvent(
                    new ServiceProvider(executionContext.clock()),
                    executionContext.getComPort(),
                    executionContext.getConnectionTask(),
                    executionContext.getComTaskExecution(),
                    LogLevel.DEBUG,
                    logMessage);
        }
        else {
            String logMessage = this.buildLogMessage(comCommand, LogLevel.DEBUG);
            return new ComCommandLoggingEvent(
                    new ServiceProvider(Clock.systemUTC()),
                    null,
                    null,
                    null,
                    LogLevel.DEBUG,
                    logMessage);
        }
    }

    private String buildLogMessage(ComCommand comCommand, LogLevel logLevel) {
        String journalMessageDescription = comCommand.toJournalMessageDescription(logLevel);
        String errorDescription = comCommand.issuesToJournalMessageDescription();
        String logMessage;
        if (!errorDescription.isEmpty()) {
            logMessage = (journalMessageDescription + "; ") + errorDescription;
        }
        else {
            logMessage = journalMessageDescription;
        }
        return logMessage;
    }

    private LogLevel getServerLogLevel (ExecutionContext executionContext) {
        return this.getServerLogLevel(executionContext.getComPort());
    }

    private LogLevel getServerLogLevel (ComPort comPort) {
        return this.getServerLogLevel(comPort.getComServer());
    }

    private LogLevel getServerLogLevel (ComServer comServer) {
        return LogLevelMapper.forComServerLogLevel().toLogLevel(comServer.getCommunicationLogLevel());
    }

    private class ServiceProvider implements AbstractComServerEventImpl.ServiceProvider {
        private final Clock clock;

        private ServiceProvider(Clock clock) {
            super();
            this.clock = clock;
        }

        @Override
        public Clock clock() {
            return this.clock;
        }

    }

}