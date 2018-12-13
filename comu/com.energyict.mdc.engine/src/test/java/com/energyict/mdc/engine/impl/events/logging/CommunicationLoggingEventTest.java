/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.logging;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;

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
 * Tests the {@link com.energyict.mdc.engine.impl.events.logging.CommunicationLoggingEvent} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-08 (11:47)
 */
@RunWith(MockitoJUnitRunner.class)
public class CommunicationLoggingEventTest {

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
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(this.serviceProvider, null, null, LogLevel.DEBUG, "testCategory");

        // Business method
        Category category = event.getCategory();

        // Asserts
        assertThat(category).isEqualTo(Category.LOGGING);
    }

    @Test
    public void testOccurrenceTimestamp () {
        Instant now = LocalDateTime.of(2012, Calendar.NOVEMBER, 6, 17, 22, 01, 0).toInstant(ZoneOffset.UTC);  // Random pick
        when(this.clock.instant()).thenReturn(now);

        CommunicationLoggingEvent event = new CommunicationLoggingEvent(this.serviceProvider, null, null, LogLevel.INFO, "testOccurrenceTimestamp");

        // Business method
        Instant timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }

    @Test
    public void testIsLoggingRelated () {
        // Business method
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(this.serviceProvider, null, null, LogLevel.DEBUG, "testLogLevel");

        // Asserts
        assertThat(event.isLoggingRelated()).isTrue();
    }

    @Test
    public void testLogLevel () {
        LogLevel expectedLogLevel = LogLevel.DEBUG;
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(this.serviceProvider, null, null, expectedLogLevel, "testLogLevel");

        // Business method
        LogLevel logLevel = event.getLogLevel();

        // Asserts
        assertThat(logLevel).isEqualTo(expectedLogLevel);
    }

    @Test
    public void testLogMessage () {
        String expectedLogMessage = "testLogMessage";
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(this.serviceProvider, null, null, LogLevel.DEBUG, expectedLogMessage);

        // Business method
        String logMessage = event.getLogMessage();

        // Asserts
        assertThat(logMessage).isEqualTo(expectedLogMessage);
    }

    @Test
    public void testIsDeviceRelated () {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(this.serviceProvider, connectionTask, comPort, LogLevel.INFO, "testIsDeviceRelatedBy");

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isTrue();
        assertThat(event.getDevice()).isEqualTo(device);
    }

    @Test
    public void testIsNotConnectionTaskRelatedByDefault () {
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(this.serviceProvider, null, null, LogLevel.DEBUG, "testIsNotConnectionTaskRelatedByDefault");

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isFalse();

    }

    @Test
    public void testIsConnectionTaskRelated () {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(this.serviceProvider, connectionTask, comPort, LogLevel.INFO, "testIsDeviceRelatedBy");

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isTrue();
        assertThat(event.getConnectionTask()).isEqualTo(connectionTask);

    }

    @Test
    public void testIsNotComPortRelated () {
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(this.serviceProvider, null, null, LogLevel.DEBUG, "testIsNotComPortRelated");

        // Business method & asserts
        assertThat(event.isComPortRelated()).isFalse();

    }

    @Test
    public void testIsComPortRelated () {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(this.serviceProvider, connectionTask, comPort, LogLevel.INFO, "testIsDeviceRelatedBy");

        // Business method & asserts
        assertThat(event.isComPortRelated()).isTrue();
        assertThat(event.getComPort()).isEqualTo(comPort);
    }

    @Test
    public void testIsNotComPortPoolRelated () {
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(this.serviceProvider, null, null, LogLevel.DEBUG, "testIsNotComPortPoolRelated");

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();

    }

    @Test
    public void testIsNotComPortPoolRelatedForOutboundComPorts () {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPort.isInbound()).thenReturn(false);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(this.serviceProvider, connectionTask, comPort, LogLevel.INFO, "testIsDeviceRelatedBy");

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsComPortPoolRelatedForInboundComPorts () {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPort.isInbound()).thenReturn(true);
        when(comPort.getComPortPool()).thenReturn(comPortPool);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(this.serviceProvider, connectionTask, comPort, LogLevel.INFO, "testIsDeviceRelatedBy");

        // Business method & asserts
        assertThat(event.isComPortPoolRelated()).isTrue();
        assertThat(event.getComPortPool()).isEqualTo(comPortPool);
    }

    @Test
    public void testIsNotComTaskExecutionRelated () {
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(this.serviceProvider, null, null, LogLevel.DEBUG, "testIsNotComTaskExecutionRelated");

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isFalse();

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
        CommunicationLoggingEvent event = new CommunicationLoggingEvent(this.serviceProvider, connectionTask, comPort, LogLevel.INFO, "testSerializationDoesNotFail");

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}