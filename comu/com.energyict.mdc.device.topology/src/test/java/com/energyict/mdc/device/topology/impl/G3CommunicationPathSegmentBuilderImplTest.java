package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link G3CommunicationPathSegmentBuilderImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-18 (11:46)
 */
@RunWith(MockitoJUnitRunner.class)
public class G3CommunicationPathSegmentBuilderImplTest {

    private static final int COST = 103;

    @Mock
    private Clock clock;
    @Mock
    private TopologyServiceImpl topologyService;
    @Mock
    private Device sourceDevice;

    @Test
    public void addDoesNotReallyAddAnything() {
        G3CommunicationPathSegmentBuilderImpl builder = this.testInstance();

        // Business methods
        builder.add(mock(Device.class), null, Duration.ofMinutes(1), COST);
        builder.add(mock(Device.class), null, Duration.ofMinutes(1), COST);

        // Asserts
        verifyZeroInteractions(this.topologyService);
    }

    @Test
    public void completeUseClockOnlyOnce() {
        G3CommunicationPathSegmentBuilderImpl builder = this.testInstance();
        builder.add(mock(Device.class), null, Duration.ofMinutes(1), COST);
        builder.add(mock(Device.class), null, Duration.ofMinutes(1), COST);

        // Business methods
        builder.complete();

        // Asserts
        verify(this.clock).instant();
    }

    @Test
    public void completeDelegatesToTopologyService() {
        Instant now = LocalDateTime.of(2014, 12, 18, 11, 56).toInstant(ZoneOffset.UTC);
        when(this.clock.instant()).thenReturn(now);
        G3CommunicationPathSegmentBuilderImpl builder = this.testInstance();
        Duration timeToLive = Duration.ofMinutes(1);
        builder.add(mock(Device.class), null, timeToLive, COST);
        builder.add(mock(Device.class), null, timeToLive, COST);

        // Business methods
        builder.complete();

        // Asserts
        verify(this.topologyService, times(2)).addCommunicationSegment(eq(now), eq(this.sourceDevice), any(Device.class), eq(null), eq(timeToLive), eq(COST));
    }

    private G3CommunicationPathSegmentBuilderImpl testInstance() {
        return new G3CommunicationPathSegmentBuilderImpl(this.topologyService, this.clock, this.sourceDevice);
    }

}