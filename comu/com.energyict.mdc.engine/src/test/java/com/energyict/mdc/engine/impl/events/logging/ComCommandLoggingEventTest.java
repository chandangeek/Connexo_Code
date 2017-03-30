/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.logging;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
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

@RunWith(MockitoJUnitRunner.class)
public class ComCommandLoggingEventTest {

    private static final long COMPORT_ID = 1;
    private static final long DEVICE_ID = 2;
    private static final long CONNECTION_TASK_ID = 3;
    private static final long COMTASK_EXECUTION_ID = 4;

    @Mock
    public Clock clock;
    @Mock
    private AbstractComServerEventImpl.ServiceProvider serviceProvider;

    @Before
    public void setupServiceProvider () {
        when(clock.instant()).thenReturn(new DateTime(1969, 5, 2, 1, 40, 0).toDate().toInstant()); // Set some default
        when(this.serviceProvider.clock()).thenReturn(this.clock);
    }

    @Test
    public void testCategory () {
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(this.serviceProvider, null, null, null, LogLevel.DEBUG, "testCategory");

        // Business method
        Category category = event.getCategory();

        // Asserts
        assertThat(category).isEqualTo(Category.LOGGING);
    }


    @Test
    public void testOccurrenceTimestamp () {
        Instant now = LocalDateTime.of(2012, Calendar.NOVEMBER, 22, 16, 23, 12, 0).toInstant(ZoneOffset.UTC);  // Random pick
        when(this.clock.instant()).thenReturn(now);

        ComCommandLoggingEvent event = new ComCommandLoggingEvent(this.serviceProvider, null, null, null, LogLevel.INFO, "testOccurrenceTimestamp");

        // Business method
        Instant timestamp = event.getOccurrenceTimestamp();

        // Asserts
        assertThat(timestamp).isEqualTo(now);
    }


    @Test
    public void testIsLoggingRelated () {
        // Business method
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(this.serviceProvider, null, null, null, LogLevel.DEBUG, "testLogLevel");

        // Asserts
        assertThat(event.isLoggingRelated()).isTrue();
    }


    @Test
    public void testLogLevel () {
        LogLevel expectedLogLevel = LogLevel.DEBUG;
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(this.serviceProvider, null, null, null, LogLevel.DEBUG, "testLogLevel");

        // Business method
        LogLevel logLevel = event.getLogLevel();

        // Asserts
        assertThat(logLevel).isEqualTo(expectedLogLevel);
    }

    @Test
    public void testLogMessage () {
        String expectedLogMessage = "testLogMessage";
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(this.serviceProvider, null, null, null, LogLevel.DEBUG, expectedLogMessage);

        // Business method
        String logMessage = event.getLogMessage();

        // Asserts
        assertThat(logMessage).isEqualTo(expectedLogMessage);
    }

    @Test
    public void testIsDeviceRelated () {
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        Device device = mock(Device.class);
        when(connectionTask.getDevice()).thenReturn(device);

        ComCommandLoggingEvent event = new ComCommandLoggingEvent(this.serviceProvider, null, connectionTask, null, LogLevel.INFO, "testIsDeviceRelatedBy");

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isTrue();
        assertThat(event.getDevice()).isEqualTo(device);
    }

    @Test
    public void testIsConnectionTaskRelated(){
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        Device device = mock(Device.class);
        when(connectionTask.getDevice()).thenReturn(device);

        ComCommandLoggingEvent event = new ComCommandLoggingEvent(this.serviceProvider, null, connectionTask, null, LogLevel.INFO, "testIsConnectionTaskRelated");

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isTrue();
        assertThat(event.getConnectionTask()).isEqualTo(connectionTask);
    }

    @Test
    public void isComTaskExecutionRelated(){
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(this.serviceProvider, null, null, comTaskExecution, LogLevel.INFO, "testIsComTaskExecutionRelated");

        // Business method & asserts
        assertThat(event.isComTaskExecutionRelated()).isTrue();
        assertThat(event.getComTaskExecution()).isEqualTo(comTaskExecution);
    }

    @Test
    public void testIsComPortRelated(){
        ComPort comPort = mock(ComPort.class);
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(this.serviceProvider, comPort, null, null, LogLevel.INFO, "testIsComPortRelated");

        // Business method & asserts
        assertThat(event.isComPortRelated()).isTrue();
        assertThat(event.getComPort()).isEqualTo(comPort);
    }

    @Test
    public void testIsComPortPoolRelated(){
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.isInbound()).thenReturn(true);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(comPort.getComPortPool()).thenReturn(comPortPool);
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(this.serviceProvider, comPort, null, null, LogLevel.INFO, "testIsComPortPoolRelated");

        // Business method & asserts
        assertThat(event.isComPortRelated()).isTrue();
        assertThat(event.getComPort()).isEqualTo(comPort);
        assertThat(event.isComPortPoolRelated()).isTrue();
        assertThat(event.getComPortPool()).isEqualTo(comPortPool);
    }

    @Test
    public void testIsNotComPortPoolRelated(){
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.isInbound()).thenReturn(false);
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(this.serviceProvider, comPort, null, null, LogLevel.INFO, "testIsNotComPortPoolRelated");

        // Business method & asserts
        assertThat(event.isComPortRelated()).isTrue();
        assertThat(event.getComPort()).isEqualTo(comPort);
        assertThat(event.isComPortPoolRelated()).isFalse();
    }

    @Test
    public void testIsNotComPortRelated () {
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(this.serviceProvider, null, null, null, LogLevel.DEBUG, "testIsNotComPortRelated");

        // Business method & asserts
        assertThat(event.isComPortRelated()).isFalse();
    }

    @Test
    public void testIsNotConnectionTaskRelated () {
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(this.serviceProvider, null, null, null, LogLevel.DEBUG, "testIsNotConnectionTaskRelated");

        // Business method & asserts
        assertThat(event.isConnectionTaskRelated()).isFalse();
    }

    @Test
    public void testIsNotDeviceRelated(){
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(this.serviceProvider, null, null, null, LogLevel.DEBUG, "testIsNotDeviceRelated");

        // Business method & asserts
        assertThat(event.isDeviceRelated()).isFalse();
    }

    @Test
    public void testIsNotComTaskExecutionRelated(){
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(this.serviceProvider, null, null, null, LogLevel.DEBUG, "testIsNotComTaskExecutionRelated");

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
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASK_EXECUTION_ID);
        ComCommandLoggingEvent event = new ComCommandLoggingEvent(this.serviceProvider, comPort, connectionTask, comTaskExecution, LogLevel.INFO, "testSerializationDoesNotFail");

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}