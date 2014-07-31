package com.energyict.mdc.engine.impl.core.aspects.logging;

import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.core.RescheduleBehavior;
import com.energyict.mdc.engine.impl.core.ScheduledJobImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.engine.impl.core.ComPortServerProcess;
import com.energyict.mdc.engine.impl.core.MultiThreadedScheduledComPort;
import com.energyict.mdc.engine.impl.core.ScheduledComPortImpl;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import java.util.logging.Logger;

/**
 * Defines pointcuts and advice that will do logging for the
 * {@link com.energyict.mdc.engine.impl.core.ComPortServerProcess} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-08 (13:05)
 */
public abstract aspect AbstractComPortLogging {
    declare precedence :
            com.energyict.mdc.engine.impl.core.aspects.statistics.OutboundCommunicationStatisticsMonitor,
            ComChannelReadWriteLogger,
            com.energyict.mdc.engine.impl.core.aspects.journaling.ComCommandJournaling,
            com.energyict.mdc.engine.impl.core.aspects.events.OutboundConnectionEventPublisher,
            com.energyict.mdc.engine.impl.core.aspects.events.OutboundComTaskEventPublisher,
            InboundComPortLogging,
            ComPortLogging,
            com.energyict.mdc.engine.impl.core.aspects.events.ComPortLogEventPublisher;

    CompositeComPortConnectionLogger ExecutionContext.connectionLogger;
    ComPortOperationsLogger ComPortServerProcess.normalOperationsLogger;
    ComPortOperationsLogger ComPortServerProcess.eventOperationsLogger;

    private pointcut initializeExecutionContext (JobExecution jobExecution, ConnectionTask connectionTask, ComPort comPort, boolean logConnectionProperties):
            execution(ExecutionContext newExecutionContext(ConnectionTask, ComPort, boolean))
         && target(jobExecution)
         && args(connectionTask, comPort, logConnectionProperties);

    after (JobExecution jobExecution, ConnectionTask connectionTask, ComPort comPort, boolean logConnectionProperties) returning (ExecutionContext executionContext) : initializeExecutionContext(jobExecution, connectionTask, comPort, logConnectionProperties) {
        if (executionContext.connectionLogger == null) {
            executionContext.connectionLogger = new CompositeComPortConnectionLogger();
        }
        ComPortConnectionLogger comPortConnectionLogger = this.initializeUniqueLogger(comPort, executionContext, this.getServerLogLevel(comPort));
        executionContext.connectionLogger.add(comPortConnectionLogger);
        LoggerFactory.LoggerHolder loggerHolder = (LoggerFactory.LoggerHolder) comPortConnectionLogger;
        Logger logger = loggerHolder.getLogger();
        if (executionContext.getLogger() instanceof CompositeLogger) {
            CompositeLogger compositeLogger = (CompositeLogger) executionContext.getLogger();
            compositeLogger.add(logger);
        }
        else {
            // Overrule the default logger that is installed in the ExecutionContext
            CompositeLogger compositeLogger = new CompositeLogger();
            executionContext.setLogger(compositeLogger);
            compositeLogger.add(logger);
        }
    }

    protected abstract ComPortConnectionLogger initializeUniqueLogger (ComPort comPort, ExecutionContext executionContext, LogLevel logLevel);

    private pointcut establishConnectionFor (ScheduledJobImpl scheduledJob):
            execution(boolean ScheduledJobImpl.establishConnectionFor())
         && target(scheduledJob);

    after (ScheduledJobImpl scheduledJob) returning (boolean success) : establishConnectionFor(scheduledJob) {
        ExecutionContext executionContext = scheduledJob.getExecutionContext();
        ComPortConnectionLogger logger = executionContext.connectionLogger;
        if (success) {
            this.connectionEstablished(logger, scheduledJob.getComPort(), scheduledJob);
        }
    }

    protected void connectionEstablished (ComPortConnectionLogger logger, ComPort comPort, ScheduledJobImpl scheduledJob) {
        // At most one subclass should be calling logger.connectionEstablished(scheduledJob.getThreadName(), comPort.getName());
    }

    private pointcut started (ComPortServerProcess comPort):
            execution(void ComPortServerProcess.start())
         && target(comPort);

    after (ComPortServerProcess comPort): started(comPort) {
        this.getOperationsLogger(comPort).started(comPort.getThreadName());
    }

    private pointcut shuttingDown (ComPortServerProcess comPort):
            execution(void ComPortServerProcess.shutdown())
                && target(comPort);

    before (ComPortServerProcess comPort): shuttingDown(comPort) {
        this.getOperationsLogger(comPort).shuttingDown(comPort.getThreadName());
    }

    private pointcut monitorChanges (ComPortServerProcess comPort):
            execution(public void ComPortServerProcess.checkAndApplyChanges())
         && target(comPort);

    before (ComPortServerProcess comPort): monitorChanges(comPort) {
        this.getOperationsLogger(comPort).monitoringChanges(comPort.getComPort());
    }

    private pointcut connectionFailed (ExecutionContext context, ConnectionException e, ConnectionTask connectionTask):
            execution(void ExecutionContext.connectionFailed(ConnectionException, ConnectionTask))
         && target(context)
         && args(e, connectionTask);

    after (ExecutionContext context, ConnectionException e, ConnectionTask connectionTask) : connectionFailed(context, e, connectionTask) {
        this.failedToEstablishConnection(context.connectionLogger, e, context.getJob().getThreadName());
    }

    protected void failedToEstablishConnection (ComPortConnectionLogger logger, ConnectionException e, String threadName) {
        // At most one subclass should be calling logger.cannotEstablishConnection(e, threadName);
    }

    private pointcut startTask (JobExecution job, ComTaskExecution comTaskExecution):
            execution(void JobExecution.start(ComTaskExecution))
         && target(job)
         && args(comTaskExecution);

    before (JobExecution job, ComTaskExecution comTaskExecution): startTask(job, comTaskExecution) {
        ExecutionContext executionContext = job.getExecutionContext();
        this.startingTask(executionContext.connectionLogger, job, comTaskExecution);
    }

    protected void startingTask (CompositeComPortConnectionLogger logger, JobExecution job, ComTaskExecution comTaskExecution) {
        // At most one subclass should be calling logger.startingTask(job.getThreadName(), comTaskExecution);
    }

    private pointcut alreadyScheduled (MultiThreadedScheduledComPort comPort, ComTaskExecution comTaskExecution):
            execution(void MultiThreadedScheduledComPort.alreadyScheduled(ComTaskExecution))
         && target(comPort)
         && args(comTaskExecution);

    before (MultiThreadedScheduledComPort comPort, ComTaskExecution comTaskExecution): alreadyScheduled(comPort, comTaskExecution) {
        this.getOperationsLogger(comPort).alreadyScheduled(comPort.getThreadName(), comTaskExecution);
    }

    private pointcut cannotSchedule (MultiThreadedScheduledComPort comPort, ComTaskExecution comTaskExecution):
            execution(void MultiThreadedScheduledComPort.cannotSchedule(ComTaskExecution))
         && target(comPort)
         && args(comTaskExecution);

    before (MultiThreadedScheduledComPort comPort, ComTaskExecution comTaskExecution): cannotSchedule(comPort, comTaskExecution) {
        this.getOperationsLogger(comPort).cannotSchedule(comPort.getThreadName(), comTaskExecution);
    }

    private pointcut unscheduled (MultiThreadedScheduledComPort comPort, ComTaskExecution comTaskExecution):
            execution(void MultiThreadedScheduledComPort.unscheduled(ComTaskExecution))
         && target(comPort)
         && args(comTaskExecution);

    before (MultiThreadedScheduledComPort comPort, ComTaskExecution comTaskExecution): unscheduled(comPort, comTaskExecution) {
        this.getOperationsLogger(comPort).unscheduled(comPort.getThreadName(), comTaskExecution);
    }

    private pointcut completeTask (JobExecution job, ComTaskExecution comTaskExecution):
            execution(void JobExecution.completeExecutedComTask(ComTaskExecution))
         && target(job)
         && args(comTaskExecution);

    before (JobExecution job, ComTaskExecution comTaskExecution): completeTask(job, comTaskExecution) {
        ExecutionContext executionContext = job.getExecutionContext();
        this.completingTask(executionContext.connectionLogger, job, comTaskExecution);
    }

    protected void completingTask (CompositeComPortConnectionLogger logger, JobExecution job, ComTaskExecution comTaskExecution) {
        // At most one subclass should be calling logger.completingTask(job.getThreadName(), comTaskExecution);
    }

    private pointcut rescheduleAfterFailure(JobExecution job, RescheduleBehavior.RescheduleReason rescheduleReason):
            execution(void JobExecution.reschedule(java.lang.Throwable, RescheduleBehavior.RescheduleReason))
            && target(job)
            && args(.., rescheduleReason);

    before(JobExecution job, RescheduleBehavior.RescheduleReason rescheduleReason): rescheduleAfterFailure(job, rescheduleReason) {
        ExecutionContext executionContext = job.getExecutionContext();
        for (ComTaskExecution failedComTasks : job.getFailedComTaskExecutions()) {
            this.rescheduleAfterFailure(executionContext.connectionLogger, job, failedComTasks);
        }
    }

    protected void rescheduleAfterFailure (CompositeComPortConnectionLogger logger, JobExecution job, ComTaskExecution comTaskExecution) {
        // At most one subclass should be calling logger.reschedulingTask(job.getThreadName(), comTaskExecution);
    }

    private pointcut executeTask (JobExecution job, JobExecution.PreparedComTaskExecution comTaskExecution):
            execution(boolean JobExecution.execute(JobExecution.PreparedComTaskExecution))
         && target(job)
         && args(comTaskExecution);

    after (JobExecution job, JobExecution.PreparedComTaskExecution comTaskExecution) returning (boolean success) : executeTask(job, comTaskExecution) {
        if (!success) {
            ExecutionContext executionContext = job.getExecutionContext();
            this.executionFailedDueToProblems(executionContext.connectionLogger, job, comTaskExecution.getComTaskExecution());
        }
    }

    after (JobExecution job, JobExecution.PreparedComTaskExecution comTaskExecution) throwing (RuntimeException e) : executeTask(job, comTaskExecution) {
        ExecutionContext executionContext = job.getExecutionContext();
        this.executionFailed(executionContext.connectionLogger, e, job, comTaskExecution.getComTaskExecution());
    }

    protected void executionFailed (CompositeComPortConnectionLogger logger, RuntimeException e, JobExecution job, ComTaskExecution comTaskExecution) {
        // At most one subclass should be calling logger.taskExecutionFailed(e, job.getThreadName(), comTaskExecution);
    }

    protected void executionFailedDueToProblems (CompositeComPortConnectionLogger logger, JobExecution job, ComTaskExecution comTaskExecution) {
        // At most one subclass should be calling logger.executionFailedDueToErrors(e, job.getThreadName(), comTaskExecution);
    }

    private pointcut executeTasks (ScheduledComPortImpl comPort):
            execution(void ScheduledComPortImpl.executeTasks())
         && target(comPort);

    before (ScheduledComPortImpl comPort): executeTasks(comPort) {
        this.getOperationsLogger(comPort).lookingForWork(comPort.getThreadName());
    }

    private pointcut doRun (ScheduledComPortImpl comPort):
            execution(void ScheduledComPortImpl.doRun())
         && target(comPort);

    after (ScheduledComPortImpl comPort) throwing (Throwable t) : doRun(comPort) {
        this.getOperationsLogger(comPort).unexpectedError(t, comPort.getThreadName());
    }

    protected abstract ComPortOperationsLogger getOperationsLogger (ComPortServerProcess comPortProcess);

    protected LogLevel getServerLogLevel (ComPort comPort) {
        return this.getServerLogLevel(comPort.getComServer());
    }

    private LogLevel getServerLogLevel (ComServer comServer) {
        return LogLevelMapper.map(comServer.getServerLogLevel());
    }

    protected ComPortOperationsLogger getNormalOperationsLogger (ComPortServerProcess comPortProcess) {
        return comPortProcess.normalOperationsLogger;
    }

    protected void setNormalOperationsLogger (ComPortServerProcess comPortProcess, ComPortOperationsLogger operationsLogger) {
        comPortProcess.normalOperationsLogger = operationsLogger;
    }

    protected ComPortOperationsLogger getEventOperationsLogger (ComPortServerProcess comPortProcess) {
        return comPortProcess.eventOperationsLogger;
    }

    protected void setEventOperationsLogger (ComPortServerProcess comPortProcess, ComPortOperationsLogger operationsLogger) {
        comPortProcess.eventOperationsLogger = operationsLogger;
    }

}