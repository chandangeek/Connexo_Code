package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.events.ComPortLogHandler;
import com.energyict.mdc.engine.impl.core.logging.ComPortOperationsLogger;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.engine.impl.monitor.ScheduledComPortMonitor;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.users.UserService;
import org.joda.time.DateTimeConstants;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
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

    public interface ServiceProvider extends JobExecution.ServiceProvider {

        public UserService userService();

        public ThreadPrincipalService threadPrincipalService();

        public ManagementBeanFactory managementBeanFactory();

    }

    private final ServiceProvider serviceProvider;
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
    private Instant lastActivityTimestamp;

    public ScheduledComPortImpl(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider) {
        this(comPort, comServerDAO, deviceCommandExecutor, Executors.defaultThreadFactory(), serviceProvider);
    }

    public ScheduledComPortImpl(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ThreadFactory threadFactory, ServiceProvider serviceProvider) {
        super();
        this.serviceProvider = serviceProvider;
        assert comPort != null : "Scheduling a ComPort requires at least the ComPort to be scheduled instead of null!";
        this.comPort = comPort;
        this.comServerDAO = comServerDAO;
        this.threadFactory = threadFactory;
        this.deviceCommandExecutor = deviceCommandExecutor;
        this.schedulingInterpollDelay = comPort.getComServer().getSchedulingInterPollDelay();
        this.loggerHolder = new LoggerHolder(comPort);
        this.lastActivityTimestamp = this.serviceProvider.clock().instant();
    }

    protected abstract void setThreadPrinciple();

    public OutboundComPort getComPort () {
        return comPort;
    }

    protected void setComPort (OutboundComPort comPort) {
        this.comPort = comPort;
        this.schedulingInterpollDelay = comPort.getComServer().getSchedulingInterPollDelay();
    }

    protected ComServerDAO getComServerDAO () {
        return comServerDAO;
    }

    protected ThreadFactory getThreadFactory () {
        return threadFactory;
    }

    public String getThreadName () {
        if (this.threadName == null) {
            this.threadName = this.initializeThreadName();
        }
        return threadName;
    }

    protected String initializeThreadName () {
        return "ComPort schedule for " + this.getComPort().getName();
    }

    protected DeviceCommandExecutor getDeviceCommandExecutor () {
        return deviceCommandExecutor;
    }

    @Override
    public ServerProcessStatus getStatus () {
        return this.status;
    }

    @Override
    public void changesInterpollDelayChanged (TimeDuration changesInterpollDelay) {
        // No implementation required for now
    }

    @Override
    public void schedulingInterpollDelayChanged (TimeDuration schedulingInterpollDelay) {
        this.schedulingInterpollDelay = schedulingInterpollDelay;
    }

    @Override
    public Instant getLastActivityTimestamp() {
        return this.lastActivityTimestamp;
    }

    @Override
    public final void start () {
        this.doStart();
        this.getLogger().started(this.getThreadName());
    }

    protected void doStart () {
        this.registerAsMBean();
        this.status = ServerProcessStatus.STARTING;
        this.continueRunning = new AtomicBoolean(true);
        self = this.threadFactory.newThread(this);
        self.setName(this.getThreadName());
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

    protected ScheduledComPortMonitor getOperationalMonitor() {
        return this.operationalMonitor;
    }

    @Override
    public void shutdown () {
        this.doShutdown();
        this.getLogger().shuttingDown(this.getThreadName());
    }

    private void doShutdown () {
        this.unregisterAsMBean();
        if (this.isStarted()) {
            this.status = ServerProcessStatus.SHUTTINGDOWN;
            this.continueRunning.set(false);
            self.interrupt();
        }
    }

    private boolean isStarted () {
        return ServerProcessStatus.STARTED.equals(this.status);
    }

    @Override
    public void shutdownImmediate () {
        this.doShutdown();
    }

    @Override
    public void run () {
        setThreadPrinciple();
        while (this.continueRunning.get() && !Thread.currentThread().isInterrupted()) {
            try {
                this.doRun();
            } catch (Throwable t) {
                getLogger().unexpectedError(getThreadName(), t);
            }
        }
        this.status = ServerProcessStatus.SHUTDOWN;
    }

    protected abstract void doRun ();

    protected void reschedule () {
        try {
            int seconds = this.schedulingInterpollDelay.getSeconds();
            Thread.sleep(seconds * DateTimeConstants.MILLIS_PER_SECOND);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected final void executeTasks () {
        this.getLogger().lookingForWork(this.getThreadName());
        List<ComJob> jobs = this.getComServerDAO().findExecutableOutboundComTasks(this.getComPort());
        this.queriedForTasks();
        scheduleAll(jobs);
    }

    private void queriedForTasks () {
        this.lastActivityTimestamp = this.serviceProvider.clock().instant();
        this.getOperationalMonitor().getOperationalStatistics().setLastCheckForWorkTimestamp(Date.from(this.lastActivityTimestamp));
    }

    private void scheduleAll(List<ComJob> jobs) {
        if (jobs.isEmpty()) {
            this.getLogger().noWorkFound(this.getThreadName());
            reschedule();
        } else {
            this.getLogger().workFound(this.getThreadName(), jobs.size());
            this.getJobScheduler().scheduleAll(jobs);
        }
    }

    protected abstract JobScheduler getJobScheduler ();

    protected interface JobScheduler {
        public void scheduleAll(List<ComJob> jobs);
    }

    protected ScheduledComTaskExecutionJob newComTaskJob (ComTaskExecution comTaskExecution) {
        return new ScheduledComTaskExecutionJob(this.getComPort(), this.getComServerDAO(), this.deviceCommandExecutor, comTaskExecution, this.serviceProvider);
    }

    protected ScheduledComTaskExecutionGroup newComTaskGroup (ScheduledConnectionTask connectionTask) {
        return new ScheduledComTaskExecutionGroup(this.getComPort(), this.getComServerDAO(), this.deviceCommandExecutor, connectionTask, this.serviceProvider);
    }

    protected ScheduledComTaskExecutionGroup newComTaskGroup (ComJob groupComJob) {
        ScheduledConnectionTask connectionTask = groupComJob.getConnectionTask();
        ScheduledComTaskExecutionGroup group = newComTaskGroup(connectionTask);
        for (ComTaskExecution scheduledComTask : groupComJob.getComTaskExecutions()) {
            group.add(scheduledComTask);
        }
        return group;

    }

    ServiceProvider getServiceProvider() {
        return serviceProvider;
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

        private ComPortOperationsLogger newLogger (OutboundComPort comPort) {
            return new CompositeComPortOperationsLogger(
                    LoggerFactory.getLoggerFor(ComPortOperationsLogger.class, this.getServerLogLevel(comPort)),
                    LoggerFactory.getLoggerFor(
                            ComPortOperationsLogger.class,
                            this.getAnonymousLogger(new ComPortLogHandler(comPort, serviceProvider.eventPublisher(), new ComServerEventServiceProvider())))
            );
        }

        private LogLevel getServerLogLevel (ComPort comPort) {
            return this.getServerLogLevel(comPort.getComServer());
        }

        private LogLevel getServerLogLevel (ComServer comServer) {
            return LogLevelMapper.forComServerLogLevel().toLogLevel(comServer.getServerLogLevel());
        }

        private Logger getAnonymousLogger (Handler handler) {
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

    }

    private class ComServerEventServiceProvider implements AbstractComServerEventImpl.ServiceProvider {
        @Override
        public Clock clock() {
            return serviceProvider.clock();
        }
    }

}