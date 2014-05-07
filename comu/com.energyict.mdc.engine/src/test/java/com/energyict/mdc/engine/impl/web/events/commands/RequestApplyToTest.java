package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.engine.events.Category;
import com.energyict.comserver.eventsimpl.EventPublisher;
import com.energyict.comserver.eventsimpl.EventReceiver;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.MdwInterface;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.engine.impl.web.events.commands.ComPortPoolRequest;
import com.energyict.mdc.engine.impl.web.events.commands.ComPortRequest;
import com.energyict.mdc.engine.impl.web.events.commands.ComTaskExecutionRequest;
import com.energyict.mdc.engine.impl.web.events.commands.ConnectionTaskRequest;
import com.energyict.mdc.engine.impl.web.events.commands.DeviceRequest;
import com.energyict.mdc.engine.impl.web.events.commands.LoggingRequest;
import com.energyict.mdc.engine.impl.web.events.commands.Request;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.ports.ComPortFactory;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.ports.ComPortPoolFactory;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.communication.tasks.ServerComTaskExecutionFactory;
import com.energyict.mdc.communication.tasks.ServerConnectionTaskFactory;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdw.core.DeviceFactory;
import org.junit.*;
import org.mockito.ArgumentCaptor;

import java.util.EnumSet;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
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
public class RequestApplyToTest {

    private static final int DEVICE1_ID = 1;
    private static final int CONNECTION_TASK_ID = DEVICE1_ID + 1;
    private static final int COM_TASK_EXECUTION_ID = CONNECTION_TASK_ID + 1;
    private static final int COM_PORT_ID = COM_TASK_EXECUTION_ID + 1;
    private static final int COM_PORT_POOL_ID = COM_PORT_ID + 1;

    @Test
    public void testCategoryRequest () {
        LoggingRequest request = new LoggingRequest(LogLevel.TRACE, EnumSet.allOf(Category.class));
        EventPublisher eventPublisher = mock(EventPublisher.class);

        // Business method
        request.applyTo(eventPublisher);

        // Asserts
        verify(eventPublisher).narrowInterestToCategories(any(EventReceiver.class), anySetOf(Category.class));
        verify(eventPublisher).narrowInterestToLogLevel(any(EventReceiver.class), any(LogLevel.class));
    }

