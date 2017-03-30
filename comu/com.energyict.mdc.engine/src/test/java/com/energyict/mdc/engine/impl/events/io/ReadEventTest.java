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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.events.io.ReadEvent} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-12 (12:27)
 */
@RunWith(MockitoJUnitRunner.class)
public class ReadEventTest {

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
    public void testIsRead () {
        ReadEvent event = new ReadEvent(this.serviceProvider, null, null);
        assertThat(event.isRead()).isTrue();
    }

    @Test
    public void testIsNotWrite () {
        ReadEvent event = new ReadEvent(this.serviceProvider, null, null);
        assertThat(event.isWrite()).isFalse();
    }

    @Test
    public void testConstructor () {
        Instant occurrenceTimestamp = LocalDateTime.of(2012, Calendar.NOVEMBER, 7, 17, 22, 01, 0).toInstant(ZoneOffset.UTC);  // Random pick
        when(this.clock.instant()).thenReturn(occurrenceTimestamp);
        byte[] bytes = "testConstructor".getBytes();
        ComPort comPort = mock(ComPort.class);

        // Business method
        ReadEvent event = new ReadEvent(this.serviceProvider, comPort, bytes);

        // Asserts
        assertThat(event.getOccurrenceTimestamp()).isEqualTo(occurrenceTimestamp);
        assertThat(event.getBytes()).isEqualTo(bytes);
    }

    @Test
    public void testGetCategory () {
        ReadEvent event = new ReadEvent(this.serviceProvider, null, null);
        assertThat(event.getCategory()).isEqualTo(Category.CONNECTION);
    }

    @Test
    public void testIsNotEstablishing () {
        ReadEvent event = new ReadEvent(this.serviceProvider, null, null);
        assertThat(event.isEstablishing()).isFalse();
    }

    @Test
    public void testIsNotFailure () {
        ReadEvent event = new ReadEvent(this.serviceProvider, null, null);
        assertThat(event.isFailure()).isFalse();
    }

    @Test
    public void testIsNotClosed () {
        ReadEvent event = new ReadEvent(this.serviceProvider, null, null);
        assertThat(event.isClosed()).isFalse();
    }

    @Test
    public void testIsNotLoggingRelated() {
        ReadEvent event = new ReadEvent(this.serviceProvider, null, null);

        // Business method & asserts
        assertThat(event.isLoggingRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortRelated() {
        ReadEvent event = new ReadEvent(this.serviceProvider, null, null);
        assertThat(event.isComPortRelated()).isFalse();
        assertThat(event.getComPort()).isNull();
    }

    @Test
    public void testIsComPortRelated () {
        ComPort comPort = mock(ComPort.class);
        when(comPort.isInbound()).thenReturn(false);
        ReadEvent event = new ReadEvent(this.serviceProvider, comPort, "testIsComPortRelated".getBytes());
        assertThat(event.isComPortRelated()).isTrue();
        assertThat(event.getComPort()).isEqualTo(comPort);
    }

    @Test
    public void testIsNotConnectionTaskRelated () {
        ReadEvent event = new ReadEvent(this.serviceProvider, null, null);
        assertThat(event.isConnectionTaskRelated()).isFalse();
        assertThat(event.getConnectionTask()).isNull();
    }

    @Test
    public void testIsNotDeviceRelated () {
        ReadEvent event = new ReadEvent(this.serviceProvider, null, null);
        assertThat(event.isDeviceRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortPoolRelatedByDefault () {
        ReadEvent event = new ReadEvent(this.serviceProvider, null, null);
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsComPortPoolRelatedForOutboundComPort () {
        ComPort comPort = mock(ComPort.class);
        when(comPort.isInbound()).thenReturn(false);
        ReadEvent event = new ReadEvent(this.serviceProvider, comPort, "testIsComPortPoolRelatedForOutboundComPort".getBytes());
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsComPortPoolRelatedForInboundComPort () {
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.isInbound()).thenReturn(true);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(comPort.getComPortPool()).thenReturn(comPortPool);

        ReadEvent event = new ReadEvent(this.serviceProvider, comPort, "testIsComPortPoolRelatedForInboundComPort".getBytes());
        assertThat(event.isComPortPoolRelated()).isTrue();
        assertThat(event.getComPortPool()).isEqualTo(comPortPool);
    }

    @Test
    public void testIsNotComTaskExecutionRelated () {
        ReadEvent event = new ReadEvent(this.serviceProvider, null, null);
        assertThat(event.isComTaskExecutionRelated()).isFalse();
    }

    @Test
    public void testToStringDoesNotFail () throws IOException {
        Instant occurrenceTimestamp = LocalDateTime.of(2012, Calendar.NOVEMBER, 7, 17, 22, 01, 0).toInstant(ZoneOffset.UTC);  // Random pick
        when(this.clock.instant()).thenReturn(occurrenceTimestamp);
        byte[] bytes = "testToStringDoesNotFail".getBytes();
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ReadEvent event = new ReadEvent(this.serviceProvider, comPort, bytes);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}