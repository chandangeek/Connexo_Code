/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.status;

import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.monitor.ComServerMonitor;
import com.energyict.mdc.engine.monitor.InboundComPortMonitor;
import com.energyict.mdc.engine.monitor.ScheduledComPortMonitor;
import com.energyict.mdc.engine.status.ComServerStatus;
import com.energyict.mdc.engine.status.ComServerType;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation for the {@link ComServerStatus} interface
 * for a {@link ComServer} that either does not exist or is not running.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (13:10)
 */
public class NotRunningComServerStatusImpl implements ComServerStatus {

    private final String comServerName;
    private final long id;
    private final ComServerType type;

    public NotRunningComServerStatusImpl(ComServer comServer) {
        super();
        this.comServerName = comServer.getName();
        this.id=comServer.getId();
        this.type = ComServerType.typeFor(comServer);
    }

    @Override
    public String getComServerName() {
        return this.comServerName;
    }

    @Override
    public ComServerType getComServerType() {
        return this.type;
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public boolean isBlocked() {
        return false;
    }

    @Override
    public Duration getBlockTime() {
        return null;
    }

    @Override
    public Instant getBlockTimestamp() {
        return null;
    }

    @Override
    public long getComServerId() {
        return this.id;
    }

    @Override
    public ComServerMonitor getComServerMonitor() {
        return null;
    }

    @Override
    public List<ScheduledComPortMonitor> getScheduledComportMonitors() {
        return Collections.emptyList();
    }

    @Override
    public List<InboundComPortMonitor> getInboundComportMonitors() {
        return Collections.emptyList();
    }
}