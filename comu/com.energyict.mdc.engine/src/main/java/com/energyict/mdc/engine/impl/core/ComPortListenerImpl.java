package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.time.TimeDuration;

import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.impl.core.logging.InboundComPortLogger;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.io.CommunicationException;

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

    private static final TimeDuration WAIT_AFTER_COMMUNICATION_TIMEOUT = new TimeDuration(1, TimeDuration.TimeUnit.MINUTES);

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
    private LoggerHolder loggerHolder;

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
        this.loggerHolder = new LoggerHolder(comPort);
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
        this.getLogger().started(this.getThreadName());
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
        this.getLogger().shuttingDown(this.getThreadName());
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
     * Nicely wait after a {@link CommunicationException} so things can get back to normal.
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
        this.getLogger().monitoringChanges(this.getComPort());
        InboundComPort newVersion = (InboundComPort) this.getComServerDAO().refreshComPort(this.getComPort());
        this.loggerHolder.reset(newVersion);
        this.setComPort(this.applyChanges(newVersion, this.getComPort()));
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

    protected InboundComPortLogger getLogger() {
        return this.loggerHolder.get();
    }

    /**
     * Simple class to check and apply changes to this ComPort.
     * It will sleep until the {@link com.energyict.mdc.engine.config.ComServer#getChangesInterPollDelay()}
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

    private class LoggerHolder {
        private InboundComPort comPort;
        private InboundComPortLogger logger;

        private LoggerHolder(InboundComPort comPort) {
            super();
            this.reset(comPort);
        }

        private InboundComPortLogger get() {
            return logger;
        }

        private InboundComPortLogger newLogger (ComPort comPort) {
            return LoggerFactory.getLoggerFor(InboundComPortLogger.class, this.getServerLogLevel(comPort));
        }

        private LogLevel getServerLogLevel (ComPort comPort) {
            return this.getServerLogLevel(comPort.getComServer());
        }

        private LogLevel getServerLogLevel (ComServer comServer) {
            return LogLevelMapper.forComServerLogLevel().toLogLevel(comServer.getServerLogLevel());
        }

        public void reset(InboundComPort comPort) {
            this.comPort = comPort;
            this.logger = this.newLogger(comPort);
        }

    }

}