/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.connection;

import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;

import org.joda.time.DateTime;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.events.connection.UndiscoveredEstablishConnectionEvent} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-07 (14:04)
 */
@RunWith(MockitoJUnitRunner.class)
public class UndiscoveredEstablishConnectionEventTest {

    private static final long DEVICE_ID = 1;
    private static final long COMPORT_ID = DEVICE_ID + 1;

    @Mock
    public Clock clock;
    @Mock
    private AbstractComServerEventImpl.ServiceProvider serviceProvider;

    @Before
    public void setupServiceProvider () {
        when(this.clock.instant()).thenReturn(new DateTime(1969, 5, 2, 1, 40, 0).toDate().toInstant()); // Set some default
        when(this.serviceProvider.clock()).thenReturn(this.clock);
    }

    @Test
    public void testCategory () {
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(this.serviceProvider, null);

        // Business method
        Category category = event.getCategory();

        // Asserts
        assertThat(category).isEqualTo(Category.CONNECTION);
    }

    @Test
    public void testOccurrenceTimestamp () {
        Clock frozenClock = Clock.fixed(new DateTime(2012, 11, 6, 13, 45, 17, 0).toDate().toInstant(), ZoneId.systemDefault());  // Random pick
        Instant now = frozenClock.instant();
        when(this.clock.instant()).thenReturn(now);

        InboundComPort comPort = mock(InboundComPort.class);
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(this.serviceProvider, comPort);

        // Business method
        Instant timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testIsEstablishd () {
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(this.serviceProvider, null);

        // Business method & asserts
        assertThat(event.isEstablishing()).isTrue();
    }

    @Test
    public void testIsNotClosed () {
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(this.serviceProvider, null);

        // Business method & asserts
        assertThat(event.isClosed()).isFalse();
    }

    @Test
    public void testIsNotFailure () {
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(this.serviceProvider, null);

        // Business method & asserts
        assertThat(event.isFailure()).isFalse();
        assertThat(event.getFailureMessage()).isNull();
    }

    @Test
    public void testIsNotLoggingRelated () {
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(this.serviceProvider, null);

        // Business method & asserts
        assertThat(event.isLoggingRelated()).isFalse();
    }

    @Test
    public void testIsNotComTaskRelated () {
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(this.serviceProvider, null);

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortRelated () {
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(this.serviceProvider, null);

        // Business method & asserts
        assertThat(event.isComPortRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortPoolRelated () {
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(this.serviceProvider, null);

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsNotDeviceRelatedWithoutComPort () {
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(this.serviceProvider, null);

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isFalse();
    }

    @Test
    public void testIsComPortRelated () {
        InboundComPort comPort = mock(InboundComPort.class);
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(this.serviceProvider, comPort);

        // Business method & asserts
        assertThat(event.isComPortRelated()).isTrue();
        assertThat(event.getComPort()).isEqualTo(comPort);
    }

    @Test
    public void testIsComPortPoolRelated () {
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.isInbound()).thenReturn(true);
        when(comPort.getComPortPool()).thenReturn(comPortPool);
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(this.serviceProvider, comPort);

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isTrue();
        assertThat(event.getComPortPool()).isEqualTo(comPortPool);
    }

    @Test
    public void testIsNotComTaskExecutionRelated () {
        InboundComPort comPort = mock(InboundComPort.class);
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(this.serviceProvider, comPort);

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isFalse();
    }

    @Test
    public void testIsNotConnectionTaskRelated () {
        InboundComPort comPort = mock(InboundComPort.class);
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(this.serviceProvider, comPort);

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isFalse();
        assertThat(event.getConnectionTask()).isNull();
    }

    @Test
    public void testIsNotConnectionTaskRelatedWithoutComPort () {
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(this.serviceProvider, null);

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isFalse();
    }

    @Test
    public void testIsNotDeviceRelated () {
        InboundComPort comPort = mock(InboundComPort.class);
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(this.serviceProvider, comPort);

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isFalse();
        assertThat(event.getDevice()).isNull();
    }

    @Test
    public void testToStringDoesNotFail () throws IOException {
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        UndiscoveredEstablishConnectionEvent event = new UndiscoveredEstablishConnectionEvent(this.serviceProvider, comPort);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}