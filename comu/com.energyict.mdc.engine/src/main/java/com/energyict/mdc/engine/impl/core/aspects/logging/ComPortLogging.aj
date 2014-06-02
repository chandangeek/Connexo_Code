package com.energyict.mdc.engine.impl.core.aspects.logging;

import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.core.ScheduledJobImpl;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.engine.impl.core.ComPortServerProcess;
import com.energyict.mdc.engine.impl.core.ScheduledComPortImpl;
import com.energyict.mdc.engine.impl.core.ComJob;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.protocol.api.ConnectionException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Defines pointcuts and advice that will do logging for the
 * {@link ComPortServerProcess} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-14 (13:37)
 */
public aspect ComPortLogging extends AbstractComPortLogging {

    private pointcut scheduleJobs (ScheduledComPortImpl scheduledComPort, List<ComJob> jobs):
           execution(private void scheduleAll (List<ComJob>))
        && target(scheduledComPort)
        && args(jobs);

    before (ScheduledComPortImpl scheduledComPort, List<ComJob> jobs) : scheduleJobs(scheduledComPort, jobs) {
        ComPortOperationsLogger operationsLogger = this.getOperationsLogger(scheduledComPort);
        if (jobs.isEmpty()) {
            operationsLogger.noWorkFound(scheduledComPort.getThreadName());
        }
        else {
            operationsLogger.workFound(scheduledComPort.getThreadName(), jobs.size());
        }
    }

    @Override
    protected ComPortConnectionLogger initializeUniqueLogger (ComPort comPort, ExecutionContext executionContext, LogLevel logLevel) {
        ComPortConnectionLogger logger = this.getUniqueLogger(logLevel);
        this.attachHandlerTo(logger, executionContext);
        return logger;
    }

    @Override
    protected ComPortOperationsLogger getOperationsLogger (ComPortServerProcess comPortProcess) {
        if (this.getNormalOperationsLogger(comPortProcess) == null) {
            ComPort comPort = comPortProcess.getComPort();
            this.setNormalOperationsLogger(comPortProcess, this.newOperationsLogger(this.getServerLogLevel(comPort)));
        }
        return this.getNormalOperationsLogger(comPortProcess);
    }

    private ComPortOperationsLogger newOperationsLogger(LogLevel logLevel) {
        return LoggerFactory.getLoggerFor(ComPortOperationsLogger.class, logLevel);
    }

    private void attachHandlerTo (ComPortConnectionLogger logger, ExecutionContext executionContext) {
        Logger actualLogger = ((LoggerFactory.LoggerHolder) logger).getLogger();
        actualLogger.addHandler(new ExecutionContextLogHandler(ServiceProvider.instance.get().clock(), executionContext));
    }

    private ComPortConnectionLogger getUniqueLogger(LogLevel logLevel) {
        return LoggerFactory.getUniqueLoggerFor(ComPortConnectionLogger.class, logLevel);
    }

    @Override
    protected void connectionEstablished (ComPortConnectionLogger logger, ComPort comPort, ScheduledJobImpl scheduledJob) {
        logger.connectionEstablished(scheduledJob.getThreadName(), comPort.getName());
    }

    @Override
    protected void failedToEstablishConnection (ComPortConnectionLogger logger, ConnectionException e, String threadName) {
        logger.cannotEstablishConnection(e, threadName);
    }

    @Override
    protected void startingTask (CompositeComPortConnectionLogger logger, JobExecution job, ComTaskExecution comTaskExecution) {
        logger.startingTask(job.getThreadName(), comTaskExecution.getComTask().getName());
    }

    @Override
    protected void completingTask (CompositeComPortConnectionLogger logger, JobExecution job, ComTaskExecution comTaskExecution) {
        logger.completingTask(job.getThreadName(), comTaskExecution.getComTask().getName());
    }

    @Override
    protected void rescheduleAfterFailure (CompositeComPortConnectionLogger logger, JobExecution job, ComTaskExecution comTaskExecution) {
        logger.reschedulingTask(job.getThreadName(), comTaskExecution.getComTask().getName());
    }

    @Override
    protected void executionFailed (CompositeComPortConnectionLogger logger, RuntimeException e, JobExecution job, ComTaskExecution comTaskExecution) {
        logger.taskExecutionFailed(e, job.getThreadName(), comTaskExecution.getComTask().getName());
    }

    @Override
    protected void executionFailedDueToProblems (CompositeComPortConnectionLogger logger, JobExecution job, ComTaskExecution comTaskExecution) {
        logger.taskExecutionFailedDueToProblems(job.getThreadName(), comTaskExecution.getComTask().getName());
    }

}