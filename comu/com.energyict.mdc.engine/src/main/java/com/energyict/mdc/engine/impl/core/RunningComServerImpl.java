package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.impl.core.devices.DeviceCommandExecutorImpl;
import com.energyict.mdc.engine.impl.core.factories.ComPortListenerFactory;
import com.energyict.mdc.engine.impl.core.factories.ComPortListenerFactoryImpl;
import com.energyict.mdc.engine.impl.core.factories.ScheduledComPortFactory;
import com.energyict.mdc.engine.impl.core.factories.ScheduledComPortFactoryImpl;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServer;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServerFactory;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundCapable;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundCapable;
import com.energyict.mdc.engine.model.OutboundCapableComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.RemoteComServer;

import org.joda.time.DateTimeConstants;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides an implementation for the {@link RunningComServer} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-03 (09:58)
 */
public class RunningComServerImpl implements RunningComServer, Runnable {

    /**
     * The default queue size of the DeviceCommandExecutor
     * that is used for non {@link OnlineComServer}s.
     */
    private static final int DEFAULT_STORE_TASK_QUEUE_SIZE = 50;

    /**
     * The default number of threads of the DeviceCommandExecutor
     * that is used for non {@link OnlineComServer}s.
     */
    private static final int DEFAULT_NUMBER_OF_THREADS = 1;

    /**
     * The number of milliseconds that this server will wait for nested
     * {@link ServerProcess}es to complete the shutdown process.
     */
    private static final long SHUTDOWN_WAIT_TIME = 100;
    private final ServiceProvider serviceProvider;

    private volatile ServerProcessStatus status = ServerProcessStatus.SHUTDOWN;
    private AtomicBoolean continueRunning;
    private Thread self;
    private ComServerDAO comServerDAO;
    private ScheduledComPortFactory scheduledComPortFactory;
    private ComPortListenerFactory comPortListenerFactory;
    private ThreadFactory threadFactory;
    private ComServer comServer;
    private Collection<ScheduledComPort> scheduledComPorts = new ArrayList<>();
    private Collection<ComPortListener> comPortListeners = new ArrayList<>();
    private EmbeddedWebServer eventMechanism;
    private DeviceCommandExecutorImpl deviceCommandExecutor;
    private CleanupDuringStartup cleanupDuringStartup;
    private TimeOutMonitor timeOutMonitor;

    protected RunningComServerImpl(OnlineComServer comServer, ComServerDAO comServerDAO, ScheduledComPortFactory scheduledComPortFactory, ComPortListenerFactory comPortListenerFactory, CleanupDuringStartup cleanupDuringStartup, ServiceProvider serviceProvider) {
        this(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new ComServerThreadFactory(comServer), cleanupDuringStartup, serviceProvider);
    }

    protected RunningComServerImpl(OnlineComServer comServer, ComServerDAO comServerDAO, ScheduledComPortFactory scheduledComPortFactory, ComPortListenerFactory comPortListenerFactory, ThreadFactory threadFactory, CleanupDuringStartup cleanupDuringStartup, ServiceProvider serviceProvider) {
        super();
        this.serviceProvider = serviceProvider;
        this.initialize(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, cleanupDuringStartup);
        this.initializeDeviceCommandExecutor(comServer);
        this.initializeTimeoutMonitor(comServer);
        this.addOutboundComPorts(comServer.getOutboundComPorts());
        this.addInboundComPorts(comServer.getInboundComPorts());
    }

    protected RunningComServerImpl(RemoteComServer comServer, ComServerDAO comServerDAO, ScheduledComPortFactory scheduledComPortFactory, ComPortListenerFactory comPortListenerFactory, ThreadFactory threadFactory, CleanupDuringStartup cleanupDuringStartup, ServiceProvider serviceProvider) {
        super();
        this.serviceProvider = serviceProvider;
        this.initialize(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, cleanupDuringStartup);
        this.initializeDeviceCommandExecutor(DEFAULT_STORE_TASK_QUEUE_SIZE, DEFAULT_NUMBER_OF_THREADS, Thread.NORM_PRIORITY);
        this.initializeTimeoutMonitor(comServer);
        this.addOutboundComPorts(comServer.getOutboundComPorts());
        this.addInboundComPorts(comServer.getInboundComPorts());
    }

