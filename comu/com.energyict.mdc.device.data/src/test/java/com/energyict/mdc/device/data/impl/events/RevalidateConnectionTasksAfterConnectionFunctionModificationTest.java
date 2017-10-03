/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.MessageService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.protocol.api.ConnectionFunction;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.energyict.mdc.device.data.impl.events.ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandlerFactory.TASK_DESTINATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link RevalidateConnectionTasksAfterConnectionFunctionModification} component
 *
 * @author Stijn Vanhoorelbeke
 * @since 18.08.17 - 11:02
 */
@RunWith(MockitoJUnitRunner.class)
public class RevalidateConnectionTasksAfterConnectionFunctionModificationTest {

    private static final String PREVIOUS_CONNECTION_FUNCTION_ID = "1";
    private static final String CONNECTION_TASK_IDS = "1,2,3";

    @Mock
    private MessageService messageService;
    @Mock
    private ConnectionTaskService connectionTaskService;
    private ConnectionFunction connectionFunction1;
    private ConnectionFunction connectionFunction2;

    @Before
    public void setUp() throws Exception {
        connectionFunction1 = mock(ConnectionFunction.class);
        when(connectionFunction1.getId()).thenReturn(1L);
        connectionFunction2 = mock(ConnectionFunction.class);
        when(connectionFunction2.getId()).thenReturn(2L);
    }

    @Test
    public void publishUsesTheCorrectDestinationSpec() {
        RevalidateConnectionTasksAfterConnectionFunctionModification testInstance = this.publishTestInstance();
        MessageBuilder messageBuilder = mock(MessageBuilder.class);
        DestinationSpec destinationSpec = mock(DestinationSpec.class);
        when(destinationSpec.message(anyString())).thenReturn(messageBuilder);
        when(this.messageService.getDestinationSpec(TASK_DESTINATION)).thenReturn(Optional.of(destinationSpec));

        // Business method
        testInstance.publish();

        // Asserts
        verify(this.messageService).getDestinationSpec(ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandlerFactory.TASK_DESTINATION);
    }

    @Test
    public void payloadIncludesClassNameForTheMessageParser() {
        RevalidateConnectionTasksAfterConnectionFunctionModification testInstance = this.publishTestInstance();
        MessageBuilder messageBuilder = mock(MessageBuilder.class);
        DestinationSpec destinationSpec = mock(DestinationSpec.class);
        when(destinationSpec.message(anyString())).thenReturn(messageBuilder);
        when(this.messageService.getDestinationSpec(TASK_DESTINATION)).thenReturn(Optional.of(destinationSpec));

        // Business method
        testInstance.publish();

        // Asserts
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(destinationSpec).message(messageCaptor.capture());
        String message = messageCaptor.getValue();
        assertThat(message).contains(RevalidateConnectionTasksAfterConnectionFunctionModification.class.getSimpleName());
    }

    @Test
    public void payloadIncludesPreviousConnectionFunctionIdAndPartialConnectionTaskIdForTheMessageParser() {
        RevalidateConnectionTasksAfterConnectionFunctionModification testInstance = this.publishTestInstance();
        MessageBuilder messageBuilder = mock(MessageBuilder.class);
        DestinationSpec destinationSpec = mock(DestinationSpec.class);
        when(destinationSpec.message(anyString())).thenReturn(messageBuilder);
        when(this.messageService.getDestinationSpec(TASK_DESTINATION)).thenReturn(Optional.of(destinationSpec));

        // Business method
        testInstance.publish();

        // Asserts
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(destinationSpec).message(messageCaptor.capture());
        String message = messageCaptor.getValue();
        assertThat(message).contains(String.valueOf(PREVIOUS_CONNECTION_FUNCTION_ID) +
                RevalidateConnectionTasksAfterConnectionFunctionModification.CONNECTION_FUNCTION_CONNECTION_TASKS_DELIMITER +
                String.valueOf(CONNECTION_TASK_IDS));
    }

    @Test
    public void processFindsAllConnectionTasks() {
        RevalidateConnectionTasksAfterConnectionFunctionModification testInstance = this.processTestInstance();
        when(this.connectionTaskService.findConnectionTask(anyInt())).thenReturn(Optional.empty());

        // Business method
        testInstance.process();

        // Asserts
        verify(this.connectionTaskService).findConnectionTask(1);
        verify(this.connectionTaskService).findConnectionTask(2);
        verify(this.connectionTaskService).findConnectionTask(3);
    }

    @Test
    public void processCallsNotifyConnectionFunctionUpdateOnAllConnectionTasks() {
        RevalidateConnectionTasksAfterConnectionFunctionModification testInstance = this.processTestInstance();
        ServerConnectionTask ct1 = mockedServerConnectionTask();
        ServerConnectionTask ct2 = mockedServerConnectionTask();
        ServerConnectionTask ct3 = mockedServerConnectionTask();


        when(this.connectionTaskService.findConnectionTask(1)).thenReturn(Optional.of(ct1));
        when(this.connectionTaskService.findConnectionTask(2)).thenReturn(Optional.of(ct2));
        when(this.connectionTaskService.findConnectionTask(3)).thenReturn(Optional.of(ct3));

        // Business method
        testInstance.process();

        // Asserts
        verify(ct1).notifyConnectionFunctionUpdate(Optional.of(connectionFunction1), Optional.of(connectionFunction2));
        verify(ct2).notifyConnectionFunctionUpdate(Optional.of(connectionFunction1), Optional.of(connectionFunction2));
        verify(ct3).notifyConnectionFunctionUpdate(Optional.of(connectionFunction1), Optional.of(connectionFunction2));
    }

    private ServerConnectionTask mockedServerConnectionTask() {
        Device device = mock(Device.class);
        DeviceType deviceType = mock(DeviceType.class);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);

        ServerConnectionTask connectionTask = mock(ServerConnectionTask.class);
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        when(connectionTask.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(partialConnectionTask.getConnectionFunction()).thenReturn(Optional.of(connectionFunction2));
        when(connectionTask.getDevice()).thenReturn(device);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(deviceProtocolPluggableClass.getProvidedConnectionFunctions()).thenReturn(Arrays.asList(connectionFunction1, connectionFunction2));
        return connectionTask;
    }

    private RevalidateConnectionTasksAfterConnectionFunctionModification publishTestInstance() {
        return RevalidateConnectionTasksAfterConnectionFunctionModification
                .forPublishing(this.messageService)
                .with(
                        String.valueOf(PREVIOUS_CONNECTION_FUNCTION_ID) +
                                RevalidateConnectionTasksAfterConnectionFunctionModification.CONNECTION_FUNCTION_CONNECTION_TASKS_DELIMITER +
                                String.valueOf(CONNECTION_TASK_IDS)
                );
    }

    private RevalidateConnectionTasksAfterConnectionFunctionModification processTestInstance() {
        List<Long> connectionTaskIds = Arrays.stream(CONNECTION_TASK_IDS.split(RevalidateConnectionTasksAfterConnectionFunctionModification.CONNECTION_TASKS_DELIMITER))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        return new RevalidateConnectionTasksAfterConnectionFunctionModification(this.messageService, this.connectionTaskService).with(Long.parseLong(PREVIOUS_CONNECTION_FUNCTION_ID), connectionTaskIds);
    }

}