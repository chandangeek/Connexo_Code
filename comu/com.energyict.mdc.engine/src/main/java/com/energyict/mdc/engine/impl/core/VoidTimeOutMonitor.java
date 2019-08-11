/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.comserver.OfflineComServer;

/**
 * Provides an implementation for the {@link TimeOutMonitor} interface
 * that does absolutely nothing which is practical for
 * {@link OfflineComServer}
 * that do not need any monitoring.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-03 (14:35)
 */
public class VoidTimeOutMonitor implements TimeOutMonitor {

    private ServerProcessStatus status = ServerProcessStatus.STARTING;

    @Override
    public ServerProcessStatus getStatus () {
        return this.status;
    }

    @Override
    public void start () {
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
        this.status = ServerProcessStatus.SHUTDOWN;
    }

}