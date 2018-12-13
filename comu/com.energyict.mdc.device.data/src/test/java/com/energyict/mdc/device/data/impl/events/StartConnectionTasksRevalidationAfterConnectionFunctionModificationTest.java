/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.MessageService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.energyict.mdc.device.data.impl.events.ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandlerFactory.TASK_DESTINATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link StartConnectionTasksRevalidationAfterConnectionFunctionModification} component
 *
 * @author Stijn Vanhoorelbeke
 * @since 18.08.17 - 10:49
 */
@RunWith(MockitoJUnitRunner.class)
public class StartConnectionTasksRevalidationAfterConnectionFunctionModificationTest {
    private static final long PARTIAL_CONNECTION_TASK_ID = 97L;
    private static final long PREVIOUS_CONNECTION_FUNCTION_ID = 98L;
    private static final int NUMBER_OF_CONNECTION_TASKS_PER_TRANSACTION = 5;

    @Mock
    private MessageService messageService;
    @Mock
    private ServerConnectionTaskService connectionTaskService;

    @Test
    public void publishUsesTheCorrectDestinationSpec() {
        StartConnectionTasksRevalidationAfterConnectionFunctionModification testInstance = this.publishTestInstance();
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
        StartConnectionTasksRevalidationAfterConnectionFunctionModification testInstance = this.publishTestInstance();
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
        assertThat(message).contains(StartConnectionTasksRevalidationAfterConnectionFunctionModification.class.getSimpleName());
    }

    @Test
    public void payloadIncludesPartialConnectionTaskIdAndForTheMessageParser() {
        StartConnectionTasksRevalidationAfterConnectionFunctionModification testInstance = this.publishTestInstance();
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
        assertThat(message).contains(String.valueOf(PARTIAL_CONNECTION_TASK_ID) +
                StartConnectionTasksRevalidationAfterConnectionFunctionModification.PARTIAL_CONNECTION_TASK_CONNECTION_FUNCTION_DELIMITER +
                String.valueOf(PREVIOUS_CONNECTION_FUNCTION_ID));
    }

    @Test
    public void publishSendsTheMessage() {
        StartConnectionTasksRevalidationAfterConnectionFunctionModification testInstance = this.publishTestInstance();
        MessageBuilder messageBuilder = mock(MessageBuilder.class);
        DestinationSpec destinationSpec = mock(DestinationSpec.class);
        when(destinationSpec.message(anyString())).thenReturn(messageBuilder);
        when(this.messageService.getDestinationSpec(TASK_DESTINATION)).thenReturn(Optional.of(destinationSpec));

        // Business method
        testInstance.publish();

        // Asserts
        verify(messageBuilder).send();
    }

    @Test
    public void processFindsConnectionTasksForPartialConnectionTaskId() {
        StartConnectionTasksRevalidationAfterConnectionFunctionModification testInstance = this.processTestInstance();
        when(this.connectionTaskService.findConnectionTasksForPartialId(PARTIAL_CONNECTION_TASK_ID)).thenReturn(Collections.<Long>emptyList());

        // Business method
        testInstance.process();

        // Asserts
        verify(this.connectionTaskService).findConnectionTasksForPartialId(PARTIAL_CONNECTION_TASK_ID);
    }

    @Test
    public void processPartitionsConnectionTasks() {
        StartConnectionTasksRevalidationAfterConnectionFunctionModification testInstance = this.processTestInstance();
        List<Long> ids = new ArrayList<>();
        for (long id = 1; id < 13; id++) {
            ids.add(id);
        }
        when(this.connectionTaskService.findConnectionTasksForPartialId(PARTIAL_CONNECTION_TASK_ID)).thenReturn(ids);
        MessageBuilder messageBuilder = mock(MessageBuilder.class);
        DestinationSpec destinationSpec = mock(DestinationSpec.class);
        when(destinationSpec.message(anyString())).thenReturn(messageBuilder);
        when(this.messageService.getDestinationSpec(TASK_DESTINATION)).thenReturn(Optional.of(destinationSpec));

        // Business method
        testInstance.process();

        // Asserts
        verify(messageBuilder, times(3)).send();
    }

    @Test
    public void processUsesAllConnectionTasks() {
        StartConnectionTasksRevalidationAfterConnectionFunctionModification testInstance = this.processTestInstance();
        List<Long> expectedIds = new ArrayList<>();
        for (long id = 1; id < 13; id++) {
            expectedIds.add(id);
        }
        when(this.connectionTaskService.findConnectionTasksForPartialId(PARTIAL_CONNECTION_TASK_ID)).thenReturn(expectedIds);
        MessageBuilder messageBuilder = mock(MessageBuilder.class);
        DestinationSpec destinationSpec = mock(DestinationSpec.class);
        when(destinationSpec.message(anyString())).thenReturn(messageBuilder);
        when(this.messageService.getDestinationSpec(TASK_DESTINATION)).thenReturn(Optional.of(destinationSpec));

        // Business method
        testInstance.process();

        // Asserts
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(destinationSpec, times(3)).message(messageCaptor.capture());
        List<String> messages = messageCaptor.getAllValues();
        List<Long> actualIds = new ArrayList<>();
        for (String message : messages) {
            String messageProperties = message.split(ConnectionTasksRevalidationMessage.CLASS_NAME_PROPERTIES_SEPARATOR)[1];
            String connectionTaskIds = messageProperties.split(RevalidateConnectionTasksAfterConnectionFunctionModification.CONNECTION_FUNCTION_CONNECTION_TASKS_DELIMITER)[1];
            actualIds.addAll(
                    Stream
                            .of(connectionTaskIds.split(RevalidateConnectionTasksAfterConnectionFunctionModification.CONNECTION_TASKS_DELIMITER))
                            .map(Long::parseLong)
                            .collect(Collectors.toList()));
        }
        assertThat(actualIds).isEqualTo(expectedIds);
    }

    private StartConnectionTasksRevalidationAfterConnectionFunctionModification publishTestInstance() {
        return StartConnectionTasksRevalidationAfterConnectionFunctionModification
                .forPublishing(this.messageService)
                .with(
                        String.valueOf(PARTIAL_CONNECTION_TASK_ID) +
                                StartConnectionTasksRevalidationAfterConnectionFunctionModification.PARTIAL_CONNECTION_TASK_CONNECTION_FUNCTION_DELIMITER +
                                String.valueOf(PREVIOUS_CONNECTION_FUNCTION_ID)
                );
    }

    private StartConnectionTasksRevalidationAfterConnectionFunctionModification processTestInstance() {
        return new StartConnectionTasksRevalidationAfterConnectionFunctionModification(
                this.messageService,
                this.connectionTaskService,
                NUMBER_OF_CONNECTION_TASKS_PER_TRANSACTION)
                .with(PARTIAL_CONNECTION_TASK_ID, PREVIOUS_CONNECTION_FUNCTION_ID);
    }

}