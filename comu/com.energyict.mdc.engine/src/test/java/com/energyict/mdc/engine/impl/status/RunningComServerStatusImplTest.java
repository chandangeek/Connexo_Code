/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.status;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.monitor.ComServerMonitor;
import com.energyict.mdc.engine.monitor.ComServerOperationalStatistics;
import com.energyict.mdc.engine.monitor.ScheduledComPortMonitor;
import com.energyict.mdc.engine.monitor.ScheduledComPortOperationalStatistics;
import com.energyict.mdc.engine.status.ComServerType;

import org.joda.time.DateTimeConstants;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link RunningComServerStatusImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (16:47)
 */
@RunWith(MockitoJUnitRunner.class)
public class RunningComServerStatusImplTest {

    private static final String COMSERVER_NAME = "RunningComServerStatusImplTest";

    @Mock
    private ComServer comServer;
    @Mock
    private ComServerMonitor comServerMonitor;

    @Before
    public void setupComServer () {
        when(this.comServer.getName()).thenReturn(COMSERVER_NAME);
    }

    @Before
    public void setupComServerAsOnline () {
        when(this.comServer.isOnline()).thenReturn(true);
        when(this.comServer.isOffline()).thenReturn(false);
        when(this.comServer.isRemote()).thenReturn(false);
    }

    @Test
    public void testOnlineComServerIsOfTypeOnline () {
        when(this.comServer.isOnline()).thenReturn(true);
        when(this.comServer.isRemote()).thenReturn(false);
        when(this.comServer.isOffline()).thenReturn(false);
        RunningComServerStatusImpl comServerStatus = new RunningComServerStatusImpl(Clock.systemDefaultZone(), this.comServer, this.comServerMonitor, Collections.emptyList(), Collections.emptyList());

        // Business method
        ComServerType comServerType = comServerStatus.getComServerType();

        // Asserts
        assertThat(comServerType).isEqualTo(ComServerType.ONLINE);
    }

    @Test
    public void testRemoteComServerIsOfTypeRemote () {
        when(this.comServer.isRemote()).thenReturn(true);
        when(this.comServer.isOnline()).thenReturn(false);
        when(this.comServer.isOffline()).thenReturn(false);
        RunningComServerStatusImpl comServerStatus = new RunningComServerStatusImpl(Clock.systemDefaultZone(), this.comServer, this.comServerMonitor, Collections.emptyList(), Collections.emptyList());

        // Business method
        ComServerType comServerType = comServerStatus.getComServerType();

        // Asserts
        assertThat(comServerType).isEqualTo(ComServerType.REMOTE);
    }

    @Test
    public void testMobileComServerIsOfTypeMobile () {
        when(this.comServer.isOffline()).thenReturn(true);
        when(this.comServer.isRemote()).thenReturn(false);
        when(this.comServer.isOnline()).thenReturn(false);
        RunningComServerStatusImpl comServerStatus = new RunningComServerStatusImpl(Clock.systemDefaultZone(), this.comServer, this.comServerMonitor, Collections.emptyList(), Collections.emptyList());

        // Business method
        ComServerType comServerType = comServerStatus.getComServerType();

        // Asserts
        assertThat(comServerType).isEqualTo(ComServerType.MOBILE);
    }

    @Test
    public void testComServerName () {
        RunningComServerStatusImpl comServerStatus = new RunningComServerStatusImpl(Clock.systemDefaultZone(), this.comServer, this.comServerMonitor, Collections.emptyList(), Collections.emptyList());

        // Business method
        String comServerName = comServerStatus.getComServerName();

        // Asserts
        assertThat(comServerName).isEqualTo(COMSERVER_NAME);
    }

    @Test
    public void testIsRunning () {
        RunningComServerStatusImpl comServerStatus = new RunningComServerStatusImpl(Clock.systemDefaultZone(), this.comServer, this.comServerMonitor, Collections.emptyList(), Collections.emptyList());

        // Business method & asserts
        assertThat(comServerStatus.isRunning()).isTrue();
    }

