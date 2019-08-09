/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.comserver.HighPriorityComJob;
import com.energyict.mdc.common.comserver.OutboundCapableComServer;
import com.energyict.mdc.engine.impl.core.events.HighPriorityTaskSchedulerOperationsLogHandler;
import com.energyict.mdc.engine.impl.core.logging.HighPriorityTaskSchedulerOperationsLogger;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.tasks.NoMoreHighPriorityTasksCanBePickedUpRuntimeException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HighPriorityTaskSchedulerImpl implements HighPriorityTaskScheduler, Runnable {

    public static final Logger LOGGER = Logger.getLogger(HighPriorityTaskSchedulerImpl.class.getName());
    private static final int WAIT_FOR_HIGH_PRIO_TASKS = 10000;
    private static final int WAKE_UP_AT = 15; // wake up the high prio task scheduler at round minute + 15s
    private volatile ServerProcessStatus status = ServerProcessStatus.SHUTDOWN;
    private final RunningComServer runningComServer;
    private final RunningComServerImpl.ServiceProvider serviceProvider;
    private final EventPublisher eventPublisher;
    private final OutboundCapableComServer comServer;
    private final ThreadFactory threadFactory;
    private String threadName;
    private Thread self;
    private AtomicBoolean continueRunning;
    private TimeDuration schedulingInterpollDelay;
    private Instant lastSchedulePollDate;
    private Throwable previousUnexpectedThrowable;
    private HighPriorityTaskSchedulerOperationsLogger operationsLogger;

    public HighPriorityTaskSchedulerImpl(RunningComServer runningComServer, OutboundCapableComServer comServer, RunningComServerImpl.ServiceProvider serviceProvider, EventPublisher eventPublisher) {
        this(runningComServer, comServer, serviceProvider, eventPublisher, Executors.defaultThreadFactory());
    }

    public HighPriorityTaskSchedulerImpl(RunningComServer runningComServer, OutboundCapableComServer comServer, RunningComServerImpl.ServiceProvider serviceProvider, EventPublisher eventPublisher, ThreadFactory threadFactory) {
        super();
        this.runningComServer = runningComServer;
        this.comServer = comServer;
        this.schedulingInterpollDelay = comServer.getSchedulingInterPollDelay();
        this.serviceProvider = serviceProvider;
        this.eventPublisher = eventPublisher;
        this.threadFactory = threadFactory;
    }

    @Override
    public Instant getLastSchedulePollDate() {
        return lastSchedulePollDate;
    }

    private void updateLastSchedulePollDate() {
        updateLastSchedulePollDate(Instant.now());
    }

    private void updateLastSchedulePollDate(Instant timeStamp) {
        lastSchedulePollDate = timeStamp;
    }

    private RunningComServer getRunningComServer() {
        return runningComServer;
    }

    private OutboundCapableComServer getComServer() {
        return comServer;
    }

    private String getComServerName() {
        return getRunningComServer().getComServer().getName();
    }

    private ComServerDAO getComServerDAO() {
        return getRunningComServer().getComServerDAO();
    }

    protected ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    public String getThreadName() {
        if (threadName == null) {
            threadName = initializeThreadName();
        }
        return threadName;
    }

    protected String initializeThreadName() {
        return "Priority schedules for " + getComServerName();
    }

    @Override
    public ServerProcessStatus getStatus() {
        return status;
    }

    @Override
    public void schedulingInterpollDelayChanged(TimeDuration schedulingInterpollDelay) {
        this.schedulingInterpollDelay = schedulingInterpollDelay;
    }

    @Override
    public final void start() {
        doStart();
        getOperationsLogger().started(getThreadName());
    }

    protected void doStart() {
        status = ServerProcessStatus.STARTING;
        continueRunning = new AtomicBoolean(true);
        self = threadFactory.newThread(this);
        self.setName(getThreadName());
        self.start();
        started();
    }

    private boolean isStarted() {
        return ServerProcessStatus.STARTED.equals(status);
    }

    private void started() {
        status = ServerProcessStatus.STARTED;
    }

    @Override
    public void shutdown() {
        getOperationsLogger().shuttingDown(getThreadName());
        doShutdown();
    }

    private void doShutdown() {
        if (isStarted()) {
            status = ServerProcessStatus.SHUTTINGDOWN;
            continueRunning.set(false);
            self.interrupt();
        }
    }

    @Override
    public void shutdownImmediate() {
        doShutdown();
    }

    @Override
    public void run() {
        while (continueRunning.get() && !Thread.currentThread().isInterrupted()) {
            try {
                doRun();
                previousUnexpectedThrowable = null;
            } catch (Throwable throwable) {
                logUnexpectedError(throwable);

                //Wait for next poll if the current poll failed for some reason (e.g. database connection lost)
                waitInterPollDelay();
            }
        }
        status = ServerProcessStatus.SHUTDOWN;
    }

    private void logUnexpectedError(Throwable throwable) {
        try {
            if (sameMessage(throwable) && sameStackTrace(throwable)) {
                getOperationsLogger().unexpectedError(getThreadName(), throwable.toString());
            } else {
                //Log the full exception, including the stacktrace
                getOperationsLogger().unexpectedError(throwable, getThreadName());
            }
        } finally {
            previousUnexpectedThrowable = throwable;
        }
    }

    private boolean sameMessage(Throwable throwable) {
        return previousUnexpectedThrowable != null && throwable.toString()
                .equals(previousUnexpectedThrowable.toString());
    }

    private boolean sameStackTrace(Throwable throwable) {
        return previousUnexpectedThrowable != null && Arrays.deepEquals(throwable.getStackTrace(), previousUnexpectedThrowable
                .getStackTrace());
    }

    protected void doRun() {
        if (getRunningComServer().canExecuteTasksWithHighPriority()) {
            try {
                executeTasks();
            } catch (UnableToAcceptHighPriorityTasksException e) {
                /* Apparently, the last active comport of the com server was deactivated while we were querying for tasks.
                 * It is safe to ignore this, next loop will test for the high priority task capability again. */
                LOGGER.warning("UnableToAcceptHighPriorityTasksException");
            }
        } else {
            getOperationsLogger().notLookingForWork(getThreadName());
            updateLastSchedulePollDate();
            waitInterPollDelay();
        }
    }

    /**
     * Looks for {@link ComJob}s that are ready to execute
     * and dispatches them to an appropriate port,
     * interrupting the port if it is busy running another task.
     */
    private void executeTasks() {
        getOperationsLogger().lookingForWork(getThreadName());
        Map<Long, Integer> highPriorityLoadPerComPortPool = getRunningComServer().getHighPriorityLoadPerComPortPool();
        try {
            List<HighPriorityComJob> jobs = getComServerDAO().findExecutableHighPriorityOutboundComTasks(
                    getComServer(),
                    highPriorityLoadPerComPortPool);
            dispatchAll(jobs);
        } catch (NoMoreHighPriorityTasksCanBePickedUpRuntimeException e) {
            // Apparently, the comserver is already busy with the execution of maximum number of high priority tasks.
            // In such case, no new high priority tasks can be picked up - instead we will wait for 10 seconds,
            // after which we will continue (and thus recheck for executable high priority tasks)
            LOGGER.info("[high-prio]: " + e.getMessage());
            delay(WAIT_FOR_HIGH_PRIO_TASKS);
        }
        updateLastSchedulePollDate();
    }

    private void dispatchAll(List<HighPriorityComJob> jobs) {
        if (jobs.isEmpty()) {
            getOperationsLogger().noWorkFound(getThreadName());
            LOGGER.info("found no work to execute");
        } else {
            getOperationsLogger().workFound(getThreadName(), jobs.size());
            LOGGER.info("found " + jobs.size() + " job(s) to execute");
        }

        if (!jobs.isEmpty()) {
            for (HighPriorityComJob job : jobs) {
                getRunningComServer().executeWithHighPriority(job);
            }
            giveTheConsumersSomeSpace();
        } else {
            waitInterPollDelay();
        }
    }

    private void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
        }
    }

    protected void waitInterPollDelay() {
        try {
            // forcing the high prio scheduler thread to wake up at round minute + 30s
            long sleepDurationInMs = getSleepDurationInMs();
            Thread.sleep(sleepDurationInMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private long getSleepDurationInMs() {
        Instant now = Instant.now();
        Instant nextExecutionMoment = now.plus(schedulingInterpollDelay.getMilliSeconds(), ChronoUnit.MILLIS).truncatedTo(ChronoUnit.MINUTES).plus(WAKE_UP_AT, ChronoUnit.SECONDS);
        if (now.isBefore(nextExecutionMoment)) {
            return now.until(nextExecutionMoment, ChronoUnit.MILLIS);
        }
        return now.until(nextExecutionMoment.plus(schedulingInterpollDelay.getMilliSeconds(), ChronoUnit.MILLIS), ChronoUnit.MILLIS);
    }

    /**
     * After we populate the queue, it is recommended to wait a couple of seconds for the workers to fetch and lock the tasks.
     * This way the first tasks aren't fetched again and only non busy tasks are put on the queue.
     */
    private void giveTheConsumersSomeSpace() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private HighPriorityTaskSchedulerOperationsLogger getOperationsLogger() {
        if (operationsLogger == null) {
            operationsLogger = new CompositeHighPriorityTaskSchedulerOperationsLogger();
        }
        return operationsLogger;
    }

    private LogLevel getServerLogLevel() {
        return LogLevelMapper.forComServerLogLevel().toLogLevel(getRunningComServer().getComServer().getServerLogLevel());
    }

    private class CompositeHighPriorityTaskSchedulerOperationsLogger implements HighPriorityTaskSchedulerOperationsLogger {
        private HighPriorityTaskSchedulerOperationsLogger normalOperationsLogger;
        private HighPriorityTaskSchedulerOperationsLogger eventOperationsLogger;

        CompositeHighPriorityTaskSchedulerOperationsLogger() {
            super();
            normalOperationsLogger = LoggerFactory.getLoggerFor(HighPriorityTaskSchedulerOperationsLogger.class, getServerLogLevel());
            eventOperationsLogger = LoggerFactory.getLoggerFor(HighPriorityTaskSchedulerOperationsLogger.class,
                    getAnonymousLogger(new HighPriorityTaskSchedulerOperationsLogHandler(eventPublisher, serviceProvider)));
        }

        private Logger getAnonymousLogger(Handler handler) {
            Logger logger = Logger.getAnonymousLogger();
            logger.setLevel(Level.FINEST);
            logger.addHandler(handler);
            return logger;
        }

        @Override
        public void started(String threadName) {
            normalOperationsLogger.started(threadName);
            eventOperationsLogger.started(threadName);
        }

        @Override
        public void shuttingDown(String threadName) {
            normalOperationsLogger.shuttingDown(threadName);
            eventOperationsLogger.shuttingDown(threadName);
        }

        @Override
        public void lookingForWork(String threadName) {
            normalOperationsLogger.lookingForWork(threadName);
            eventOperationsLogger.lookingForWork(threadName);
        }

        @Override
        public void notLookingForWork(String threadName) {
            normalOperationsLogger.notLookingForWork(threadName);
            eventOperationsLogger.notLookingForWork(threadName);
        }

        @Override
        public void noWorkFound(String threadNqme) {
            normalOperationsLogger.noWorkFound(threadName);
            eventOperationsLogger.noWorkFound(threadName);
        }

        @Override
        public void workFound(String threadName, int numberOfJobs) {
            normalOperationsLogger.workFound(threadName, numberOfJobs);
            eventOperationsLogger.workFound(threadName, numberOfJobs);
        }

        @Override
        public void unexpectedError(Throwable unexpected, String threadName) {
            normalOperationsLogger.unexpectedError(unexpected, threadName);
            eventOperationsLogger.unexpectedError(unexpected, threadName);
        }

        @Override
        public void unexpectedError(String threadName, String message) {
            normalOperationsLogger.unexpectedError(threadName, message);
            eventOperationsLogger.unexpectedError(threadName, message);
        }

        // TODO: uncomment high-prio
//        @Override
//        public void interruptingWork(String threadName, PriorityComTaskExecutionLink priorityComTaskExecutionLink) {
//            normalOperationsLogger.interruptingWork(threadName, priorityComTaskExecutionLink);
//            eventOperationsLogger.interruptingWork(threadName, priorityComTaskExecutionLink);
//        }

    }
}
