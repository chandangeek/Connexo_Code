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
        return this.isBlocked(operationalStatistics.getLastCheckForWorkTimestamp(), new Duration(operationalStatistics.getSchedulingInterPollDelay().getMilliSeconds()));
    }

    private boolean isBlocked(ComServerMonitor comServerMonitor) {
        ComServerOperationalStatistics operationalStatistics = comServerMonitor.getOperationalStatistics();
        return this.isBlocked(operationalStatistics.getLastCheckForChangesTimestamp(), new Duration(operationalStatistics.getChangesInterPollDelay().getMilliSeconds()));
    }

    private boolean isBlocked(Date lastActivity, Duration lenientDuration) {
        Instant now = new Instant(this.clock.now());
        Instant latestExpectedActivity = now.minus(lenientDuration.getMillis());
        Instant lastCheckForChangesTimestamp = new Instant(lastActivity.getTime());
        return lastCheckForChangesTimestamp.isBefore(latestExpectedActivity);
    }

    @Override
    public Duration getBlockTime() {
    }

}