    @Test
    public void testComServerWithoutComPortsIsNotBlockedWhenLastCheckedForChangesWithinChangesInterpollDelay () {
        Clock clock = mock(Clock.class);
        Instant now = Instant.ofEpochMilli(DateTimeConstants.MILLIS_PER_HOUR);
        when(clock.instant()).thenReturn(now);
        ComServerOperationalStatistics operationalStatistics = mock(ComServerOperationalStatistics.class);
        when(operationalStatistics.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(operationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(Optional.of(Date.from(now.minus(Duration.ofMinutes(1)))));
        when(this.comServerMonitor.getOperationalStatistics()).thenReturn(operationalStatistics);
        RunningComServerStatusImpl comServerStatus = new RunningComServerStatusImpl(clock, this.comServer, this.comServerMonitor, Collections.emptyList(), Collections.emptyList());

        // Business method & asserts
        assertThat(comServerStatus.isBlocked()).isFalse();
        assertThat(comServerStatus.getBlockTime()).isNull();
    }

    @Test
    public void testComServerWithoutComPortsIsNotBlockedWhenNotCheckedForChangesYetButStartedWithinChangesInterpollDelay () {
        Clock clock = mock(Clock.class);
        Instant now = Instant.ofEpochMilli(DateTimeConstants.MILLIS_PER_HOUR);
        when(clock.instant()).thenReturn(now);
        ComServerOperationalStatistics operationalStatistics = mock(ComServerOperationalStatistics.class);
        when(operationalStatistics.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(operationalStatistics.getStartTimestamp()).thenReturn(Date.from(now.minus(Duration.ofMinutes(1))));
        when(operationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(Optional.<Date>empty());
        when(this.comServerMonitor.getOperationalStatistics()).thenReturn(operationalStatistics);
        RunningComServerStatusImpl comServerStatus = new RunningComServerStatusImpl(clock, this.comServer, this.comServerMonitor, Collections.emptyList(), Collections.emptyList());

        // Business method & asserts
        assertThat(comServerStatus.isBlocked()).isFalse();
        assertThat(comServerStatus.getBlockTime()).isNull();
    }

    @Test
    public void testComServerWithoutComPortsIsBlockedWhenLastCheckedForChangesOutsideChangesInterpollDelay () {
        Clock clock = mock(Clock.class);
        Instant now = Instant.ofEpochMilli(DateTimeConstants.MILLIS_PER_HOUR);
        when(clock.instant()).thenReturn(now);
        ComServerOperationalStatistics operationalStatistics = mock(ComServerOperationalStatistics.class);
        when(operationalStatistics.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(operationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(Optional.of(Date.from(now.minus(Duration.ofMinutes(10)))));
        when(this.comServerMonitor.getOperationalStatistics()).thenReturn(operationalStatistics);
        RunningComServerStatusImpl comServerStatus = new RunningComServerStatusImpl(clock, this.comServer, this.comServerMonitor, Collections.emptyList(), Collections.emptyList());

        // Business method & asserts
        assertThat(comServerStatus.isBlocked()).isTrue();
    }

    @Test
    public void testComServerWithoutComPortsIsBlockedWhenNotCheckedForChangesYetAndStartedOutsideChangesInterpollDelay () {
        Clock clock = mock(Clock.class);
        Instant now = Instant.ofEpochMilli(DateTimeConstants.MILLIS_PER_HOUR);
        when(clock.instant()).thenReturn(now);
        ComServerOperationalStatistics operationalStatistics = mock(ComServerOperationalStatistics.class);
        when(operationalStatistics.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(operationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(Optional.<Date>empty());
        when(operationalStatistics.getStartTimestamp()).thenReturn(Date.from(now.minus(Duration.ofMinutes(10))));
        when(this.comServerMonitor.getOperationalStatistics()).thenReturn(operationalStatistics);
        RunningComServerStatusImpl comServerStatus = new RunningComServerStatusImpl(clock, this.comServer, this.comServerMonitor, Collections.emptyList(), Collections.emptyList());

        // Business method & asserts
        assertThat(comServerStatus.isBlocked()).isTrue();
    }

    @Test
    public void testBlockTimeForComServerWithoutComPortsWhenLastCheckedForChangesOutsideChangesInterpollDelay () {
        Clock clock = mock(Clock.class);
        Instant now = Instant.ofEpochMilli(DateTimeConstants.MILLIS_PER_HOUR);
        when(clock.instant()).thenReturn(now);
        ComServerOperationalStatistics operationalStatistics = mock(ComServerOperationalStatistics.class);
        when(operationalStatistics.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(operationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(Optional.of(Date.from(now.minus(Duration.ofMinutes(15)))));
        when(this.comServerMonitor.getOperationalStatistics()).thenReturn(operationalStatistics);
        RunningComServerStatusImpl comServerStatus = new RunningComServerStatusImpl(clock, this.comServer, this.comServerMonitor, Collections.emptyList(), Collections.emptyList());

        // Business method & asserts
        assertThat(comServerStatus.getBlockTime()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    public void testBlockTimeForComServerWithoutComPortsThatHasNotCheckedForChangesAndStartedOutsideChangesInterpollDelay () {
        Clock clock = mock(Clock.class);
        Instant now = Instant.ofEpochMilli(DateTimeConstants.MILLIS_PER_HOUR);
        when(clock.instant()).thenReturn(now);
        ComServerOperationalStatistics operationalStatistics = mock(ComServerOperationalStatistics.class);
        when(operationalStatistics.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(operationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(Optional.<Date>empty());
        when(operationalStatistics.getStartTimestamp()).thenReturn(Date.from(now.minus(Duration.ofMinutes(15))));
        when(this.comServerMonitor.getOperationalStatistics()).thenReturn(operationalStatistics);
        RunningComServerStatusImpl comServerStatus = new RunningComServerStatusImpl(clock, this.comServer, this.comServerMonitor, Collections.emptyList(), Collections.emptyList());

        // Business method & asserts
        assertThat(comServerStatus.getBlockTime()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    public void testComServerWithComPortsIsNotBlocked () {
        Clock clock = mock(Clock.class);
        Instant now = Instant.ofEpochMilli(DateTimeConstants.MILLIS_PER_HOUR);
        when(clock.instant()).thenReturn(now);
        // Setup such that ComServer is not blocked
        ComServerOperationalStatistics operationalStatistics = mock(ComServerOperationalStatistics.class);
        when(operationalStatistics.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(operationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(Optional.of(Date.from(now.minus(Duration.ofMinutes(1)))));
        when(this.comServerMonitor.getOperationalStatistics()).thenReturn(operationalStatistics);

        ScheduledComPortMonitor nonBlockedComPortMonitor = mock(ScheduledComPortMonitor.class);
        ScheduledComPortOperationalStatistics nonBlockedOperationalStatistics = mock(ScheduledComPortOperationalStatistics.class);
        when(nonBlockedOperationalStatistics.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(nonBlockedOperationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(Optional.of(Date.from(now.minus(Duration.ofMinutes(2)))));
        when(nonBlockedOperationalStatistics.getLastCheckForWorkTimestamp()).thenReturn(Optional.of(Date.from(now.minus(Duration.ofMinutes(1)))));
        when(nonBlockedComPortMonitor.getOperationalStatistics()).thenReturn(nonBlockedOperationalStatistics);
        RunningComServerStatusImpl comServerStatus =
                new RunningComServerStatusImpl(
                        clock,
                        this.comServer,
                        this.comServerMonitor,
                        Arrays.asList(nonBlockedComPortMonitor),
                        Collections.emptyList());

        // Business method & asserts
        assertThat(comServerStatus.isBlocked()).isFalse();
        assertThat(comServerStatus.getBlockTime()).isNull();
    }

    @Test
    public void testComServerIsBlockAsSoonAsOneComPortIsBlocked () {
        Clock clock = mock(Clock.class);
        Instant now = Instant.ofEpochMilli(DateTimeConstants.MILLIS_PER_HOUR);
        when(clock.instant()).thenReturn(now);
        // Setup such that ComServer is not blocking
        ComServerOperationalStatistics operationalStatistics = mock(ComServerOperationalStatistics.class);
        when(operationalStatistics.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(operationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(Optional.of(Date.from(now.minus(Duration.ofMinutes(1)))));
        when(this.comServerMonitor.getOperationalStatistics()).thenReturn(operationalStatistics);

        ScheduledComPortMonitor blockedComPortMonitor = mock(ScheduledComPortMonitor.class);
        ScheduledComPortOperationalStatistics blockedOperationalStatistics = mock(ScheduledComPortOperationalStatistics.class);
        when(blockedOperationalStatistics.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(blockedOperationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(Optional.of(Date.from(now.minus(Duration.ofMinutes(15)))));
        when(blockedComPortMonitor.getOperationalStatistics()).thenReturn(blockedOperationalStatistics);
        ScheduledComPortMonitor nonBlockedComPortMonitor = mock(ScheduledComPortMonitor.class);
        ScheduledComPortOperationalStatistics nonBlockedOperationalStatistics = mock(ScheduledComPortOperationalStatistics.class);
        when(nonBlockedOperationalStatistics.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(nonBlockedOperationalStatistics.getLastCheckForWorkTimestamp()).thenReturn(Optional.empty());
        when(nonBlockedOperationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(Optional.of(Date.from(now.minus(Duration.ofMinutes(15)))));
        when(nonBlockedComPortMonitor.getOperationalStatistics()).thenReturn(nonBlockedOperationalStatistics);
        RunningComServerStatusImpl comServerStatus =
                new RunningComServerStatusImpl(
                        clock,
                        this.comServer,
                        this.comServerMonitor,
                        Arrays.asList(nonBlockedComPortMonitor, blockedComPortMonitor),
                        Collections.emptyList());

        // Business method & asserts
        assertThat(comServerStatus.isBlocked()).isTrue();
    }

    @Test
    public void testComServerIsBlockAsSoonAsOneComPortIsBlockedBecauseNotCheckedForChangesOrWorkAndStartedOutsideInterpollDelay () {
        Clock clock = mock(Clock.class);
        Instant now = Instant.ofEpochMilli(DateTimeConstants.MILLIS_PER_HOUR);
        when(clock.instant()).thenReturn(now);
        // Setup such that ComServer is not block
        ComServerOperationalStatistics operationalStatistics = mock(ComServerOperationalStatistics.class);
        when(operationalStatistics.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(operationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(Optional.of(Date.from(now.minus(Duration.ofMinutes(1)))));
        when(this.comServerMonitor.getOperationalStatistics()).thenReturn(operationalStatistics);

        ScheduledComPortMonitor blockedComPortMonitor = mock(ScheduledComPortMonitor.class);
        ScheduledComPortOperationalStatistics blockedOperationalStatistics = mock(ScheduledComPortOperationalStatistics.class);
        when(blockedOperationalStatistics.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(blockedOperationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(null);
        when(blockedOperationalStatistics.getLastCheckForWorkTimestamp()).thenReturn(null);
        when(blockedOperationalStatistics.getStartTimestamp()).thenReturn(Date.from(now.minus(Duration.ofMinutes(15))));
        when(blockedComPortMonitor.getOperationalStatistics()).thenReturn(blockedOperationalStatistics);
        ScheduledComPortMonitor nonBlockedComPortMonitor = mock(ScheduledComPortMonitor.class);
        ScheduledComPortOperationalStatistics nonBlockedOperationalStatistics = mock(ScheduledComPortOperationalStatistics.class);
        when(nonBlockedOperationalStatistics.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(nonBlockedOperationalStatistics.getLastCheckForWorkTimestamp()).thenReturn(Optional.empty());
        when(nonBlockedOperationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(Optional.of(Date.from(now.minus(Duration.ofMinutes(15)))));
        when(nonBlockedComPortMonitor.getOperationalStatistics()).thenReturn(nonBlockedOperationalStatistics);
        RunningComServerStatusImpl comServerStatus =
                new RunningComServerStatusImpl(
                        clock,
                        this.comServer,
                        this.comServerMonitor,
                        Arrays.asList(nonBlockedComPortMonitor, blockedComPortMonitor),
                        Collections.emptyList());

        // Business method & asserts
        assertThat(comServerStatus.isBlocked()).isTrue();
    }

    @Test
    public void testBlockTimeForComServerWithOneBlockedComPort () {
        Clock clock = mock(Clock.class);
        Instant now = Instant.ofEpochMilli(DateTimeConstants.MILLIS_PER_HOUR);
        when(clock.instant()).thenReturn(now);
        // Setup such that ComServer is not block
        ComServerOperationalStatistics operationalStatistics = mock(ComServerOperationalStatistics.class);
        when(operationalStatistics.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(operationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(Optional.of(Date.from(now.minus(Duration.ofMinutes(1)))));
        when(this.comServerMonitor.getOperationalStatistics()).thenReturn(operationalStatistics);

        ScheduledComPortMonitor blockedComPortMonitor = mock(ScheduledComPortMonitor.class);
        ScheduledComPortOperationalStatistics blockedOperationalStatistics = mock(ScheduledComPortOperationalStatistics.class);
        when(blockedOperationalStatistics.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(blockedOperationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(Optional.of(Date.from(now.minus(Duration.ofMinutes(15)))));
        when(blockedComPortMonitor.getOperationalStatistics()).thenReturn(blockedOperationalStatistics);
        ScheduledComPortMonitor nonBlockedComPortMonitor = mock(ScheduledComPortMonitor.class);
        ScheduledComPortOperationalStatistics nonBlockedOperationalStatistics = mock(ScheduledComPortOperationalStatistics.class);
        when(nonBlockedOperationalStatistics.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(nonBlockedOperationalStatistics.getLastCheckForWorkTimestamp()).thenReturn(Optional.empty());
        when(nonBlockedOperationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(Optional.of(Date.from(now.minus(Duration.ofMinutes(15)))));
        when(nonBlockedComPortMonitor.getOperationalStatistics()).thenReturn(nonBlockedOperationalStatistics);
        RunningComServerStatusImpl comServerStatus =
                new RunningComServerStatusImpl(
                        clock,
                        this.comServer,
                        this.comServerMonitor,
                        Arrays.asList(nonBlockedComPortMonitor, blockedComPortMonitor),
                        Collections.emptyList());

        // Business method & asserts
        assertThat(comServerStatus.getBlockTime()).isEqualTo(Duration.ofMinutes(10));
    }

}