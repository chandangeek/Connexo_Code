package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.MessageService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;

import static com.energyict.mdc.device.data.impl.events.ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory.TASK_DESTINATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link StartConnectionTasksRevalidationAfterPropertyRemoval} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-16 (14:31)
 */
@RunWith(MockitoJUnitRunner.class)
public class StartConnectionTasksRevalidationAfterPropertyRemovalTest {

    private static final long PARTIAL_CONNECTION_TASK_ID = 97L;
    private static final int NUMBER_OF_CONNECTION_TASKS_PER_TRANSACTION = 5;

    @Mock
    private MessageService messageService;
    @Mock
    private ServerConnectionTaskService connectionTaskService;

    @Test
    public void publishUsesTheCorrectDestinationSpec() {
        StartConnectionTasksRevalidationAfterPropertyRemoval testInstance = this.publishTestInstance();
        MessageBuilder messageBuilder = mock(MessageBuilder.class);
        DestinationSpec destinationSpec = mock(DestinationSpec.class);
        when(destinationSpec.message(anyString())).thenReturn(messageBuilder);
        when(this.messageService.getDestinationSpec(TASK_DESTINATION)).thenReturn(Optional.of(destinationSpec));

        // Business method
        testInstance.publish();

        // Asserts
        verify(this.messageService).getDestinationSpec(ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory.TASK_DESTINATION);
    }

    @Test
    public void payloadIncludesClassNameForTheMessageParser() {
        StartConnectionTasksRevalidationAfterPropertyRemoval testInstance = this.publishTestInstance();
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
        assertThat(message).contains(StartConnectionTasksRevalidationAfterPropertyRemoval.class.getSimpleName());
    }

    @Test
    public void payloadIncludesPartialConnectionTaskIdForTheMessageParser() {
        StartConnectionTasksRevalidationAfterPropertyRemoval testInstance = this.publishTestInstance();
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
        assertThat(message).contains(String.valueOf(PARTIAL_CONNECTION_TASK_ID));
    }

    @Test
    public void publishSendsTheMessage() {
        StartConnectionTasksRevalidationAfterPropertyRemoval testInstance = this.publishTestInstance();
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
        StartConnectionTasksRevalidationAfterPropertyRemoval testInstance = this.processTestInstance();
        when(this.connectionTaskService.findConnectionTasksForPartialId(PARTIAL_CONNECTION_TASK_ID)).thenReturn(Collections.<Long>emptyList());

        // Business method
        testInstance.process();

        // Asserts
        verify(this.connectionTaskService).findConnectionTasksForPartialId(PARTIAL_CONNECTION_TASK_ID);
    }

    @Test
    public void processPartitionsConnectionTasks() {
        StartConnectionTasksRevalidationAfterPropertyRemoval testInstance = this.processTestInstance();
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
        StartConnectionTasksRevalidationAfterPropertyRemoval testInstance = this.processTestInstance();
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
            actualIds.addAll(
                    Stream
                            .of(messageProperties.split(RevalidateConnectionTasksAfterPropertyRemoval.DELIMITER))
                            .map(Long::parseLong)
                        .collect(Collectors.toList()));
        }
        assertThat(actualIds).isEqualTo(expectedIds);
    }

    private StartConnectionTasksRevalidationAfterPropertyRemoval publishTestInstance() {
        return StartConnectionTasksRevalidationAfterPropertyRemoval.forPublishing(this.messageService).with(String.valueOf(PARTIAL_CONNECTION_TASK_ID));
    }

    private StartConnectionTasksRevalidationAfterPropertyRemoval processTestInstance() {
        return new StartConnectionTasksRevalidationAfterPropertyRemoval(
                    this.messageService,
                    this.connectionTaskService,
                    NUMBER_OF_CONNECTION_TASKS_PER_TRANSACTION)
                .with(PARTIAL_CONNECTION_TASK_ID);
    }

}