/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OutboundCapableComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.events.EventReceiver;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifierType;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link Request#applyTo(EventPublisher)} method
 * of the various Request implementation classes.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-05 (14:15)
 */
@RunWith(MockitoJUnitRunner.class)
public class RequestApplyToTest {

    private static final long DEVICE1_ID = 1;
    private static final long CONNECTION_TASK_ID = DEVICE1_ID + 1;
    private static final long COM_TASK_EXECUTION_ID = CONNECTION_TASK_ID + 1;
    private static final long COM_PORT_ID = COM_TASK_EXECUTION_ID + 1;
    private static final long COM_PORT_POOL_ID = COM_PORT_ID + 1;

    @Mock
    private ConnectionTaskService connectionTaskService;
    @Mock
    private CommunicationTaskService communicationTaskService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private EngineConfigurationService engineConfigurationService;
    @Mock
    private RunningComServer runningComServer;
    @Mock
    private OutboundCapableComServer comServer;
    @Mock
    private IdentificationService identificationService;

    @Before
    public void initializeMocks() {
        when(this.runningComServer.getComServer()).thenReturn(this.comServer);
        when(this.comServer.getComPorts()).thenReturn(Collections.emptyList());
    }

    @Test
    public void testCategoryRequest() {
        LoggingRequest request = new LoggingRequest(LogLevel.TRACE, EnumSet.allOf(Category.class));
        EventPublisher eventPublisher = mock(EventPublisher.class);

        // Business method
        request.applyTo(eventPublisher);

        // Asserts
        verify(eventPublisher).narrowInterestToCategories(any(EventReceiver.class), anySetOf(Category.class));
        verify(eventPublisher).narrowInterestToLogLevel(any(EventReceiver.class), any(LogLevel.class));
    }

    @Test
    public void testDeviceRequest() {
        Device device = this.mockDevice();
        DeviceRequest request = new DeviceRequest(identificationService, Collections.singleton(DEVICE1_ID));
        EventPublisher eventPublisher = mock(EventPublisher.class);

        // Business method
        request.applyTo(eventPublisher);

        // Asserts
        ArgumentCaptor<List> deviceArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventPublisher).narrowInterestToDevices(any(EventReceiver.class), deviceArgumentCaptor.capture());
        assertThat(deviceArgumentCaptor.getValue()).containsOnly(device);
    }

    @Test
    public void testConnectionTaskRequest() {
        ConnectionTask connectionTask = this.mockConnectionTask();
        ConnectionTaskRequest request = new ConnectionTaskRequest(connectionTaskService, CONNECTION_TASK_ID);
        EventPublisher eventPublisher = mock(EventPublisher.class);

        // Business method
        request.applyTo(eventPublisher);

        // Asserts
        ArgumentCaptor<List> connectionTaskArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventPublisher).narrowInterestToConnectionTasks(any(EventReceiver.class), connectionTaskArgumentCaptor.capture());
        assertThat(connectionTaskArgumentCaptor.getValue()).containsOnly(connectionTask);
    }

    @Test
    public void testComTaskExecutionRequest() {
        ComTaskExecution comTaskExecution = this.mockComTaskExecution();
        ComTaskExecutionRequest comTaskExecutionRequest = new ComTaskExecutionRequest(this.communicationTaskService, COM_TASK_EXECUTION_ID);
        EventPublisher eventPublisher = mock(EventPublisher.class);

        // Business method
        comTaskExecutionRequest.applyTo(eventPublisher);

        // Asserts
        ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventPublisher).narrowInterestToComTaskExecutions(any(EventReceiver.class), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).containsOnly(comTaskExecution);
    }

    @Test
    public void testComPortRequest() {
        ComPort comPort = this.mockComPort();
        ComPortRequest request = new ComPortRequest(this.runningComServer, COM_PORT_ID);
        EventPublisher eventPublisher = mock(EventPublisher.class);

        // Business method
        request.applyTo(eventPublisher);

        // Asserts
        ArgumentCaptor<List> comPortArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventPublisher).narrowInterestToComPorts(any(EventReceiver.class), comPortArgumentCaptor.capture());
        assertThat(comPortArgumentCaptor.getValue()).containsOnly(comPort);
    }

    @Test
    public void testComPortPoolRequest() {
        ComPortPool comPortPool = this.mockComPortPool();
        ComPortPoolRequest request = new ComPortPoolRequest(engineConfigurationService, COM_PORT_POOL_ID);
        EventPublisher eventPublisher = mock(EventPublisher.class);

        // Business method
        request.applyTo(eventPublisher);

        // Asserts
        ArgumentCaptor<List> comPortPoolArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventPublisher).narrowInterestToComPortPools(any(EventReceiver.class), comPortPoolArgumentCaptor.capture());
        assertThat(comPortPoolArgumentCaptor.getValue()).containsOnly(comPortPool);
    }

    private Device mockDevice() {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE1_ID);
        when(this.deviceService.findDeviceById(DEVICE1_ID)).thenReturn(Optional.of(device));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        when(deviceIdentifier.getDeviceIdentifierType()).thenReturn(DeviceIdentifierType.ActualDevice);
        when(deviceIdentifier.getIdentifier()).thenReturn(String.valueOf(DEVICE1_ID));
        when(this.identificationService.createDeviceIdentifierByDatabaseId(DEVICE1_ID)).thenReturn(deviceIdentifier);
        return device;
    }

    private ConnectionTask mockConnectionTask() {
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(this.connectionTaskService.findConnectionTask(CONNECTION_TASK_ID)).thenReturn(Optional.of(connectionTask));
        return connectionTask;
    }

    private ComTaskExecution mockComTaskExecution() {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COM_TASK_EXECUTION_ID);
        when(this.communicationTaskService.findComTaskExecution(COM_TASK_EXECUTION_ID)).thenReturn(Optional.of(comTaskExecution));
        return comTaskExecution;
    }

    private ComPort mockComPort() {
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getId()).thenReturn(COM_PORT_ID);
        when(this.comServer.getComPorts()).thenReturn(Collections.singletonList(comPort));
        when(this.comServer.getOutboundComPorts()).thenReturn(Collections.singletonList(comPort));
        when(comPort.getId()).thenReturn(COM_PORT_ID);
        return comPort;
    }

    private ComPortPool mockComPortPool() {
        ComPortPool comPortPool = mock(ComPortPool.class);
        when(comPortPool.getId()).thenReturn(Long.valueOf(COM_PORT_POOL_ID));
        doReturn(Optional.of(comPortPool)).when(this.engineConfigurationService).findComPortPool(COM_PORT_POOL_ID);
        return comPortPool;
    }

}