    @Test
    public void testDeviceRequest () {
        BaseDevice device = this.mockDevice();
        DeviceRequest request = new DeviceRequest(DEVICE1_ID);
        EventPublisher eventPublisher = mock(EventPublisher.class);

        // Business method
        request.applyTo(eventPublisher);

        // Asserts
        ArgumentCaptor<List> deviceArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventPublisher).narrowInterestToDevices(any(EventReceiver.class), deviceArgumentCaptor.capture());
        assertThat(deviceArgumentCaptor.getValue()).containsOnly(device);
    }

    @Test
    public void testConnectionTaskRequest () {
        ConnectionTask connectionTask = this.mockConnectionTask();
        ConnectionTaskRequest request = new ConnectionTaskRequest(CONNECTION_TASK_ID);
        EventPublisher eventPublisher = mock(EventPublisher.class);

        // Business method
        request.applyTo(eventPublisher);

        // Asserts
        ArgumentCaptor<List> connectionTaskArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventPublisher).narrowInterestToConnectionTasks(any(EventReceiver.class), connectionTaskArgumentCaptor.capture());
        assertThat(connectionTaskArgumentCaptor.getValue()).containsOnly(connectionTask);
    }

    @Test
    public void testComTaskExecutionRequest () {
        ComTaskExecution comTaskExecution = this.mockComTaskExecution();
        ComTaskExecutionRequest comTaskExecutionRequest = new ComTaskExecutionRequest(COM_TASK_EXECUTION_ID);
        EventPublisher eventPublisher = mock(EventPublisher.class);

        // Business method
        comTaskExecutionRequest.applyTo(eventPublisher);

        // Asserts
        ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventPublisher).narrowInterestToComTaskExecutions(any(EventReceiver.class), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).containsOnly(comTaskExecution);
    }

    @Test
    public void testComPortRequest () {
        ComPort comPort = this.mockComPort();
        ComPortRequest request = new ComPortRequest(COM_PORT_ID);
        EventPublisher eventPublisher = mock(EventPublisher.class);

        // Business method
        request.applyTo(eventPublisher);

        // Asserts
        ArgumentCaptor<List> comPortArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventPublisher).narrowInterestToComPorts(any(EventReceiver.class), comPortArgumentCaptor.capture());
        assertThat(comPortArgumentCaptor.getValue()).containsOnly(comPort);
    }

    @Test
    public void testComPortPoolRequest () {
        ComPortPool comPortPool = this.mockComPortPool();
        ComPortPoolRequest request = new ComPortPoolRequest(COM_PORT_POOL_ID);
        EventPublisher eventPublisher = mock(EventPublisher.class);

        // Business method
        request.applyTo(eventPublisher);

        // Asserts
        ArgumentCaptor<List> comPortPoolArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventPublisher).narrowInterestToComPortPools(any(EventReceiver.class), comPortPoolArgumentCaptor.capture());
        assertThat(comPortPoolArgumentCaptor.getValue()).containsOnly(comPortPool);
    }

    private BaseDevice mockDevice () {
        BaseDevice device = mock(BaseDevice.class);
        when(device.getId()).thenReturn(DEVICE1_ID);
        DeviceFactory deviceFactory = mock(DeviceFactory.class);
        when(deviceFactory.find(DEVICE1_ID)).thenReturn(device);
        ServerManager manager = mock(ServerManager.class);
        MdwInterface mdwInterface = mock(MdwInterface.class);
        when(mdwInterface.getDeviceFactory()).thenReturn(deviceFactory);
        when(manager.getMdwInterface()).thenReturn(mdwInterface);
        ManagerFactory.setCurrent(manager);
        return device;
    }

    private ConnectionTask mockConnectionTask () {
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        ServerConnectionTaskFactory connectionTaskFactory = mock(ServerConnectionTaskFactory.class);
        when(connectionTaskFactory.find(CONNECTION_TASK_ID)).thenReturn(connectionTask);
        ServerManager manager = mock(ServerManager.class);
        when(manager.getConnectionTaskFactory()).thenReturn(connectionTaskFactory);
        ManagerFactory.setCurrent(manager);
        return connectionTask;
    }

    private ComTaskExecution mockComTaskExecution () {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COM_TASK_EXECUTION_ID);
        ServerComTaskExecutionFactory comTaskExecutionFactory = mock(ServerComTaskExecutionFactory.class);
        when(comTaskExecutionFactory.find(COM_TASK_EXECUTION_ID)).thenReturn(comTaskExecution);
        ServerManager manager = mock(ServerManager.class);
        when(manager.getComTaskExecutionFactory()).thenReturn(comTaskExecutionFactory);
        ManagerFactory.setCurrent(manager);
        return comTaskExecution;
    }

    private ComPort mockComPort () {
        ComPort comPort = mock(ComPort.class);
        when(comPort.getId()).thenReturn(COM_PORT_ID);
        ComPortFactory comPortFactory = mock(ComPortFactory.class);
        when(comPortFactory.find(COM_PORT_ID)).thenReturn(comPort);
        ServerManager manager = mock(ServerManager.class);
        when(manager.getComPortFactory()).thenReturn(comPortFactory);
        ManagerFactory.setCurrent(manager);
        return comPort;
    }

    private ComPortPool mockComPortPool () {
        ComPortPool comPortPool = mock(ComPortPool.class);
        when(comPortPool.getId()).thenReturn(Long.valueOf(COM_PORT_POOL_ID));
        ComPortPoolFactory comPortPoolFactory = mock(ComPortPoolFactory.class);
        when(comPortPoolFactory.find(COM_PORT_POOL_ID)).thenReturn(comPortPool);
        ServerManager manager = mock(ServerManager.class);
        when(manager.getComPortPoolFactory()).thenReturn(comPortPoolFactory);
        ManagerFactory.setCurrent(manager);
        return comPortPool;
    }

}