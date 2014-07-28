package com.energyict.mdc.engine.impl.status;

import com.energyict.mdc.engine.impl.monitor.ComServerMonitor;
import com.energyict.mdc.engine.impl.monitor.ComServerOperationalStatistics;
import com.energyict.mdc.engine.impl.monitor.ScheduledComPortMonitor;
import com.energyict.mdc.engine.impl.monitor.ScheduledComPortOperationalStatistics;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.status.ComServerStatus;
import com.energyict.mdc.engine.status.ComServerType;

import com.elster.jupiter.util.time.Clock;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.Interval;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Provides an implementation for the {@link ComServerStatus}
 * of an actual running {@link com.energyict.mdc.engine.model.ComServer}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (13:08)
 */
public class RunningComServerStatusImpl implements ComServerStatus {

    private final Clock clock;
    private final String comServerName;
    private final ComServerType comServerType;
    private final ComServerMonitor monitor;
    private final List<ScheduledComPortMonitor> comPortMonitors;

    public RunningComServerStatusImpl(Clock clock, ComServer comServer, ComServerMonitor monitor, List<ScheduledComPortMonitor> comPortMonitors) {
        super();
        this.clock = clock;
        this.comServerName = comServer.getName();
        this.comServerType = ComServerType.typeFor(comServer);
        this.monitor = monitor;
        this.comPortMonitors = Collections.unmodifiableList(comPortMonitors);
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
    public boolean isBlocked() {
        for (ScheduledComPortMonitor comPortMonitor : this.comPortMonitors) {
            if (this.isBlocked(comPortMonitor)) {
                return true;
            }
        }
        return this.isBlocked(this.monitor);
    }

    private boolean isBlocked(ScheduledComPortMonitor comPortMonitor) {
        ScheduledComPortOperationalStatistics operationalStatistics = comPortMonitor.getOperationalStatistics();
        return this.isBlocked(this.lastActivity(operationalStatistics), new Duration(operationalStatistics.getSchedulingInterPollDelay().getMilliSeconds()));
    }

    private Date lastActivity(ScheduledComPortOperationalStatistics operationalStatistics) {
        Date lastCheckForWorkTimestamp = operationalStatistics.getLastCheckForWorkTimestamp();
        Date lastActivity;
        if (lastCheckForWorkTimestamp != null) {
            lastActivity = lastCheckForWorkTimestamp;
        }
        else if (operationalStatistics.getLastCheckForChangesTimestamp() != null) {
            lastActivity = operationalStatistics.getLastCheckForChangesTimestamp();
        }
        else {
            // Never checked for work or changes: consider comport start time to be last activity
            lastActivity = operationalStatistics.getStartTimestamp();
        }
        return lastActivity;
    }

    private boolean isBlocked(ComServerMonitor comServerMonitor) {
        ComServerOperationalStatistics operationalStatistics = comServerMonitor.getOperationalStatistics();
        return this.isBlocked(this.lastActivity(operationalStatistics), new Duration(operationalStatistics.getChangesInterPollDelay().getMilliSeconds()));
    }

    private Date lastActivity(ComServerOperationalStatistics operationalStatistics) {
        Date lastCheckForChangesTimestamp = operationalStatistics.getLastCheckForChangesTimestamp();
        Date lastActivity;
        if (lastCheckForChangesTimestamp != null) {
            lastActivity = lastCheckForChangesTimestamp;
        }
        else {
            // Never checked for changes: consider server start time to be last activity
            lastActivity = operationalStatistics.getStartTimestamp();
        }
        return lastActivity;
    }

    private boolean isBlocked(Date lastActivity, Duration lenientDuration) {
        Instant now = new Instant(this.clock.now());
        Instant latestExpectedActivity = now.minus(lenientDuration.getMillis());
        Instant lastActualActivity = new Instant(lastActivity.getTime());
        return lastActualActivity.isBefore(latestExpectedActivity);
    }

    @Override
    public Duration getBlockTime() {
        for (ScheduledComPortMonitor comPortMonitor : this.comPortMonitors) {
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
        return this.getBlockTime(this.lastActivity(operationalStatistics), new Duration(operationalStatistics.getSchedulingInterPollDelay().getMilliSeconds()));
    }

    private Duration getBlockTime(ComServerMonitor comServerMonitor) {
        ComServerOperationalStatistics operationalStatistics = comServerMonitor.getOperationalStatistics();
        return this.getBlockTime(this.lastActivity(operationalStatistics), new Duration(operationalStatistics.getChangesInterPollDelay().getMilliSeconds()));
    }

    private Duration getBlockTime(Date lastActivity, Duration lenientDuration) {
        Instant now = new Instant(this.clock.now());
        return new Interval(new Instant(lastActivity.getTime()).plus(lenientDuration), now).toDuration();
    }

}