/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundCapableComServer;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.impl.core.logging.ComServerLogger;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Monitors the exeuction of {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}
 * and will abort executions that run longer than the task execution timeout
 * that is specified on the {@link com.energyict.mdc.engine.config.OutboundComPortPool}
 * of the OutboundComPort it is running on.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-03 (12:44)
 */
public class TimeOutMonitorImpl implements Runnable, TimeOutMonitor {

    private static final TimeDuration DEFAULT_WAIT_TIME = new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
    private volatile ServerProcessStatus status = ServerProcessStatus.SHUTDOWN;
    private AtomicBoolean continueRunning;
    private ThreadFactory threadFactory;
    private Thread self;
    private OutboundCapableComServer comServer;
    private ComServerDAO comServerDAO;
    private long waitTime;

    public TimeOutMonitorImpl (OutboundCapableComServer comServer, ComServerDAO comServerDAO, ThreadFactory threadFactory) {
        super();
        this.initialize(comServer, comServerDAO, threadFactory);
    }

    private void initialize (OutboundCapableComServer comServer, ComServerDAO comServerDAO, ThreadFactory threadFactory) {
        this.comServer = comServer;
        this.comServerDAO = comServerDAO;
        this.threadFactory = threadFactory;
    }

    @Override
    public ServerProcessStatus getStatus () {
        return this.status;
    }

    public OutboundCapableComServer getComServer () {
        return comServer;
    }

    @Override
    public void start () {
        this.status = ServerProcessStatus.STARTING;
        this.continueRunning = new AtomicBoolean(true);
        self = this.threadFactory.newThread(this);
        self.setName("Timeout monitor for " + this.comServer.getName());
        self.start();
        this.status = ServerProcessStatus.STARTED;
    }

    @Override
    public void shutdown () {
        this.doShutdown();
    }

    @Override
    public void shutdownImmediate () {
        this.doShutdown();
    }

    private void doShutdown () {
        this.status = ServerProcessStatus.SHUTTINGDOWN;
        this.continueRunning.set(false);
        self.interrupt();   // in case the thread was sleeping between detecting changes
    }

    @Override
    public void run () {
        while (continueRunning.get() && !Thread.currentThread().isInterrupted()) {
            this.monitorTasks();
            try {
                Thread.sleep(this.waitTime);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        this.status = ServerProcessStatus.SHUTDOWN;
    }

    private void monitorTasks () {
        try {
            this.waitTime = this.releaseTimedOutTasks();
        }
        catch (RuntimeException e) {
            this.waitTime = DEFAULT_WAIT_TIME.getMilliSeconds();
        }
    }

    private long releaseTimedOutTasks () {
        try {
            return this.comServerDAO.releaseTimedOutTasks(this.comServer).getMilliSeconds();
        }
        catch (DataAccessException e) {
            OutboundCapableComServer comServer = this.getComServer();
            this.getLogger(comServer).timeOutCleanupFailure(comServer, e);
            throw e;
        }
    }

    private ComServerLogger getLogger (ComServer comServer) {
        return LoggerFactory.getLoggerFor(ComServerLogger.class, this.getServerLogLevel(comServer));
    }

    private LogLevel getServerLogLevel (ComServer comServer) {
        return LogLevelMapper.forComServerLogLevel().toLogLevel(comServer.getServerLogLevel());
    }

}