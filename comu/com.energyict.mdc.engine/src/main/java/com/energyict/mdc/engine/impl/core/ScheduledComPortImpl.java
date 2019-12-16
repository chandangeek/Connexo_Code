/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.orm.PersistenceException;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.HighPriorityComJob;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.PriorityComTaskExecutionLink;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.events.ComPortOperationsLogHandler;
import com.energyict.mdc.engine.impl.core.logging.ComPortOperationsLogger;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.engine.impl.monitor.ServerScheduledComPortOperationalStatistics;
import com.energyict.mdc.engine.monitor.ScheduledComPortMonitor;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides an implementation for the {@link ScheduledComPort} interface.
 * Depending on the {@link OutboundComPort} settings
 * the process will actually be single or multi-threaded.
 * A ComPort with multiple simultaneous connections will
 * start in multi-threaded mode (see {@link OutboundComPort#getNumberOfSimultaneousConnections()}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-03 (10:07)
 */
public abstract class ScheduledComPortImpl implements ScheduledComPort, Runnable {

    public static final Logger LOGGER = Logger.getLogger(ScheduledComPortImpl.class.getName());
    private static final int SEND_TO_SLEEP_THRESHOLD = 100;
    private final ServiceProvider serviceProvider;
    private final RunningComServer runningComServer;
    private volatile ServerProcessStatus status = ServerProcessStatus.SHUTDOWN;
    private OutboundComPort comPort;
    private ComServerDAO comServerDAO;
    private ThreadFactory threadFactory;
    private String threadName;
    private Thread self;
    private AtomicBoolean continueRunning;
    private DeviceCommandExecutor deviceCommandExecutor;
    private TimeDuration schedulingInterpollDelay;
    private ScheduledComPortMonitor operationalMonitor;
    private LoggerHolder loggerHolder;
    private ExceptionLogger exceptionLogger = new ExceptionLogger();
    private Instant lastActivityTimestamp;
    ScheduledComPortImpl(RunningComServer runningComServer, OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider) {
        this(runningComServer, comPort, comServerDAO, deviceCommandExecutor, Executors.defaultThreadFactory(), serviceProvider);
    }

    ScheduledComPortImpl(RunningComServer runningComServer, OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ThreadFactory threadFactory, ServiceProvider serviceProvider) {
        super();
        this.serviceProvider = serviceProvider;
        assert comPort != null : "Scheduling a ComPort requires at least the ComPort to be scheduled instead of null!";
        this.runningComServer = runningComServer;
        this.comServerDAO = comServerDAO;
        this.threadFactory = threadFactory;
        this.deviceCommandExecutor = deviceCommandExecutor;
        this.schedulingInterpollDelay = comPort.getComServer().getSchedulingInterPollDelay();
        this.loggerHolder = new LoggerHolder(comPort);
        this.lastActivityTimestamp = this.serviceProvider.clock().instant();
        setComPort(comPort);
    }

    protected abstract void setThreadPrinciple();

    public OutboundComPort getComPort() {
        return comPort;
    }

    protected void setComPort(OutboundComPort comPort) {
        this.comPort = comPort;
        this.schedulingInterpollDelay = comPort.getComServer().getSchedulingInterPollDelay();
    }

    protected ComServerDAO getComServerDAO() {
        return comServerDAO;
    }

    protected ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    public String getThreadName() {
        if (this.threadName == null) {
            this.threadName = this.initializeThreadName();
        }
        return threadName;
    }

    private String initializeThreadName() {
        long threadId = 0;
        if (this.self != null) {
            threadId = this.self.getId();
        }
        return "ComPort schedule for " + this.getComPort().getName() + "/" + threadId;
    }

    protected DeviceCommandExecutor getDeviceCommandExecutor() {
        return deviceCommandExecutor;
    }

    @Override
    public ServerProcessStatus getStatus() {
        return this.status;
    }

    @Override
    public void changesInterpollDelayChanged(TimeDuration changesInterpollDelay) {
        // No implementation required for now
    }

    @Override
    public void schedulingInterpollDelayChanged(TimeDuration schedulingInterpollDelay) {
        this.schedulingInterpollDelay = schedulingInterpollDelay;
    }

    @Override
    public Instant getLastActivityTimestamp() {
        return this.lastActivityTimestamp;
    }

    @Override
    public final void start() {
        this.doStart();
        this.getLogger().started(this.getThreadName());
    }

    protected void doStart() {
        this.registerAsMBean();
        this.status = ServerProcessStatus.STARTING;
        this.continueRunning = new AtomicBoolean(true);
        self = this.threadFactory.newThread(this);
        self.setName(this.getThreadName());
        cleanupBusyTasks(); // do the cleanup asynchronously, to not clash with the cleanup started by the TimeOutMonitor, for example
        self.start();
        this.status = ServerProcessStatus.STARTED;
    }

    protected ComPortOperationsLogger getLogger() {
        return this.loggerHolder.get();
    }

    private void registerAsMBean() {
        this.operationalMonitor = (ScheduledComPortMonitor) this.serviceProvider.managementBeanFactory().findOrCreateFor(this);
    }

    private void unregisterAsMBean() {
        this.serviceProvider.managementBeanFactory().removeIfExistsFor(this);
    }

    private ScheduledComPortMonitor getOperationalMonitor() {
        return this.operationalMonitor;
    }

    @Override
    public void shutdown() {
        this.doShutdown();
        this.getLogger().shuttingDown(this.getThreadName());
    }

    private void doShutdown() {
        this.unregisterAsMBean();
        if (this.isStarted()) {
            this.status = ServerProcessStatus.SHUTTINGDOWN;
            this.continueRunning.set(false);
            self.interrupt();
        }
    }


    protected void interrupt() {
        LOGGER.info("[" + Thread.currentThread().getName() + "] - sending interrupt request to [" + self.getName() + "]");
        self.interrupt();
    }

    private boolean isStarted() {
        return ServerProcessStatus.STARTED.equals(this.status);
    }

    @Override
    public void shutdownImmediate() {
        this.doShutdown();
    }

    @Override
    public void run() {
        setThreadPrinciple();

        while (continueRunning()) {
            try {
                doRun();
            } catch (Throwable t) {
                exceptionLogger.unexpectedError(t);
                // Give the infrastructure some time to recover from e.g. unexpected SQL errors
                reschedule();
            }
        }
        status = ServerProcessStatus.SHUTDOWN;
    }

    private void cleanupBusyTasks() {
        try {
            comServerDAO.releaseTasksFor(comPort); // cleanup any previous tasks you kept busy ...
        } catch (PersistenceException e) {
            exceptionLogger.unexpectedError(e);
            runningComServer.refresh(getComPort());
            continueRunning.set(false);
        }
    }

    protected boolean continueRunning() {
        return continueRunning.get() && !Thread.currentThread().isInterrupted();
    }

    protected abstract void doRun();

    protected void goSleepIfWokeUpTooEarly() {
        long millisBeforeRoundSecond = getMillisBeforeRoundSecond();
        if (millisBeforeRoundSecond < SEND_TO_SLEEP_THRESHOLD) {
            try {
                Thread.sleep(millisBeforeRoundSecond);
            } catch (InterruptedException e) {
                LOGGER.info("[" + Thread.currentThread().getName() + "] - interrupted");
                Thread.currentThread().interrupt();
            }
        }
    }

    private long getMillisBeforeRoundSecond() {
        return TimeUnit.SECONDS.toMillis(1) - Instant.now().get(ChronoField.MILLI_OF_SECOND);
    }

    protected void reschedule() {
        try {
            Thread.sleep(Math.abs(getSleepDurationInMs()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private long getSleepDurationInMs() {
        Instant now = Instant.now();
        Instant nextExecutionMoment = now.plus(schedulingInterpollDelay.getMilliSeconds(), ChronoUnit.MILLIS).truncatedTo(ChronoUnit.MINUTES);
        return now.until(nextExecutionMoment, ChronoUnit.MILLIS);
    }

    final void executeTasks() {
        int storeTaskQueueLoadPercentage = deviceCommandExecutor.getCurrentLoadPercentage();
        if(storeTaskQueueLoadPercentage < 100) {
            getLogger().lookingForWork(getThreadName());
            LOGGER.warning("[" + Thread.currentThread().getName() + "] looking for work");
            long start = System.currentTimeMillis();
            List<ComJob> jobs = getComServerDAO().findExecutableOutboundComTasks(getComPort());
            queriedForTasks();
            scheduleAll(jobs, start);
        }else{
            getLogger().storeTaskQueueIsFull(storeTaskQueueLoadPercentage);
        }
    }

    private void queriedForTasks() {
        lastActivityTimestamp = serviceProvider.clock().instant();
        ((ServerScheduledComPortOperationalStatistics) getOperationalMonitor().getOperationalStatistics()).setLastCheckForWorkTimestamp(Date.from(lastActivityTimestamp));
    }

    private void scheduleAll(List<ComJob> jobs, long start) {
        if (jobs.isEmpty()) {
            this.getLogger().noWorkFound(this.getThreadName());
            LOGGER.warning("perf - [" + Thread.currentThread().getName() + "] found no work to execute, " + (System.currentTimeMillis() - start));
            reschedule();
        } else {
            this.getLogger().workFound(this.getThreadName(), jobs.size());
            LOGGER.warning("perf - [" + Thread.currentThread().getName() + "] found " + jobs.size() + " job(s) to execute, " + (System.currentTimeMillis() - start));
            this.getJobScheduler().scheduleAll(jobs);
        }
    }

    protected abstract JobScheduler getJobScheduler();

    protected ScheduledComTaskExecutionGroup newComTaskGroup(ScheduledConnectionTask connectionTask) {
        return new ScheduledComTaskExecutionGroup(getComPort(), getComServerDAO(), deviceCommandExecutor, connectionTask, serviceProvider);
    }

    ScheduledComTaskExecutionGroup newComTaskGroup(ComJob groupComJob) {
        long connectionTask = groupComJob.getConnectionTaskId();
        ScheduledComTaskExecutionGroup group = newComTaskGroup((ScheduledConnectionTask) groupComJob.getConnectionTask());
        groupComJob.getComTaskExecutions().forEach(group::add);
        return group;
    }

    protected HighPriorityComTaskExecutionGroup newComTaskGroup(HighPriorityComJob groupComJob) {
        ScheduledConnectionTask connectionTask = groupComJob.getConnectionTask();
        HighPriorityComTaskExecutionGroup group = newHighPriorityComTaskGroup(connectionTask);
        for (PriorityComTaskExecutionLink priorityComTaskExecutionLink : groupComJob.getPriorityComTaskExecutionLinks()) {
            group.add(priorityComTaskExecutionLink);
        }
        return group;
    }

    private HighPriorityComTaskExecutionGroup newHighPriorityComTaskGroup(ScheduledConnectionTask connectionTask) {
        return new HighPriorityComTaskExecutionGroup(getComPort(), getComServerDAO(), deviceCommandExecutor, connectionTask, serviceProvider);
    }

    ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public interface ServiceProvider extends JobExecution.ServiceProvider {

        UserService userService();

        ThreadPrincipalService threadPrincipalService();

        ManagementBeanFactory managementBeanFactory();

    }

    interface JobScheduler {
        int scheduleAll(List<ComJob> jobs);

        int getConnectionCount();
    }

    private class ExceptionLogger {
        private Optional<Throwable> previous = Optional.empty();

        void unexpectedError(Throwable current) {
            if (this.sameAsPrevious(current)) {
                getLogger().unexpectedError(getThreadName(), current.toString());
            } else {
                getLogger().unexpectedError(getThreadName(), current);
            }
            this.previous = Optional.of(current);
        }

        private boolean sameAsPrevious(Throwable current) {
            return this.sameMessageAsPrevious(current) && this.sameStackTraceAsPrevious(current);
        }

        private boolean sameMessageAsPrevious(Throwable current) {
            return this.previous
                    .map(Object::toString)
                    .map(ts -> ts.equals(current.toString()))
                    .orElse(false);
        }

        private boolean sameStackTraceAsPrevious(Throwable current) {
            return this.previous
                    .map(Throwable::getStackTrace)
                    .map(stackTraceElements -> Arrays.deepEquals(stackTraceElements, current.getStackTrace()))
                    .orElse(false);
        }

    }

    private class LoggerHolder {
        private ComPortOperationsLogger logger;

        private LoggerHolder(OutboundComPort comPort) {
            super();
            this.reset(comPort);
        }

        private ComPortOperationsLogger get() {
            return logger;
        }

        private ComPortOperationsLogger newLogger(OutboundComPort comPort) {
            return new CompositeComPortOperationsLogger(
                    LoggerFactory.getLoggerFor(ComPortOperationsLogger.class, this.getServerLogLevel(comPort)),
                    LoggerFactory.getLoggerFor(
                            ComPortOperationsLogger.class,
                            this.getAnonymousLogger(new ComPortOperationsLogHandler(comPort, serviceProvider.eventPublisher(), new ComServerEventServiceProvider())))
            );
        }

        private LogLevel getServerLogLevel(ComPort comPort) {
            return this.getServerLogLevel(comPort.getComServer());
        }

        private LogLevel getServerLogLevel(ComServer comServer) {
            return LogLevelMapper.forComServerLogLevel().toLogLevel(comServer.getServerLogLevel());
        }

        private Logger getAnonymousLogger(Handler handler) {
            Logger logger = Logger.getAnonymousLogger();
            logger.setLevel(Level.FINEST);
            logger.addHandler(handler);
            return logger;
        }

        public void reset(OutboundComPort comPort) {
            this.logger = this.newLogger(comPort);
        }

    }

    /**
     * Provides an implementation for the {@link ComPortOperationsLogger} interface
     * that delegates to a collection of actual ComPortOperationsLoggers.
     */
    private class CompositeComPortOperationsLogger implements ComPortOperationsLogger {
        private List<ComPortOperationsLogger> loggers;

        private CompositeComPortOperationsLogger(ComPortOperationsLogger... loggers) {
            super();
            this.loggers = Arrays.asList(loggers);
        }

        @Override
        public void started(String threadName) {
            this.loggers.forEach(each -> each.started(threadName));
        }

        @Override
        public void shuttingDown(String threadName) {
            this.loggers.forEach(each -> each.shuttingDown(threadName));
        }

        @Override
        public void lookingForWork(String comPortThreadName) {
            this.loggers.forEach(each -> each.lookingForWork(comPortThreadName));
        }

        @Override
        public void noWorkFound(String comPortThreadName) {
            this.loggers.forEach(each -> each.noWorkFound(comPortThreadName));
        }

        @Override
        public void workFound(String comPortThreadName, int numberOfJobs) {
            this.loggers.forEach(each -> each.workFound(comPortThreadName, numberOfJobs));
        }

        @Override
        public void alreadyScheduled(String comPortThreadName, ComTaskExecution comTaskExecution) {
            this.loggers.forEach(each -> each.alreadyScheduled(comPortThreadName, comTaskExecution));
        }

        @Override
        public void cannotSchedule(String comPortThreadName, ComTaskExecution comTaskExecution) {
            this.loggers.forEach(each -> each.cannotSchedule(comPortThreadName, comTaskExecution));
        }

        @Override
        public void unexpectedError(String comPortThreadName, Throwable unexpected) {
            this.loggers.forEach(each -> each.unexpectedError(comPortThreadName, unexpected));
        }

        @Override
        public void unexpectedError(String comPortThreadName, String message) {
            this.loggers.forEach(each -> each.unexpectedError(comPortThreadName, message));
        }

        @Override
        public void storeTaskQueueIsFull(int queueLoadPercentage) {
            this.loggers.forEach(each -> each.storeTaskQueueIsFull(queueLoadPercentage));
        }

    }

    protected class ComServerEventServiceProvider implements AbstractComServerEventImpl.ServiceProvider {
        @Override
        public Clock clock() {
            return serviceProvider.clock();
        }

        @Override
        public DeviceMessageService deviceMessageService() {
            return serviceProvider.deviceMessageService();
        }
    }
}