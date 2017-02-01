/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.io;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;

import org.joda.time.DateTime;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.events.io.WriteEvent} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-13 (14:08)
 */
@RunWith(MockitoJUnitRunner.class)
public class WriteEventTest {

    private static final long COMPORT_ID = 1;

    @Mock
    public Clock clock;
    @Mock
    private AbstractComServerEventImpl.ServiceProvider serviceProvider;

    @Before
    public void setupServiceProvider () {
        when(this.clock.instant()).thenReturn(new DateTime(2014, 5, 2, 1, 40, 0).toDate().toInstant()); // Set some default
        when(this.serviceProvider.clock()).thenReturn(this.clock);
    }

    @Test
    public void testIsWrite () {
        WriteEvent event = new WriteEvent(this.serviceProvider, null, null);
        assertThat(event.isWrite()).isTrue();
    }

    @Test
    public void testIsNotRead () {
        WriteEvent event = new WriteEvent(this.serviceProvider, null, null);
        assertThat(event.isRead()).isFalse();
    }

    @Test
    public void testConstructor () {
        Instant occurrenceTimestamp = Instant.now();
        when(this.clock.instant()).thenReturn(occurrenceTimestamp);
        byte[] bytes = "testConstructor".getBytes();
        ComPort comPort = mock(ComPort.class);

        // Business method
        WriteEvent event = new WriteEvent(this.serviceProvider, comPort, bytes);

        // Asserts
        assertThat(event.getOccurrenceTimestamp()).isEqualTo(occurrenceTimestamp);
        assertThat(event.getBytes()).isEqualTo(bytes);
    }

    @Test
    public void testGetCategory () {
        WriteEvent event = new WriteEvent(this.serviceProvider, null, null);
        assertThat(event.getCategory()).isEqualTo(Category.CONNECTION);
    }

    @Test
    public void testIsNotEstablishing () {
        WriteEvent event = new WriteEvent(this.serviceProvider, null, null);
        assertThat(event.isEstablishing()).isFalse();
    }

    @Test
    public void testIsNotFailure () {
        WriteEvent event = new WriteEvent(this.serviceProvider, null, null);
        assertThat(event.isFailure()).isFalse();
    }

    @Test
    public void testIsNotClosed () {
        WriteEvent event = new WriteEvent(this.serviceProvider, null, null);
        assertThat(event.isClosed()).isFalse();
    }

    @Test
    public void testIsNotLoggingRelated () {
        WriteEvent event = new WriteEvent(this.serviceProvider, null, null);

        // Business method & asserts
        assertThat(event.isLoggingRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortRelated () {
        WriteEvent event = new WriteEvent(this.serviceProvider, null, null);
        assertThat(event.isComPortRelated()).isFalse();
        assertThat(event.getComPort()).isNull();
    }

    @Test
    public void testIsComPortRelated () {
        ComPort comPort = mock(ComPort.class);
        when(comPort.isInbound()).thenReturn(false);
        WriteEvent event = new WriteEvent(this.serviceProvider, comPort, "testIsComPortRelated".getBytes());
        assertThat(event.isComPortRelated()).isTrue();
        assertThat(event.getComPort()).isEqualTo(comPort);
    }

    @Test
    public void testIsNotConnectionTaskRelated () {
        WriteEvent event = new WriteEvent(this.serviceProvider, null, null);
        assertThat(event.isConnectionTaskRelated()).isFalse();
        assertThat(event.getConnectionTask()).isNull();
    }

    @Test
    public void testIsNotDeviceRelated () {
        WriteEvent event = new WriteEvent(this.serviceProvider, null, null);
        assertThat(event.isDeviceRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortPoolRelated () {
        WriteEvent event = new WriteEvent(this.serviceProvider, null, null);
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsComPortPoolRelatedForOutboundComPort () {
        ComPort comPort = mock(ComPort.class);
        when(comPort.isInbound()).thenReturn(false);
        WriteEvent event = new WriteEvent(this.serviceProvider, comPort, "testIsComPortPoolRelatedForOutboundComPort".getBytes());
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsComPortPoolRelatedForInboundComPort () {
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.isInbound()).thenReturn(true);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(comPort.getComPortPool()).thenReturn(comPortPool);

        WriteEvent event = new WriteEvent(this.serviceProvider, comPort, "testIsComPortPoolRelatedForInboundComPort".getBytes());
        assertThat(event.isComPortPoolRelated()).isTrue();
        assertThat(event.getComPortPool()).isEqualTo(comPortPool);
    }

    @Test
    public void testIsNotComTaskExecutionRelated () {
        WriteEvent event = new WriteEvent(this.serviceProvider, null, null);
        assertThat(event.isComTaskExecutionRelated()).isFalse();
    }

    @Test
    public void testToStringDoesNotFail () throws IOException {
        byte[] bytes = "testSerializationDoesNotFail".getBytes();
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        WriteEvent event = new WriteEvent(this.serviceProvider, comPort, bytes);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}