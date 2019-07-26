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
 * Does periodic cleanup of outdated {@link com.energyict.mdc.device.data.tasks.ComTaskExecutionTrigger}s<br/>
 * All triggers who have a trigger timestamp older than one day in the past will be removed from the database,
 * as these triggers are no longer relevant.
 *
 * @author sva
 * @since 2016-06-29 (17:06)
 */
class ComServerCleanupProcessImpl implements Runnable, ComServerCleanupProcess {

    private static final TimeDuration WAIT_TIME = new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
    private volatile ServerProcessStatus status = ServerProcessStatus.SHUTDOWN;
    private AtomicBoolean continueRunning;
    private ThreadFactory threadFactory;
    private Thread self;
    private OutboundCapableComServer comServer;
    private ComServerDAO comServerDAO;

    ComServerCleanupProcessImpl(OutboundCapableComServer comServer, ComServerDAO comServerDAO, ThreadFactory threadFactory) {
        super();
        this.initialize(comServer, comServerDAO, threadFactory);
    }

    private void initialize(OutboundCapableComServer comServer, ComServerDAO comServerDAO, ThreadFactory threadFactory) {
        this.comServer = comServer;
        this.comServerDAO = comServerDAO;
        this.threadFactory = threadFactory;
    }

    @Override
    public ServerProcessStatus getStatus() {
        return this.status;
    }

    public OutboundCapableComServer getComServer() {
        return comServer;
    }

    @Override
    public void start() {
        this.status = ServerProcessStatus.STARTING;
        this.continueRunning = new AtomicBoolean(true);
        self = this.threadFactory.newThread(this);
        self.setName("Cleanup process for " + this.comServer.getName());
        self.start();
        this.status = ServerProcessStatus.STARTED;
    }

    @Override
    public void shutdown() {
        this.doShutdown();
    }

    @Override
    public void shutdownImmediate() {
        this.doShutdown();
    }

    private void doShutdown() {
        this.status = ServerProcessStatus.SHUTTINGDOWN;
        this.continueRunning.set(false);
        self.interrupt();   // in case the thread was sleeping between detecting changes
    }

    @Override
    public void run() {
        while (continueRunning.get() && !Thread.currentThread().isInterrupted()) {
            this.performCleanup();
            try {
                Thread.sleep(WAIT_TIME.getMilliSeconds());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        this.status = ServerProcessStatus.SHUTDOWN;
    }

    private void performCleanup() {
        try {
            this.comServerDAO.cleanupOutdatedComTaskExecutionTriggers();
        } catch (DataAccessException e) {
            OutboundCapableComServer comServer = this.getComServer();
            this.getLogger(comServer).comTaskExecutionTriggersCleanupFailure(comServer, e);
            throw e;
        }
    }

    private ComServerLogger getLogger(ComServer comServer) {
        return LoggerFactory.getLoggerFor(ComServerLogger.class, this.getServerLogLevel(comServer));
    }

    private LogLevel getServerLogLevel(ComServer comServer) {
        return LogLevelMapper.forComServerLogLevel().toLogLevel(comServer.getServerLogLevel());
    }
}