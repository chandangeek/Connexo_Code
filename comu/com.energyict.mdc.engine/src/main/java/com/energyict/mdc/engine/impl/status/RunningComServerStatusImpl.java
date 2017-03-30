/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.status;

import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.monitor.ComServerMonitor;
import com.energyict.mdc.engine.monitor.ComServerOperationalStatistics;
import com.energyict.mdc.engine.monitor.InboundComPortMonitor;
import com.energyict.mdc.engine.monitor.ScheduledComPortMonitor;
import com.energyict.mdc.engine.monitor.ScheduledComPortOperationalStatistics;
import com.energyict.mdc.engine.status.ComServerStatus;
import com.energyict.mdc.engine.status.ComServerType;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation for the {@link ComServerStatus}
 * of an actual running {@link com.energyict.mdc.engine.config.ComServer}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (13:08)
 */
public class RunningComServerStatusImpl implements ComServerStatus {

    private final Clock clock;
    private final String comServerName;
    private final long id;
    private final ComServerType comServerType;
    private final ComServerMonitor monitor;
    private final List<ScheduledComPortMonitor> scheduledComPortMonitors;
    private final List<InboundComPortMonitor> inboundComPortMonitors;

    public RunningComServerStatusImpl(Clock clock, ComServer comServer, ComServerMonitor monitor, List<ScheduledComPortMonitor> comPortMonitors, List<InboundComPortMonitor> inboundComportMonitors) {
        super();
        this.clock = clock;
        this.comServerName = comServer.getName();
        this.id = comServer.getId();
        this.comServerType = ComServerType.typeFor(comServer);
        this.monitor = monitor;
        this.scheduledComPortMonitors = Collections.unmodifiableList(comPortMonitors);
        this.inboundComPortMonitors = Collections.unmodifiableList(inboundComportMonitors);
    }

    @Override
    public String getComServerName() {
        return this.comServerName;
    }

    @Override
    public ComServerType getComServerType() {
        return this.comServerType;
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public long getComServerId() {
        return this.id;
    }

    @Override
    public boolean isBlocked() {
        for (ScheduledComPortMonitor comPortMonitor : this.scheduledComPortMonitors) {
            if (this.isBlocked(comPortMonitor)) {
                return true;
            }
        }
        return this.isBlocked(this.monitor);
    }

    @Override
    public Instant getBlockTimestamp() {
        for (ScheduledComPortMonitor comPortMonitor : this.scheduledComPortMonitors) {
            if (this.isBlocked(comPortMonitor)) {
                return this.lastActivity(comPortMonitor.getOperationalStatistics());
            }
        }
        if (this.isBlocked(this.monitor)) {
            return this.lastActivity(this.monitor.getOperationalStatistics());
        }
        return null;
    }

    private boolean isBlocked(ScheduledComPortMonitor comPortMonitor) {
        ScheduledComPortOperationalStatistics operationalStatistics = comPortMonitor.getOperationalStatistics();
        return this.isBlocked(this.lastActivity(operationalStatistics), Duration.ofMillis(operationalStatistics.getSchedulingInterPollDelay().getMilliSeconds()));
    }

    private Instant lastActivity(ScheduledComPortOperationalStatistics operationalStatistics) {
        return operationalStatistics.getLastCheckForWorkTimestamp()
                    .orElseGet(() -> operationalStatistics.getLastCheckForChangesTimestamp()
                    .orElseGet(operationalStatistics::getStartTimestamp)).toInstant();
    }

    private boolean isBlocked(ComServerMonitor comServerMonitor) {
        ComServerOperationalStatistics operationalStatistics = comServerMonitor.getOperationalStatistics();
        return this.isBlocked(this.lastActivity(operationalStatistics), Duration.ofMillis(operationalStatistics.getChangesInterPollDelay().getMilliSeconds()));
    }

    private Instant lastActivity(ComServerOperationalStatistics operationalStatistics) {
        return operationalStatistics.getLastCheckForChangesTimestamp().orElseGet(operationalStatistics::getStartTimestamp).toInstant();
    }

    private boolean isBlocked(Instant lastActivity, Duration lenientDuration) {
        Instant now = this.clock.instant();
        Instant latestExpectedActivity = now.minusMillis(lenientDuration.toMillis());
        return lastActivity.isBefore(latestExpectedActivity);
    }

    @Override
    public Duration getBlockTime() {
        for (ScheduledComPortMonitor comPortMonitor : this.scheduledComPortMonitors) {
            if (this.isBlocked(comPortMonitor)) {
                return this.getBlockTime(comPortMonitor);
            }
        }
        if (this.isBlocked(this.monitor)) {
            return this.getBlockTime(this.monitor);
        }
        return null;
    }

    private Duration getBlockTime(ScheduledComPortMonitor comPortMonitor) {
        ScheduledComPortOperationalStatistics operationalStatistics = comPortMonitor.getOperationalStatistics();
        return this.getBlockTime(this.lastActivity(operationalStatistics), Duration.ofMillis(operationalStatistics.getSchedulingInterPollDelay().getMilliSeconds()));
    }

    private Duration getBlockTime(ComServerMonitor comServerMonitor) {
        ComServerOperationalStatistics operationalStatistics = comServerMonitor.getOperationalStatistics();
        return this.getBlockTime(this.lastActivity(operationalStatistics), Duration.ofMillis(operationalStatistics.getChangesInterPollDelay().getMilliSeconds()));
    }

    private Duration getBlockTime(Instant lastActivity, Duration lenientDuration) {
        return Duration.between(lastActivity.plusMillis(lenientDuration.toMillis()), this.clock.instant());
    }

    @Override
    public ComServerMonitor getComServerMonitor() {
        return monitor;
    }

    @Override
    public List<ScheduledComPortMonitor> getScheduledComportMonitors() {
        return scheduledComPortMonitors;
    }

    @Override
    public List<InboundComPortMonitor> getInboundComportMonitors() {
        return inboundComPortMonitors;
    }
}