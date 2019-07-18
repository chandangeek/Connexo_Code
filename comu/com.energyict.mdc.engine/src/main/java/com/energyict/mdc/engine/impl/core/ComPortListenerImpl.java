/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.orm.PersistenceException;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.core.logging.InboundComPortLogger;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.engine.monitor.InboundComPortMonitor;

import com.energyict.protocol.exceptions.CommunicationException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides an implementation for the {@link ComPortListener} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-03 (11:26)
 */
abstract class ComPortListenerImpl implements ComPortListener, Runnable {

    private static final Duration WAIT_AFTER_COMMUNICATION_TIMEOUT = Duration.ofMinutes(1);

    private final RunningComServer runningComServer;
    private volatile ServerProcessStatus status = ServerProcessStatus.SHUTDOWN;
    private final InboundCommunicationHandler.ServiceProvider serviceProvider;
    private InboundComPortMonitor operationalMonitor;
    private InboundComPort comPort;
    private ComServerDAO comServerDAO;
    private ThreadFactory threadFactory;
    private String threadName;
    private Thread self;
    private AtomicBoolean continueRunning;
    private DeviceCommandExecutor deviceCommandExecutor;
    private TimeDuration changesInterpollDelay;
    private LoggerHolder loggerHolder;
    private Clock clock;
    private Instant lastActivityTimestamp;

    /**
     * Do the actual work for this Listener.
     */
    protected abstract void doRun();

    protected abstract void setThreadPrinciple();

    ComPortListenerImpl(RunningComServer runningComServer, InboundComPort comPort, Clock clock, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundCommunicationHandler.ServiceProvider serviceProvider) {
        this(runningComServer, comPort, clock, comServerDAO, Executors.defaultThreadFactory(), deviceCommandExecutor, serviceProvider);
    }

    ComPortListenerImpl(RunningComServer runningComServer, InboundComPort comPort, Clock clock, ComServerDAO comServerDAO, ThreadFactory threadFactory, DeviceCommandExecutor deviceCommandExecutor, InboundCommunicationHandler.ServiceProvider serviceProvider) {
        super();
        this.runningComServer = runningComServer;
        this.loggerHolder = new LoggerHolder(comPort);
        this.doSetComPort(comPort);
        this.threadName = "Inbound ComPort listener for " + comPort.getName();
        this.comServerDAO = comServerDAO;
        this.threadFactory = threadFactory;
        this.deviceCommandExecutor = deviceCommandExecutor;
        this.serviceProvider = serviceProvider;
        this.changesInterpollDelay = comPort.getComServer().getChangesInterPollDelay();
        this.clock = clock;
        this.lastActivityTimestamp = clock.instant();
    }

    public TimeDuration getChangesInterpollDelay () {
        return changesInterpollDelay;
    }

    public InboundComPort getComPort () {
        return comPort;
    }

    InboundComPort getServerInboundComPort(){
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

    void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public Instant getLastActivityTimestamp() {
        return this.lastActivityTimestamp;
    }

    void registerActivity() {
        this.lastActivityTimestamp = this.clock.instant();
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
        this.registerAsMBean();
        this.status = ServerProcessStatus.STARTING;
        this.continueRunning = new AtomicBoolean(true);
        this.self = this.threadFactory.newThread(this);
        this.self.setName(this.getThreadName());
        this.self.start();
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
        this.unregisterAsMBean();
        this.continueRunning.set(false);
        if(self != null)
            this.self.interrupt();
    }

    @Override
    public void run () {
        setThreadPrinciple();

        this.comServerDAO.releaseTasksFor(comPort); // cleanup any previous tasks you kept busy ...

        while (this.continueRunning.get() && !Thread.currentThread().isInterrupted()) {
            try {
                this.doRun();
            } catch (CommunicationException e) {
                logUnexpectedError(e);
                waitAfterCommunicationTimeOut();
            } catch (Throwable throwable) {
                logUnexpectedError(throwable);
                if (throwable instanceof PersistenceException) {
                    this.runningComServer.refresh(getComPort());
                    this.continueRunning.set(false);
                }
            }
        }
        this.status = ServerProcessStatus.SHUTDOWN;
    }

    private void logUnexpectedError(Throwable throwable) {
        getLogger().unexpectedError(getThreadName(), throwable);
    }

    /**
     * Nicely wait after a {@link CommunicationException} so things can get back to normal.
     */
    private void waitAfterCommunicationTimeOut() {
        try {
            Thread.sleep(WAIT_AFTER_COMMUNICATION_TIMEOUT.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected InboundComPortMonitor getOperationalMonitor() {
        return this.operationalMonitor;
    }

    private void registerAsMBean() {
         this.operationalMonitor = (InboundComPortMonitor) this.serviceProvider.managementBeanFactory().findOrCreateFor(this);
     }

     private void unregisterAsMBean() {
         this.serviceProvider.managementBeanFactory().removeIfExistsFor(this);
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

    InboundCommunicationHandler.ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    private class LoggerHolder {
        private InboundComPortLogger logger;

        private LoggerHolder(InboundComPort comPort) {
            super();
            this.initialize(comPort);
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

        public void initialize(InboundComPort comPort) {
            this.logger = this.newLogger(comPort);
        }
    }
}