    private void initialize (ComServer comServer, ComServerDAO comServerDAO, ScheduledComPortFactory scheduledComPortFactory, ComPortListenerFactory comPortListenerFactory, ThreadFactory threadFactory, CleanupDuringStartup cleanupDuringStartup) {
        this.comServer = comServer;
        this.comServerDAO = comServerDAO;
        this.scheduledComPortFactory = scheduledComPortFactory;
        this.comPortListenerFactory = comPortListenerFactory;
        this.threadFactory = threadFactory;
        this.cleanupDuringStartup = cleanupDuringStartup;
    }

    protected ComServerDAO getComServerDAO () {
        return comServerDAO;
    }

    protected final ThreadFactory getThreadFactory() {
        return this.threadFactory;
    }

    private void initializeTimeoutMonitor (OutboundCapableComServer comServer) {
        this.timeOutMonitor = new TimeOutMonitorImpl(comServer, this.comServerDAO, this.threadFactory);
    }

    private void addOutboundComPorts (List<OutboundComPort> outboundComPorts) {
        for (OutboundComPort comPort : outboundComPorts) {
            this.add(comPort);
        }
    }

    private ScheduledComPort add (OutboundComPort comPort) {
        ScheduledComPort scheduledComPort = this.getScheduledComPortFactory().newFor(comPort, this.serviceProvider);
        if (scheduledComPort == null) {
            return null;
        }
        else {
            this.add(scheduledComPort);
            return scheduledComPort;
        }
    }

    private ScheduledComPortFactory getScheduledComPortFactory () {
        if (this.scheduledComPortFactory == null) {
            this.scheduledComPortFactory = new ScheduledComPortFactoryImpl(serviceProvider, this.comServerDAO, this.deviceCommandExecutor, this.getThreadFactory());
        }
        return this.scheduledComPortFactory;
    }

    /**
     * Notifies interested parties that the specified {@link OutboundComPort}
     * is ignored because it is inactive.
     *
     * @param comPort The OutboundComPort that is ignored
     */
    private void ignored (OutboundComPort comPort) {
        // No implementation required yet
    }

    private ScheduledComPort add (ScheduledComPort scheduledComPort) {
        this.scheduledComPorts.add(scheduledComPort);
        return scheduledComPort;
    }

    private void addInboundComPorts (List<InboundComPort> inboundComPorts) {
        for (InboundComPort comPort : inboundComPorts) {
            this.add(comPort);
        }
    }

    private ComPortListener add (InboundComPort comPort) {
        ComPortListener comPortListener = getComPortListenerFactory().newFor(comPort, this.serviceProvider);
        if (comPortListener == null) {
            return null;
        }
        else {
            this.add(comPortListener);
            return comPortListener;
        }
    }

    private void initializeDeviceCommandExecutor (OnlineComServer comServer) {
        this.initializeDeviceCommandExecutor(comServer.getStoreTaskQueueSize(), comServer.getNumberOfStoreTaskThreads(), comServer.getStoreTaskThreadPriority());
    }

    private void initializeDeviceCommandExecutor (int queueSize, int numberOfThreads, int threadPriority) {
        this.deviceCommandExecutor = new DeviceCommandExecutorImpl(this.getComServer(), queueSize, numberOfThreads, threadPriority, this.getComServer().getServerLogLevel(), this.threadFactory, this.comServerDAO, this.serviceProvider.threadPrincipalService(), this.serviceProvider.userService());
    }

    /**
     * Notifies interested parties that the specified {@link InboundComPort}
     * is ignored because it is inactive.
     *
     * @param comPort The InboundComPort that is ignored
     */
    private void ignored (InboundComPort comPort) {
        // No implementation required yet
    }

    private ComPortListener add (ComPortListener comPortListener) {
        this.comPortListeners.add(comPortListener);
        return comPortListener;
    }

