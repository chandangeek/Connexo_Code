package com.energyict.mdc.engine.impl.scheduling;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.comserver.commands.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides an implementation for the {@link ComPortListener} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-03 (11:26)
 */
public abstract class ComPortListenerImpl implements ComPortListener, Runnable {

    private static final TimeDuration WAIT_AFTER_COMMUNICATION_TIMEOUT = new TimeDuration(1, TimeDuration.MINUTES);

    private volatile ServerProcessStatus status = ServerProcessStatus.SHUTDOWN;
    private InboundComPort comPort;
    private ComServerDAO comServerDAO;
    private ThreadFactory threadFactory;
    private String threadName;
    private Thread self;
    private Thread changeConfigTimerThread;
    private ChangeConfigTimer changeConfigTimer;
    private AtomicBoolean continueRunning;
    private DeviceCommandExecutor deviceCommandExecutor;
    private TimeDuration changesInterpollDelay;

    /**
     * Do the actual work for this Listener
     */
    protected abstract void doRun();

    /**
     * Apply configuration changes based on the given InboundComPort
     *
     * @param inboundComPort the inboundComPort containing new changes
     */
    protected abstract void applyChangesForNewComPort(InboundComPort inboundComPort);

    protected ComPortListenerImpl(InboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor) {
        this(comPort, comServerDAO, Executors.defaultThreadFactory(), deviceCommandExecutor);
    }

    protected ComPortListenerImpl(InboundComPort comPort, ComServerDAO comServerDAO, ThreadFactory threadFactory, DeviceCommandExecutor deviceCommandExecutor) {
        super();
        this.doSetComPort(comPort);
        this.threadName = "ComPort listener for " + comPort.getName();
        this.comServerDAO = comServerDAO;
        this.threadFactory = threadFactory;
        this.deviceCommandExecutor = deviceCommandExecutor;
        this.changeConfigTimer = new ChangeConfigTimer();
        this.changesInterpollDelay = comPort.getComServer().getChangesInterPollDelay();
    }

    public TimeDuration getChangesInterpollDelay () {
        return changesInterpollDelay;
    }

    public InboundComPort getComPort () {
        return comPort;
    }

    protected InboundComPort getServerInboundComPort(){
        return getComPort();
    }

    public void setComPort (InboundComPort comPort) {
        this.doSetComPort(comPort);
    }

    private void doSetComPort (InboundComPort comPort) {
        assert comPort != null : "Listening for a ComPort requires at least the ComPort instead of null!";
        this.comPort = comPort;
    }

    public String getThreadName () {
        return threadName;
    }

    public void setThreadName (String threadName) {
        this.threadName = threadName;
    }

    protected ComServerDAO getComServerDAO () {
        return comServerDAO;
    }

    @Override
    public ServerProcessStatus getStatus () {
        return this.status;
    }

    @Override
    public void changesInterpollDelayChanged (TimeDuration changesInterpollDelay) {
        this.changesInterpollDelay = changesInterpollDelay;
    }

    @Override
    public void schedulingInterpollDelayChanged (TimeDuration schedulingInterpollDelay) {
        // No implementation requires as we do not actually schedule but wait for an incoming connection
    }

    @Override
    public final void start () {
        this.doStart();
    }

    protected void doStart () {
        this.status = ServerProcessStatus.STARTING;
        this.continueRunning = new AtomicBoolean(true);
        this.self = this.threadFactory.newThread(this);
        this.self.setName(this.getThreadName());
        this.self.start();
        this.changeConfigTimerThread = getThreadFactory().newThread(changeConfigTimer);
        this.changeConfigTimerThread.setName("Changes monitor for inbound ComPort " + getComPort().getName());
        this.changeConfigTimerThread.start();
        this.status = ServerProcessStatus.STARTED;
    }

    @Override
    public final void shutdown () {
        this.doShutdown();
    }

    @Override
    public void shutdownImmediate () {
        this.doShutdown();
    }

    protected void doShutdown () {
        this.status = ServerProcessStatus.SHUTTINGDOWN;
        this.continueRunning.set(false);
        this.changeConfigTimer.stopTimer();
        this.changeConfigTimerThread.interrupt();
        this.self.interrupt();
    }

    @Override
    public void run () {
        while (this.continueRunning.get() && !Thread.currentThread().isInterrupted()) {
            try {
                this.doRun();
            } catch (CommunicationException e) {
                waitAfterCommunicationTimeOut();
            }
        }
        this.status = ServerProcessStatus.SHUTDOWN;
    }

    /**
     * Nicely wait after a {@link CommunicationException} so things can get back to normal
     */
    private void waitAfterCommunicationTimeOut() {
        try {
            Thread.sleep(WAIT_AFTER_COMMUNICATION_TIMEOUT.getMilliSeconds());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    @Override
    public void checkAndApplyChanges() {
        ComPort newVersion = this.getComServerDAO().refreshComPort(this.getComPort());
        this.setComPort(this.applyChanges((InboundComPort) newVersion, this.getComPort()));
    }

    protected InboundComPort applyChanges (InboundComPort newVersion, InboundComPort comPort) {
        if (newVersion == null || newVersion == comPort) {
            return comPort;
        }
        else {
            applyChangesForNewComPort(newVersion);
            return newVersion;
        }
    }

    protected DeviceCommandExecutor getDeviceCommandExecutor() {
        return this.deviceCommandExecutor;
    }

    protected ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    /**
     * Simple class to check and apply changes to this ComPort.
     * It will sleep until the {@link com.energyict.mdc.engine.model.ComServer#getChangesInterPollDelay()}
     * has expired, or someone calls {@link #stopTimer()}
     */
    protected class ChangeConfigTimer implements Runnable {

        private AtomicBoolean running;

        public ChangeConfigTimer() {
            this.running = new AtomicBoolean(true);
        }

        @Override
        public void run() {
            while (this.running.get() &&!Thread.currentThread().isInterrupted()) {
                this.doRun();
            }
        }

        private void doRun() {
            try {
                Thread.sleep(getChangesInterpollDelay().getMilliSeconds());
                checkAndApplyChanges();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void setRunning(boolean state) {
            this.running.set(state);
        }

        public void stopTimer(){
            this.running.set(false);
        }
    }
}