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
 * for a {@link ComServer} that does not exist.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (13:10)
 */
public class UnknownComServerStatusImpl implements ComServerStatus {

    @Override
    public String getComServerName() {
        return "Not existing";
    }

    @Override
    public ComServerType getComServerType() {
        return ComServerType.NOT_APPLICABLE;
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
        return 0;
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
        return  Collections.emptyList();
    }
}