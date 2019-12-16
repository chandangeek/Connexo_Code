/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.OutboundCapableComServer;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.impl.core.logging.ComServerLogger;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Monitors the exeuction of {@link ComTaskExecution}
 * and will abort executions that run longer than the task execution timeout
 * that is specified on the {@link OutboundComPortPool}
 * of the OutboundComPort it is running on.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-03 (12:44)
 */
public class TimeOutMonitorImpl implements Runnable, TimeOutMonitor {
    private static final Logger LOGGER = Logger.getLogger(TimeOutMonitorImpl.class.getName());
    private static final TimeDuration DEFAULT_WAIT_TIME = new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
    private volatile ServerProcessStatus status = ServerProcessStatus.SHUTDOWN;
    private AtomicBoolean continueRunning;
    private ThreadFactory threadFactory;
    private Thread self;
    private OutboundCapableComServer comServer;
    private ComServerDAO comServerDAO;
    private long waitTime;

    public TimeOutMonitorImpl(OutboundCapableComServer comServer, ComServerDAO comServerDAO, ThreadFactory threadFactory) {
        super();
        initialize(comServer, comServerDAO, threadFactory);
    }

    private void initialize(OutboundCapableComServer comServer, ComServerDAO comServerDAO, ThreadFactory threadFactory) {
        this.comServer = comServer;
        this.comServerDAO = comServerDAO;
        this.threadFactory = threadFactory;
    }

    @Override
    public ServerProcessStatus getStatus() {
        return status;
    }

    public OutboundCapableComServer getComServer() {
        return comServer;
    }

    @Override
    public void start() {
        status = ServerProcessStatus.STARTING;
        continueRunning = new AtomicBoolean(true);
        self = threadFactory.newThread(this);
        self.setName("Timeout monitor for " + comServer.getName());
        // the first time, execute the cleanup asynchronously, to not clash with the cleanup started on the outbound ComPorts
        monitorTasks();
        self.start();
        status = ServerProcessStatus.STARTED;
    }

    @Override
    public void shutdown() {
        doShutdown();
    }

    @Override
    public void shutdownImmediate() {
        doShutdown();
    }

    private void doShutdown() {
        status = ServerProcessStatus.SHUTTINGDOWN;
        continueRunning.set(false);
        self.interrupt();   // in case the thread was sleeping between detecting changes
    }

    @Override
    public void run() {
        while (continueRunning.get() && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(waitTime);
                monitorTasks();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        this.status = ServerProcessStatus.SHUTDOWN;
    }

    private void monitorTasks() {
        try {
            waitTime = releaseTimedOutTasks();
        } catch (RuntimeException e) {
            waitTime = DEFAULT_WAIT_TIME.getMilliSeconds();
        }
    }

    private long releaseTimedOutTasks() {
        try {
            long waitTimeMax = 0;
            for (ComPort comPort : comServer.getComPorts()) {
                waitTimeMax = Math.max(waitTimeMax, comServerDAO.releaseTimedOutTasks(comPort).getMilliSeconds());
            }

            return waitTimeMax;
        } catch (DataAccessException e) {
            getLogger(comServer).timeOutCleanupFailure(comServer, e);
            throw e;
        }
    }

    private ComServerLogger getLogger(ComServer comServer) {
        return LoggerFactory.getLoggerFor(ComServerLogger.class, getServerLogLevel(comServer));
    }

    private LogLevel getServerLogLevel(ComServer comServer) {
        return LogLevelMapper.forComServerLogLevel().toLogLevel(comServer.getServerLogLevel());
    }

}