    @Override
    public ServerProcessStatus getStatus () {
        return this.status;
    }

    @Override
    public final void start () {
        this.status = ServerProcessStatus.STARTING;
        this.continueRunning = new AtomicBoolean(true);
        try {
            this.cleanupDuringStartup.releaseInterruptedTasks();
            this.continueStartupAfterCleanup();
        }
        catch (SQLException e) {
            this.cleanupFailed();
        }
    }

    private void cleanupFailed () {
        this.status = ServerProcessStatus.SHUTDOWN;
    }

    private void continueStartupAfterCleanup () {
        try {
            this.startComServerDAO();
            this.continueStartupAfterDAOStart();
        }
        catch (RuntimeException e) {
            this.comServerDAOStartFailed(e);
        }
    }

    protected void startComServerDAO () {
        this.comServerDAO.start();
    }

    private void comServerDAOStartFailed (RuntimeException e) {
        e.printStackTrace(System.err);
        this.status = ServerProcessStatus.SHUTDOWN;
    }

    protected void continueStartupAfterDAOStart () {
        this.startEventMechanism();
        this.startDeviceCommandExecutor();
        this.startOutboundComPorts();
        this.startInboundComPorts();
        this.timeOutMonitor.start();
        self = this.threadFactory.newThread(this);
        self.setName("Changes monitor for " + this.comServer.getName());
        self.start();
        this.installShutdownHook();
        this.status = ServerProcessStatus.STARTED;
    }

