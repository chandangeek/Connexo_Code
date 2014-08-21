package com.energyict.mdc.engine.impl.core.aspects.journaling;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.DeviceProtocol;

/**
 * Defines pointcuts and advice that will create ComTaskExecutionJournalEntry
 * objects for {@link ComCommand}s that are being executed.
 * <p/>
 * Copyrights EnergyICT
 * Date: 27/08/12
 * Time: 10:39
 */
public aspect ComCommandJournaling {

    declare precedence:
            com.energyict.mdc.engine.impl.core.aspects.performance.ComCommandPerformance ,
            ComCommandJournaling,
            com.energyict.mdc.engine.impl.core.aspects.logging.ComCommandLogging,
            com.energyict.mdc.engine.impl.core.aspects.events.ComCommandLogEventPublisher;

    private pointcut startComTaskExecution(JobExecution jobExecution, ComTaskExecution comTaskExecution):
        execution(void start(ComTaskExecution))
     && target(jobExecution)
     && args(comTaskExecution);

    after (JobExecution jobExecution, ComTaskExecution comTaskExecution): startComTaskExecution(jobExecution, comTaskExecution) {
        jobExecution.getExecutionContext().initializeJournalist();
    }

    private pointcut comCommandExecution (ComCommand comCommand, DeviceProtocol deviceProtocol, ExecutionContext executionContext):
        execution(void ComCommand.execute(DeviceProtocol, ExecutionContext))
     && target(comCommand)
     && args(deviceProtocol, executionContext);


    after (DeviceProtocol deviceProtocol, ExecutionContext executionContext, ComCommand comCommand): comCommandExecution(comCommand, deviceProtocol, executionContext) {
        this.delegateToJournalistIfAny(executionContext, comCommand);
    }

    private void delegateToJournalistIfAny (ExecutionContext executionContext, ComCommand comCommand) {
        /* Business code validates that execution context can be null
         * and will throw a CodingException when that is the case. */
        if (executionContext != null) {
            ComCommandJournalist journalist = executionContext.getJournalist();
            if (journalist != null) {
                journalist.executionCompleted(comCommand, this.getServerLogLevel(executionContext));
            }
        }
    }

    private LogLevel getServerLogLevel (ExecutionContext executionContext) {
        return this.getServerLogLevel(executionContext.getComPort());
    }

    private LogLevel getServerLogLevel (ComPort comPort) {
        return this.getServerLogLevel(comPort.getComServer());
    }

    private LogLevel getServerLogLevel (ComServer comServer) {
        return LogLevelMapper.map(comServer.getCommunicationLogLevel());
    }

}