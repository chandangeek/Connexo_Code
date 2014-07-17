package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.engine.impl.monitor.ScheduledComPortMonitor;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.OutboundComPort;

import org.joda.time.DateTimeConstants;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

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
    }

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
    public final void start () {
        doStart();
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

    private void registerAsMBean() {
        this.operationalMonitor = (ScheduledComPortMonitor) this.serviceProvider.managementBeanFactory().findOrCreateFor(this);
    }

    protected ScheduledComPortMonitor getOperationalMonitor() {
        return this.operationalMonitor;
    }

    @Override
    public void shutdown () {
        this.doShutdown();
    }

    private void doShutdown () {
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
        while (this.continueRunning.get() && !Thread.currentThread().isInterrupted()) {
            try {
                this.doRun();
            }
            catch (Throwable t) {
                /* AOP handles logging.
                 * Eat exception to avoid that the thread stops. */
                this.eat(t);
            }
        }
        this.status = ServerProcessStatus.SHUTDOWN;
    }

    private void eat (Throwable yummy) {
        yummy.printStackTrace(System.err);
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
        List<ComJob> jobs = this.getComServerDAO().findExecutableOutboundComTasks(this.getComPort());
        this.queriedForTasks();
        scheduleAll(jobs);
    }

    private void queriedForTasks () {
        this.getOperationalMonitor().getOperationalStatistics().setLastCheckForChangesTimestamp(this.serviceProvider.clock().now());
    }

    private void scheduleAll(List<ComJob> jobs) {
        if(jobs.isEmpty()){
            reschedule();
        } else {
            this.getJobScheduler().scheduleAll(jobs);
        }
    }

    protected abstract JobScheduler getJobScheduler ();

    public void checkAndApplyChanges () {
        ComPort newVersion = this.getComServerDAO().refreshComPort(this.getComPort());
        this.setComPort(this.applyChanges((OutboundComPort) newVersion, this.getComPort()));
    }

    private OutboundComPort applyChanges (OutboundComPort newVersion, OutboundComPort comPort) {
        if (newVersion == null || newVersion == comPort) {
            return comPort;
        }
        else {
            // Todo: detect and apply changes
            return newVersion;
        }
    }

    protected interface JobScheduler {
        public int scheduleAll(List<ComJob> jobs);
    }

    protected ScheduledComTaskExecutionJob newComTaskJob (ComTaskExecution comTask) {
        return new ScheduledComTaskExecutionJob(this.getComPort(), this.getComServerDAO(), this.deviceCommandExecutor, comTask, this.serviceProvider);
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

}