    private void installShutdownHook () {
        Thread shutdownHook = this.threadFactory.newThread(new Runnable() {
            @Override
            public void run () {
                shutdownImmediate();
            }
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    private void startOutboundComPorts () {
        for (ScheduledComPort scheduledComPort : this.scheduledComPorts) {
            scheduledComPort.start();
        }
    }

    private void startInboundComPorts () {
        for (ComPortListener comPortListener : this.comPortListeners) {
            comPortListener.start();
        }
    }

    private void startEventMechanism () {
        this.eventMechanism = EmbeddedWebServerFactory.DEFAULT.get().findOrCreateEventWebServer(this.comServer);
        this.eventMechanism.start();
    }

    private void startDeviceCommandExecutor () {
        this.deviceCommandExecutor.start();
    }

    @Override
    public final void shutdown () {
        this.shutdown(false);
    }

    @Override
    public void shutdownImmediate () {
        this.shutdown(true);
    }

    private void shutdown (boolean immediate) {
        this.doShutdown();
        this.shutdownNestedServerProcesses(immediate);
        if (!immediate) {
            this.awaitNestedServerProcessesAreShutDown();
        }
    }

    protected void shutdownNestedServerProcesses (boolean immediate) {
        this.shutdownComServerDAO(immediate);
        this.shutdownEventMechanism(immediate);
        this.shutdownTimeOutMonitor(immediate);
        this.shutdownOutboundComPorts(immediate);
        this.shutdownInboundComPorts(immediate);
        this.shutdownDeviceCommandExecutor(immediate);
    }

    private void shutdownComServerDAO (boolean immediate) {
        if (immediate) {
            this.comServerDAO.shutdownImmediate();
        }
        else {
            this.comServerDAO.shutdown();
        }
    }

    private void shutdownEventMechanism (boolean immediate) {
        if (this.eventMechanism != null) {
            if (immediate) {
                this.eventMechanism.shutdownImmediate();
            }
            else {
                this.eventMechanism.shutdown();
            }
        }
    }

    private void doShutdown () {
        this.status = ServerProcessStatus.SHUTTINGDOWN;
        this.continueRunning.set(false);
        self.interrupt();   // in case the thread was sleeping between detecting changes
    }

    private void shutdownTimeOutMonitor (boolean immediate) {
        if (immediate) {
            this.timeOutMonitor.shutdownImmediate();
        }
        else {
            this.timeOutMonitor.shutdown();
        }
    }

    private void awaitNestedServerProcessesAreShutDown () {
        while (this.atLeastOneNestedServerProcessStillRunning()) {
            try {
                Thread.sleep(SHUTDOWN_WAIT_TIME);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private boolean atLeastOneNestedServerProcessStillRunning () {
        for (ServerProcess serverProcess : this.getNestedServerProcesses()) {
            if (!ServerProcessStatus.SHUTDOWN.equals(serverProcess.getStatus())) {
                return true;
            }
        }
        return false;
    }

    protected List<ServerProcess> getNestedServerProcesses () {
        List<ServerProcess> nestedProcesses = new ArrayList<>();
        nestedProcesses.addAll(this.scheduledComPorts);
        nestedProcesses.addAll(this.comPortListeners);
        nestedProcesses.add(this.deviceCommandExecutor);
        return nestedProcesses;
    }

    private void shutdownOutboundComPorts (boolean immediate) {
        for (ScheduledComPort scheduledComPort : this.scheduledComPorts) {
            if(immediate){
                scheduledComPort.shutdownImmediate();
            } else {
                scheduledComPort.shutdown();
            }
        }
    }

    private void shutdownInboundComPorts (boolean immediate) {
        for (ComPortListener comPortListener : this.comPortListeners) {
            if(immediate){
                comPortListener.shutdownImmediate();
            } else {
                comPortListener.shutdown();
            }
        }
    }

    private void shutdownDeviceCommandExecutor (boolean immediate) {
        if(immediate){
            this.deviceCommandExecutor.shutdownImmediate();
        } else {
            this.deviceCommandExecutor.shutdown();
        }
    }

    @Override
    public void run () {
        while (continueRunning.get() && !Thread.currentThread().isInterrupted()) {
            this.monitorChanges();
        }
        this.status = ServerProcessStatus.SHUTDOWN;
    }

    private void monitorChanges () {
        try {
            Thread.sleep(this.getChangesInterPollDelayMillis());
            ComServer newVersion =  this.comServerDAO.refreshComServer(this.comServer);
            if (newVersion == null) {
                // ComServer was deleted or made obsolete, shutdown
                this.shutdown();
            }
            else if (newVersion == this.comServer) {
                // No changes found
            }
            else {
                if (!newVersion.isActive()) {
                    // ComServer is no longer active, shutdown
                    this.shutdown();
                }
                else {
                    this.applyChanges((InboundCapable) newVersion);
                    this.applyChanges((OutboundCapable) newVersion);
                    this.applyDelayChanges(newVersion);
                    this.notifyChangesApplied();
                    this.comServer = newVersion;
                }
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void applyDelayChanges (ComServer newVersion) {
        if (!this.comServer.getSchedulingInterPollDelay().equals(newVersion.getSchedulingInterPollDelay())) {
            this.notifySchedulingInterPollDelayChange(newVersion.getSchedulingInterPollDelay());
        }
        if (!this.comServer.getChangesInterPollDelay().equals(newVersion.getChangesInterPollDelay())) {
            this.notifyChangesInterPollDelayChange(newVersion.getChangesInterPollDelay());
        }
    }

    private void notifySchedulingInterPollDelayChange (TimeDuration schedulingInterPollDelay) {
        for (ScheduledComPort comPort : this.scheduledComPorts) {
            comPort.schedulingInterpollDelayChanged(schedulingInterPollDelay);
        }
        for (ComPortListener comPort : this.comPortListeners) {
            comPort.schedulingInterpollDelayChanged(schedulingInterPollDelay);
        }
    }

    private void notifyChangesInterPollDelayChange (TimeDuration changesInterPollDelay) {
        for (ScheduledComPort comPort : this.scheduledComPorts) {
            comPort.changesInterpollDelayChanged(changesInterPollDelay);
        }
        for (ComPortListener comPort : this.comPortListeners) {
            comPort.changesInterpollDelayChanged(changesInterPollDelay);
        }
    }

    private void applyChanges (InboundCapable newVersion) {
        this.shutdownAndRemoveOldInboundComPorts(newVersion);
        this.restartChangedInboundComPorts(newVersion);
        this.addAndStartNewInboundComPorts(newVersion);
    }

    protected void notifyChangesApplied() {
        // Notify interested parties that changes have been applied
    }

    private void applyChanges (OutboundCapable newVersion) {
        this.shutdownAndRemoveOldOutboundComPorts(newVersion);
        this.restartChangedOutboundComPorts(newVersion);
        this.addAndStartNewOutboundComPorts(newVersion);
        if (newVersion instanceof OnlineComServer) {
            OnlineComServer onlineComServer = (OnlineComServer) newVersion;
            this.applyDeviceCommandExecutorChanges(onlineComServer);
        }
    }

    private void applyDeviceCommandExecutorChanges (OnlineComServer newVersion) {
        this.deviceCommandExecutor.changeQueueCapacity(newVersion.getStoreTaskQueueSize());
        this.deviceCommandExecutor.changeNumberOfThreads(newVersion.getNumberOfStoreTaskThreads());
        this.deviceCommandExecutor.changeThreadPriority(newVersion.getStoreTaskThreadPriority());
    }

    private void restartChangedInboundComPorts (InboundCapable newVersion) {
        for (InboundComPort changedComPort : this.changedInboundComPortsIn(newVersion)) {
            ComPortListener oldListener = this.getListenerFor(changedComPort);
            oldListener.shutdown();
            this.comPortListeners.remove(oldListener);
            ComPortListener rescheduled = this.add(changedComPort);
            rescheduled.start();
        }
    }

    private void addAndStartNewInboundComPorts (InboundCapable newVersion) {
        for (InboundComPort comPort : this.newActivatedInboundComPortsIn(newVersion)) {
            ComPortListener comPortListener = this.add(comPort);
            if (comPortListener != null) {
                comPortListener.start();
            }
            else {
                this.ignored(comPort);
            }
        }
    }

    /**
     * Returns the existing {@link InboundComPort}s that have changed in the new version.
     *
     * @param newVersion The new version of this {@link RunningComServer}
     * @return The Collection of changed InboundComPorts
     */
    private Collection<InboundComPort> changedInboundComPortsIn (InboundCapable newVersion) {
        List<InboundComPort> newVersionInboundComPorts = newVersion.getInboundComPorts();
        Collection<InboundComPort> changed = new ArrayList<>(newVersionInboundComPorts.size());    // at most all ComPorts in the new version can have changed
        for (InboundComPort inboundComPort : newVersionInboundComPorts) {
            ComPortListener existingListener = this.getListenerFor(inboundComPort);
            if (existingListener!= null && this.hasChanged(existingListener.getComPort(), inboundComPort)) {
                changed.add(inboundComPort);
            }
        }
        return changed;
    }

    private boolean hasChanged (InboundComPort existingComPort, InboundComPort newVersion) {
        return existingComPort.getNumberOfSimultaneousConnections() != newVersion.getNumberOfSimultaneousConnections();
    }

    /**
     * Returns the newly activated {@link InboundComPort}s that can be found in the new version.
     *
     * @param newVersion The new version of this {@link RunningComServer}
     * @return The Collection of newly activated InboundComPorts
     */
    private Collection<InboundComPort> newActivatedInboundComPortsIn (InboundCapable newVersion) {
        List<InboundComPort> newVersionInboundComPorts = newVersion.getInboundComPorts();
        Collection<InboundComPort> newlyActivated = new ArrayList<>(newVersionInboundComPorts.size());    // at most all ComPorts in the new version can be activated
        for (InboundComPort inboundComPort : newVersionInboundComPorts) {
            if (inboundComPort.isActive() && !this.isAlreadyActive(inboundComPort)) {
                newlyActivated.add(inboundComPort);
            }
        }
        return newlyActivated;
    }

    private void shutdownAndRemoveOldInboundComPorts (InboundCapable newVersion) {
        for (InboundComPort comPort : this.deactivatedInboundComPortsIn(newVersion)) {
            ComPortListener listener = this.getListenerFor(comPort);
            this.comPortListeners.remove(listener);
            listener.shutdown();
        }
        List<InboundComPort> actualComPorts = newVersion.getInboundComPorts();
        Iterator<ComPortListener> comPortListenerIterator = this.comPortListeners.iterator();
        while (comPortListenerIterator.hasNext()) {
            ComPortListener comPortListener = comPortListenerIterator.next();
            if (!actualComPorts.contains(comPortListener.getComPort())) {
                comPortListenerIterator.remove();
                comPortListener.shutdown();
            }
        }
    }

    /**
     * Returns the {@link InboundComPort}s that have been deactivated in the new version.
     *
     * @param newVersion The new version of this {@link RunningComServer}
     * @return The Collection of deactivated InboundComPorts
     */
    private Collection<InboundComPort> deactivatedInboundComPortsIn (InboundCapable newVersion) {
        List<InboundComPort> newVersionInboundComPorts = newVersion.getInboundComPorts();
        Collection<InboundComPort> deactivated = new ArrayList<>(newVersionInboundComPorts.size());    // at most all ComPorts in the new version can be activated
        for (InboundComPort inboundComPort : newVersionInboundComPorts) {
            if (this.isAlreadyActive(inboundComPort) && !inboundComPort.isActive()) {
                deactivated.add(inboundComPort);
            }
        }
        return deactivated;
    }

    private ComPortListener getListenerFor (InboundComPort comPort) {
        for (ComPortListener listener : this.comPortListeners) {
            if (comPort.getId() == listener.getComPort().getId()) {
                return listener;
            }
        }
        return null;
    }

    private boolean isAlreadyActive (InboundComPort comPort) {
        return this.getListenerFor(comPort) != null;
    }

    private void restartChangedOutboundComPorts (OutboundCapable newVersion) {
        for (OutboundComPort changedComPort : this.changedOutboundComPortsIn(newVersion)) {
            ScheduledComPort previouslyScheduledComPort = this.getSchedulerFor(changedComPort);
            previouslyScheduledComPort.shutdown();
            this.scheduledComPorts.remove(previouslyScheduledComPort);
            ScheduledComPort rescheduled = this.add(changedComPort);
            rescheduled.start();
        }
    }

    private void addAndStartNewOutboundComPorts (OutboundCapable newVersion) {
        for (OutboundComPort comPort : this.newActivatedOutboundComPortsIn(newVersion)) {
            ScheduledComPort scheduledComPort = this.add(comPort);
            if (scheduledComPort != null) {
                scheduledComPort.start();
            }
            else {
                this.ignored(comPort);
            }
        }
    }

    /**
     * Returns the existing {@link OutboundComPort}s that have changed in the new version.
     *
     * @param newVersion The new version of this {@link RunningComServer}
     * @return The Collection of changed OutboundComPorts
     */
    private Collection<OutboundComPort> changedOutboundComPortsIn (OutboundCapable newVersion) {
        List<OutboundComPort> newVersionOutboundComPorts = newVersion.getOutboundComPorts();
        Collection<OutboundComPort> changed = new ArrayList<>(newVersionOutboundComPorts.size());    // at most all ComPorts in the new version can have changed
        for (OutboundComPort outboundComPort : newVersionOutboundComPorts) {
            ScheduledComPort existingScheduledComPort = this.getSchedulerFor(outboundComPort);
            if (existingScheduledComPort != null && this.hasChanged(existingScheduledComPort.getComPort(), outboundComPort)) {
                changed.add(outboundComPort);
            }
        }
        return changed;
    }

    private boolean hasChanged (OutboundComPort existingComPort, OutboundComPort newVersion) {
        return existingComPort.getNumberOfSimultaneousConnections() != newVersion.getNumberOfSimultaneousConnections();
    }

    /**
     * Returns the newly activated {@link OutboundComPort}s that can be found in the new version.
     *
     * @param newVersion The new version of this {@link RunningComServer}
     * @return The Collection of newly activated OutboundComPorts
     */
    private Collection<OutboundComPort> newActivatedOutboundComPortsIn (OutboundCapable newVersion) {
        List<OutboundComPort> newVersionOutboundComPorts = newVersion.getOutboundComPorts();
        Collection<OutboundComPort> newlyActivated = new ArrayList<>(newVersionOutboundComPorts.size());    // at most all ComPorts in the new version can be activated
        for (OutboundComPort outboundComPort : newVersionOutboundComPorts) {
            if (outboundComPort.isActive() && !this.isAlreadyActive(outboundComPort)) {
                newlyActivated.add(outboundComPort);
            }
        }
        return newlyActivated;
    }

    private void shutdownAndRemoveOldOutboundComPorts (OutboundCapable newVersion) {
        for (OutboundComPort comPort : this.deactivatedOutboundComPortsIn(newVersion)) {
            ScheduledComPort scheduledComPort = this.getSchedulerFor(comPort);
            this.scheduledComPorts.remove(scheduledComPort);
            scheduledComPort.shutdown();
        }
        List<OutboundComPort> actualComPorts = newVersion.getOutboundComPorts();
        Iterator<ScheduledComPort> scheduledComPortIterator = this.scheduledComPorts.iterator();
        while (scheduledComPortIterator.hasNext()) {
            ScheduledComPort scheduledComPort = scheduledComPortIterator.next();
            if (!actualComPorts.contains(scheduledComPort.getComPort())) {
                scheduledComPortIterator.remove();
                scheduledComPort.shutdown();
            }
        }
    }

    /**
     * Returns the {@link OutboundComPort}s that have been deactivated in the new version.
     *
     * @param newVersion The new version of this {@link RunningComServer}
     * @return The Collection of deactivated OutboundComPorts
     */
    private Collection<OutboundComPort> deactivatedOutboundComPortsIn (OutboundCapable newVersion) {
        List<OutboundComPort> newVersionOutboundComPorts = newVersion.getOutboundComPorts();
        Collection<OutboundComPort> deactivated = new ArrayList<>(newVersionOutboundComPorts.size());    // at most all ComPorts in the new version can be activated
        for (OutboundComPort outboundComPort : newVersionOutboundComPorts) {
            if (this.isAlreadyActive(outboundComPort) && !outboundComPort.isActive()) {
                deactivated.add(outboundComPort);
            }
        }
        return deactivated;
    }

    private ScheduledComPort getSchedulerFor (OutboundComPort comPort) {
        for (ScheduledComPort scheduledComPort : this.scheduledComPorts) {
            if (comPort.getId() == scheduledComPort.getComPort().getId()) {
                return scheduledComPort;
            }
        }
        return null;
    }

    private boolean isAlreadyActive (OutboundComPort comPort) {
        return this.getSchedulerFor(comPort) != null;
    }

    private int getChangesInterPollDelayMillis () {
        return this.comServer.getChangesInterPollDelay().getSeconds() * DateTimeConstants.MILLIS_PER_SECOND;
    }

    private ComPortListenerFactory getComPortListenerFactory() {
        if (this.comPortListenerFactory == null) {
            this.comPortListenerFactory = new ComPortListenerFactoryImpl(this.comServerDAO, this.deviceCommandExecutor, this.threadFactory);
        }
        return this.comPortListenerFactory;
    }

    @Override
    public ComServer getComServer () {
        return this.comServer;
    }

    @Override
    public boolean isRemoteQueryApiStarted () {
        return false;
    }

    @Override
    public int getCollectedDataStorageCapacity () {
        return this.deviceCommandExecutor.getCapacity();
    }

    @Override
    public int getCurrentCollectedDataStorageSize () {
        return this.deviceCommandExecutor.getCurrentSize();
    }

    @Override
    public int getCurrentCollectedDataStorageLoadPercentage () {
        return this.deviceCommandExecutor.getCurrentLoadPercentage();
    }

    @Override
    public int getNumberOfCollectedDataStorageThreads () {
        return this.deviceCommandExecutor.getNumberOfThreads();
    }

    @Override
    public int getCollectedDataStorageThreadPriority () {
        return this.deviceCommandExecutor.getThreadPriority();
    }

    protected ServiceProvider getServiceProvider() {
        return serviceProvider;
    }
}