package com.energyict.mdc.engine.impl.status;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.impl.monitor.ComServerMonitor;
import com.energyict.mdc.engine.impl.monitor.ComServerOperationalStatistics;
import com.energyict.mdc.engine.impl.monitor.ScheduledComPortMonitor;
import com.energyict.mdc.engine.impl.monitor.ScheduledComPortOperationalStatistics;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.status.ComServerType;

import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.impl.DefaultClock;
import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;
import org.joda.time.Instant;

import java.util.Arrays;
import java.util.Collections;

import org.junit.*;
import org.junit.runner.*;
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
        RunningComServerStatusImpl comServerStatus = new RunningComServerStatusImpl(new DefaultClock(), this.comServer, this.comServerMonitor, Collections.<ScheduledComPortMonitor>emptyList());

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
        RunningComServerStatusImpl comServerStatus = new RunningComServerStatusImpl(new DefaultClock(), this.comServer, this.comServerMonitor, Collections.<ScheduledComPortMonitor>emptyList());

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
        RunningComServerStatusImpl comServerStatus = new RunningComServerStatusImpl(new DefaultClock(), this.comServer, this.comServerMonitor, Collections.<ScheduledComPortMonitor>emptyList());

        // Business method
        ComServerType comServerType = comServerStatus.getComServerType();

        // Asserts
        assertThat(comServerType).isEqualTo(ComServerType.MOBILE);
    }

    @Test
    public void testComServerName () {
        RunningComServerStatusImpl comServerStatus = new RunningComServerStatusImpl(new DefaultClock(), this.comServer, this.comServerMonitor, Collections.<ScheduledComPortMonitor>emptyList());

        // Business method
        String comServerName = comServerStatus.getComServerName();

        // Asserts
        assertThat(comServerName).isEqualTo(COMSERVER_NAME);
    }

    @Test
    public void testIsRunning () {
        RunningComServerStatusImpl comServerStatus = new RunningComServerStatusImpl(new DefaultClock(), this.comServer, this.comServerMonitor, Collections.<ScheduledComPortMonitor>emptyList());

        // Business method & asserts
        assertThat(comServerStatus.isRunning()).isTrue();
    }

    @Test
    public void testComServerWithoutComPortsIsNotBlockedWhenLastCheckedForChangesWithinChangesInterpollDelay () {
        Clock clock = mock(Clock.class);
        Instant now = new Instant(DateTimeConstants.MILLIS_PER_HOUR);
        when(clock.now()).thenReturn(now.toDate());
        ComServerOperationalStatistics operationalStatistics = mock(ComServerOperationalStatistics.class);
        when(operationalStatistics.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(operationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(now.minus(Duration.standardMinutes(1)).toDate());
        when(this.comServerMonitor.getOperationalStatistics()).thenReturn(operationalStatistics);
        RunningComServerStatusImpl comServerStatus = new RunningComServerStatusImpl(clock, this.comServer, this.comServerMonitor, Collections.<ScheduledComPortMonitor>emptyList());

        // Business method & asserts
        assertThat(comServerStatus.isBlocked()).isFalse();
        assertThat(comServerStatus.getBlockTime()).isNull();
    }

    @Test
    public void testComServerWithoutComPortsIsNotBlockedWhenNotCheckedForChangesYetButStartedWithinChangesInterpollDelay () {
        Clock clock = mock(Clock.class);
        Instant now = new Instant(DateTimeConstants.MILLIS_PER_HOUR);
        when(clock.now()).thenReturn(now.toDate());
        ComServerOperationalStatistics operationalStatistics = mock(ComServerOperationalStatistics.class);
        when(operationalStatistics.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(operationalStatistics.getStartTimestamp()).thenReturn(now.minus(Duration.standardMinutes(1)).toDate());
        when(operationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(null);
        when(this.comServerMonitor.getOperationalStatistics()).thenReturn(operationalStatistics);
        RunningComServerStatusImpl comServerStatus = new RunningComServerStatusImpl(clock, this.comServer, this.comServerMonitor, Collections.<ScheduledComPortMonitor>emptyList());

        // Business method & asserts
        assertThat(comServerStatus.isBlocked()).isFalse();
        assertThat(comServerStatus.getBlockTime()).isNull();
    }

    @Test
    public void testComServerWithoutComPortsIsBlockedWhenLastCheckedForChangesOutsideChangesInterpollDelay () {
        Clock clock = mock(Clock.class);
        Instant now = new Instant(DateTimeConstants.MILLIS_PER_HOUR);
        when(clock.now()).thenReturn(now.toDate());
        ComServerOperationalStatistics operationalStatistics = mock(ComServerOperationalStatistics.class);
        when(operationalStatistics.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(operationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(now.minus(Duration.standardMinutes(10)).toDate());
        when(this.comServerMonitor.getOperationalStatistics()).thenReturn(operationalStatistics);
        RunningComServerStatusImpl comServerStatus = new RunningComServerStatusImpl(clock, this.comServer, this.comServerMonitor, Collections.<ScheduledComPortMonitor>emptyList());

        // Business method & asserts
        assertThat(comServerStatus.isBlocked()).isTrue();
    }

    @Test
    public void testComServerWithoutComPortsIsBlockedWhenNotCheckedForChangesYetAndStartedOutsideChangesInterpollDelay () {
        Clock clock = mock(Clock.class);
        Instant now = new Instant(DateTimeConstants.MILLIS_PER_HOUR);
        when(clock.now()).thenReturn(now.toDate());
        ComServerOperationalStatistics operationalStatistics = mock(ComServerOperationalStatistics.class);
        when(operationalStatistics.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(operationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(null);
        when(operationalStatistics.getStartTimestamp()).thenReturn(now.minus(Duration.standardMinutes(10)).toDate());
        when(this.comServerMonitor.getOperationalStatistics()).thenReturn(operationalStatistics);
        RunningComServerStatusImpl comServerStatus = new RunningComServerStatusImpl(clock, this.comServer, this.comServerMonitor, Collections.<ScheduledComPortMonitor>emptyList());

        // Business method & asserts
        assertThat(comServerStatus.isBlocked()).isTrue();
    }

    @Test
    public void testBlockTimeForComServerWithoutComPortsWhenLastCheckedForChangesOutsideChangesInterpollDelay () {
        Clock clock = mock(Clock.class);
        Instant now = new Instant(DateTimeConstants.MILLIS_PER_HOUR);
        when(clock.now()).thenReturn(now.toDate());
        ComServerOperationalStatistics operationalStatistics = mock(ComServerOperationalStatistics.class);
        when(operationalStatistics.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(operationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(now.minus(Duration.standardMinutes(15)).toDate());
        when(this.comServerMonitor.getOperationalStatistics()).thenReturn(operationalStatistics);
        RunningComServerStatusImpl comServerStatus = new RunningComServerStatusImpl(clock, this.comServer, this.comServerMonitor, Collections.<ScheduledComPortMonitor>emptyList());

        // Business method & asserts
        assertThat(comServerStatus.getBlockTime()).isEqualTo(Duration.standardMinutes(10));
    }

    @Test
    public void testBlockTimeForComServerWithoutComPortsThatHasNotCheckedForChangesAndStartedOutsideChangesInterpollDelay () {
        Clock clock = mock(Clock.class);
        Instant now = new Instant(DateTimeConstants.MILLIS_PER_HOUR);
        when(clock.now()).thenReturn(now.toDate());
        ComServerOperationalStatistics operationalStatistics = mock(ComServerOperationalStatistics.class);
        when(operationalStatistics.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(operationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(null);
        when(operationalStatistics.getStartTimestamp()).thenReturn(now.minus(Duration.standardMinutes(15)).toDate());
        when(this.comServerMonitor.getOperationalStatistics()).thenReturn(operationalStatistics);
        RunningComServerStatusImpl comServerStatus = new RunningComServerStatusImpl(clock, this.comServer, this.comServerMonitor, Collections.<ScheduledComPortMonitor>emptyList());

        // Business method & asserts
        assertThat(comServerStatus.getBlockTime()).isEqualTo(Duration.standardMinutes(10));
    }

    @Test
    public void testComServerWithComPortsIsNotBlocked () {
        Clock clock = mock(Clock.class);
        Instant now = new Instant(DateTimeConstants.MILLIS_PER_HOUR);
        when(clock.now()).thenReturn(now.toDate());
        // Setup such that ComServer is not blocked
        ComServerOperationalStatistics operationalStatistics = mock(ComServerOperationalStatistics.class);
        when(operationalStatistics.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(operationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(now.minus(Duration.standardMinutes(1)).toDate());
        when(this.comServerMonitor.getOperationalStatistics()).thenReturn(operationalStatistics);

        ScheduledComPortMonitor nonBlockedComPortMonitor = mock(ScheduledComPortMonitor.class);
        ScheduledComPortOperationalStatistics nonBlockedOperationalStatistics = mock(ScheduledComPortOperationalStatistics.class);
        when(nonBlockedOperationalStatistics.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(nonBlockedOperationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(now.minus(Duration.standardMinutes(2)).toDate());
        when(nonBlockedOperationalStatistics.getLastCheckForWorkTimestamp()).thenReturn(now.minus(Duration.standardMinutes(1)).toDate());
        when(nonBlockedComPortMonitor.getOperationalStatistics()).thenReturn(nonBlockedOperationalStatistics);
        RunningComServerStatusImpl comServerStatus =
                new RunningComServerStatusImpl(
                        clock,
                        this.comServer,
                        this.comServerMonitor,
                        Arrays.asList(nonBlockedComPortMonitor));

        // Business method & asserts
        assertThat(comServerStatus.isBlocked()).isFalse();
        assertThat(comServerStatus.getBlockTime()).isNull();
    }

    @Test
    public void testComServerIsBlockAsSoonAsOneComPortIsBlocked () {
        Clock clock = mock(Clock.class);
        Instant now = new Instant(DateTimeConstants.MILLIS_PER_HOUR);
        when(clock.now()).thenReturn(now.toDate());
        // Setup such that ComServer is not block
        ComServerOperationalStatistics operationalStatistics = mock(ComServerOperationalStatistics.class);
        when(operationalStatistics.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(operationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(now.minus(Duration.standardMinutes(1)).toDate());
        when(this.comServerMonitor.getOperationalStatistics()).thenReturn(operationalStatistics);

        ScheduledComPortMonitor blockedComPortMonitor = mock(ScheduledComPortMonitor.class);
        ScheduledComPortOperationalStatistics blockedOperationalStatistics = mock(ScheduledComPortOperationalStatistics.class);
        when(blockedOperationalStatistics.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(blockedOperationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(now.minus(Duration.standardMinutes(15)).toDate());
        when(blockedComPortMonitor.getOperationalStatistics()).thenReturn(blockedOperationalStatistics);
        ScheduledComPortMonitor nonBlockedComPortMonitor = mock(ScheduledComPortMonitor.class);
        ScheduledComPortOperationalStatistics nonBlockedOperationalStatistics = mock(ScheduledComPortOperationalStatistics.class);
        when(nonBlockedOperationalStatistics.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(nonBlockedOperationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(now.minus(Duration.standardMinutes(15)).toDate());
        when(nonBlockedComPortMonitor.getOperationalStatistics()).thenReturn(nonBlockedOperationalStatistics);
        RunningComServerStatusImpl comServerStatus =
                new RunningComServerStatusImpl(
                        clock,
                        this.comServer,
                        this.comServerMonitor,
                        Arrays.asList(nonBlockedComPortMonitor, blockedComPortMonitor));

        // Business method & asserts
        assertThat(comServerStatus.isBlocked()).isTrue();
    }

    @Test
    public void testComServerIsBlockAsSoonAsOneComPortIsBlockedBecauseNotCheckedForChangesOrWorkAndStartedOutsideInterpollDelay () {
        Clock clock = mock(Clock.class);
        Instant now = new Instant(DateTimeConstants.MILLIS_PER_HOUR);
        when(clock.now()).thenReturn(now.toDate());
        // Setup such that ComServer is not block
        ComServerOperationalStatistics operationalStatistics = mock(ComServerOperationalStatistics.class);
        when(operationalStatistics.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(operationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(now.minus(Duration.standardMinutes(1)).toDate());
        when(this.comServerMonitor.getOperationalStatistics()).thenReturn(operationalStatistics);

        ScheduledComPortMonitor blockedComPortMonitor = mock(ScheduledComPortMonitor.class);
        ScheduledComPortOperationalStatistics blockedOperationalStatistics = mock(ScheduledComPortOperationalStatistics.class);
        when(blockedOperationalStatistics.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(blockedOperationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(null);
        when(blockedOperationalStatistics.getLastCheckForWorkTimestamp()).thenReturn(null);
        when(blockedOperationalStatistics.getStartTimestamp()).thenReturn(now.minus(Duration.standardMinutes(15)).toDate());
        when(blockedComPortMonitor.getOperationalStatistics()).thenReturn(blockedOperationalStatistics);
        ScheduledComPortMonitor nonBlockedComPortMonitor = mock(ScheduledComPortMonitor.class);
        ScheduledComPortOperationalStatistics nonBlockedOperationalStatistics = mock(ScheduledComPortOperationalStatistics.class);
        when(nonBlockedOperationalStatistics.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(nonBlockedOperationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(now.minus(Duration.standardMinutes(15)).toDate());
        when(nonBlockedComPortMonitor.getOperationalStatistics()).thenReturn(nonBlockedOperationalStatistics);
        RunningComServerStatusImpl comServerStatus =
                new RunningComServerStatusImpl(
                        clock,
                        this.comServer,
                        this.comServerMonitor,
                        Arrays.asList(nonBlockedComPortMonitor, blockedComPortMonitor));

        // Business method & asserts
        assertThat(comServerStatus.isBlocked()).isTrue();
    }

    @Test
    public void testBlockTimeForComServerWithOneBlockedComPort () {
        Clock clock = mock(Clock.class);
        Instant now = new Instant(DateTimeConstants.MILLIS_PER_HOUR);
        when(clock.now()).thenReturn(now.toDate());
        // Setup such that ComServer is not block
        ComServerOperationalStatistics operationalStatistics = mock(ComServerOperationalStatistics.class);
        when(operationalStatistics.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(operationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(now.minus(Duration.standardMinutes(1)).toDate());
        when(this.comServerMonitor.getOperationalStatistics()).thenReturn(operationalStatistics);

        ScheduledComPortMonitor blockedComPortMonitor = mock(ScheduledComPortMonitor.class);
        ScheduledComPortOperationalStatistics blockedOperationalStatistics = mock(ScheduledComPortOperationalStatistics.class);
        when(blockedOperationalStatistics.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(blockedOperationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(now.minus(Duration.standardMinutes(15)).toDate());
        when(blockedComPortMonitor.getOperationalStatistics()).thenReturn(blockedOperationalStatistics);
        ScheduledComPortMonitor nonBlockedComPortMonitor = mock(ScheduledComPortMonitor.class);
        ScheduledComPortOperationalStatistics nonBlockedOperationalStatistics = mock(ScheduledComPortOperationalStatistics.class);
        when(nonBlockedOperationalStatistics.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(5));
        when(nonBlockedOperationalStatistics.getLastCheckForChangesTimestamp()).thenReturn(now.minus(Duration.standardMinutes(15)).toDate());
        when(nonBlockedComPortMonitor.getOperationalStatistics()).thenReturn(nonBlockedOperationalStatistics);
        RunningComServerStatusImpl comServerStatus =
                new RunningComServerStatusImpl(
                        clock,
                        this.comServer,
                        this.comServerMonitor,
                        Arrays.asList(nonBlockedComPortMonitor, blockedComPortMonitor));

        // Business method & asserts
        assertThat(comServerStatus.getBlockTime()).isEqualTo(Duration.standardMinutes(10));
    }

}