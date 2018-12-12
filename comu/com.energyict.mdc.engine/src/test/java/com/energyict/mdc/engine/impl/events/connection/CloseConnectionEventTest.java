/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.connection;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OutboundComPort;
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
 * Tests the {@link com.energyict.mdc.engine.impl.events.connection.CloseConnectionEvent} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-06 (15:13)
 */
@RunWith(MockitoJUnitRunner.class)
public class CloseConnectionEventTest {

    private static final long DEVICE_ID = 1;
    private static final long COMPORT_ID = DEVICE_ID + 1;
    private static final long CONNECTION_TASK_ID = COMPORT_ID + 1;

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
        CloseConnectionEvent event = new CloseConnectionEvent(this.serviceProvider, null, null);

        // Business method
        Category category = event.getCategory();

        // Asserts
        assertThat(category).isEqualTo(Category.CONNECTION);
    }

    @Test
    public void testOccurrenceTimestamp () {
        Instant now = LocalDateTime.of(2012, Calendar.NOVEMBER, 6, 13, 45, 17, 0).toInstant(ZoneOffset.UTC);  // Random pick
        when(this.clock.instant()).thenReturn(now);

        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        CloseConnectionEvent event = new CloseConnectionEvent(this.serviceProvider, comPort, connectionTask);

        // Business method
        Instant timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testIsNotEstablishing () {
        CloseConnectionEvent event = new CloseConnectionEvent(this.serviceProvider, null, null);

        // Business method & asserts
        assertThat(event.isEstablishing()).isFalse();
    }

    @Test
    public void testIsNotFailure () {
        CloseConnectionEvent event = new CloseConnectionEvent(this.serviceProvider, null, null);

        // Business method & asserts
        assertThat(event.isFailure()).isFalse();
        assertThat(event.getFailureMessage()).isNull();
    }

    @Test
    public void testIsClosed () {
        CloseConnectionEvent event = new CloseConnectionEvent(this.serviceProvider, null, null);

        // Business method & asserts
        assertThat(event.isClosed()).isTrue();
    }

    @Test
    public void testIsNotLoggingRelated () {
        CloseConnectionEvent event = new CloseConnectionEvent(this.serviceProvider, null, null);

        // Business method & asserts
        assertThat(event.isLoggingRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortRelated () {
        CloseConnectionEvent event = new CloseConnectionEvent(this.serviceProvider, null, null);

        // Business method & asserts
        assertThat(event.isComPortRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortPoolRelated () {
        CloseConnectionEvent event = new CloseConnectionEvent(this.serviceProvider, null, null);

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsNotConnectionTaskRelated () {
        CloseConnectionEvent event = new CloseConnectionEvent(this.serviceProvider, null, null);

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isFalse();
    }

    @Test
    public void testIsNotDeviceRelated () {
        CloseConnectionEvent event = new CloseConnectionEvent(this.serviceProvider, null, null);

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isFalse();
    }

    @Test
    public void testIsComPortRelated () {
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        CloseConnectionEvent event = new CloseConnectionEvent(this.serviceProvider, comPort, connectionTask);

        // Business method & asserts
        assertThat(event.isComPortRelated()).isTrue();
        assertThat(event.getComPort()).isEqualTo(comPort);
    }

    @Test
    public void testIsNotComPortPoolRelatedForOutboundComPorts () {
        OutboundComPort comPort = mock(OutboundComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        CloseConnectionEvent event = new CloseConnectionEvent(this.serviceProvider, comPort, connectionTask);

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsComPortPoolRelatedForInboundComPorts () {
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.isInbound()).thenReturn(true);
        when(comPort.getComPortPool()).thenReturn(comPortPool);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        CloseConnectionEvent event = new CloseConnectionEvent(this.serviceProvider, comPort, connectionTask);

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isTrue();
        assertThat(event.getComPortPool()).isEqualTo(comPortPool);
    }

    @Test
    public void testIsNotComTaskRelated () {
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        CloseConnectionEvent event = new CloseConnectionEvent(this.serviceProvider, comPort, connectionTask);

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isFalse();
    }

    @Test
    public void testIsConnectionTaskRelated () {
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        CloseConnectionEvent event = new CloseConnectionEvent(this.serviceProvider, comPort, connectionTask);

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isTrue();
        assertThat(event.getConnectionTask()).isEqualTo(connectionTask);
    }

    @Test
    public void testIsDeviceRelated () {
        Device device = mock(Device.class);
        ComPort comPort = mock(ComPort.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getDevice()).thenReturn(device);
        CloseConnectionEvent event = new CloseConnectionEvent(this.serviceProvider, comPort, connectionTask);

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isTrue();
        assertThat(event.getDevice()).isEqualTo(device);
    }

    @Test
    public void testToStringDoesNotFail () throws IOException {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        CloseConnectionEvent event = new CloseConnectionEvent(this.serviceProvider, comPort, connectionTask);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}