/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.MessageService;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.config.events.PartialConnectionTaskUpdateDetails;
import com.energyict.mdc.protocol.api.ConnectionFunction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.energyict.mdc.device.data.impl.events.StartConnectionTasksRevalidationAfterConnectionFunctionModification.PARTIAL_CONNECTION_TASK_CONNECTION_FUNCTION_DELIMITER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link PartialConnectionTaskUpdateHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-15 (08:41)
 */
@RunWith(MockitoJUnitRunner.class)
public class PartialConnectionTaskUpdateHandlerTest {

    private static final long PARTIAL_CONNECTION_TASK_ID = 97L;
    private static final long CONNECTION_FUNCTION_ID = 98L;
    private static final String TOPIC = EventType.PARTIAL_SCHEDULED_CONNECTION_TASK_UPDATED.topic();

    @Mock
    private MessageService messageService;
    @Mock
    private LocalEvent localEvent;
    @Mock
    private com.elster.jupiter.events.EventType eventType;

    @Before
    public void initializeMocks() {
        when(this.localEvent.getType()).thenReturn(this.eventType);
        when(this.eventType.getTopic()).thenReturn(TOPIC);
    }

    @Test
    public void handlerGetsTheEventType() {
        PartialConnectionTaskUpdateHandler testInstance = this.testInstance();
        when(this.eventType.getTopic()).thenReturn("event.type.topic");

        // Business method
        testInstance.onEvent(this.localEvent);

        // Asserts
        verify(this.localEvent).getType();
    }

    @Test
    public void eventThatDoesNotMatchTheEventIsIgnored() {
        PartialConnectionTaskUpdateHandler testInstance = this.testInstance();
        when(this.eventType.getTopic()).thenReturn("event.type.topic");

        // Business method
        testInstance.onEvent(this.localEvent);

        // Asserts
        verifyNoMoreInteractions(this.messageService);
    }

    @Test
    public void eventWithoutAddedOrRemovedRequiredPropertiesAndWithoutConnectionFunctionIsIgnored() {
        PartialConnectionTaskUpdateHandler testInstance = this.testInstance();
        PartialConnectionTaskUpdateDetails partialConnectionTaskUpdateDetails = mock(PartialConnectionTaskUpdateDetails.class);
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        when(partialConnectionTask.getConnectionFunction()).thenReturn(Optional.empty());
        when(partialConnectionTaskUpdateDetails.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(partialConnectionTaskUpdateDetails.getPreviousConnectionFunction()).thenReturn(Optional.empty());
        when(partialConnectionTaskUpdateDetails.getAddedOrRemovedRequiredProperties()).thenReturn(Collections.emptyList());

        when(localEvent.getSource()).thenReturn(partialConnectionTaskUpdateDetails);

        // Business method
        testInstance.onEvent(this.localEvent);

        // Asserts
        verifyNoMoreInteractions(this.messageService);
    }

    @Test
    public void eventWithoutAddedOrRemovedRequiredPropertiesAndWithSameConnectionFunctionIsIgnored() {
        PartialConnectionTaskUpdateHandler testInstance = this.testInstance();
        ConnectionFunction connectionFunction = mock(ConnectionFunction.class);
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        when(partialConnectionTask.getConnectionFunction()).thenReturn(Optional.of(connectionFunction));
        PartialConnectionTaskUpdateDetails partialConnectionTaskUpdateDetails = mockPartialConnectionTaskUpdateDetails(partialConnectionTask, Optional.of(connectionFunction), Collections.emptyList());

        when(localEvent.getSource()).thenReturn(partialConnectionTaskUpdateDetails);

        // Business method
        testInstance.onEvent(this.localEvent);

        // Asserts
        verifyNoMoreInteractions(this.messageService);
    }

    @Test
    public void handlerPostsAsyncMessageWhenHavingAddedOrRemovedRequiredProperties() {
        PartialConnectionTaskUpdateHandler testInstance = this.testInstance();
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        when(partialConnectionTask.getId()).thenReturn(PARTIAL_CONNECTION_TASK_ID);
        ConnectionFunction connectionFunction = mock(ConnectionFunction.class);
        when(partialConnectionTask.getConnectionFunction()).thenReturn(Optional.of(connectionFunction));

        PartialConnectionTaskUpdateDetails updateDetails = mockPartialConnectionTaskUpdateDetails(partialConnectionTask, Optional.of(connectionFunction), Arrays.asList("prop1", "prop2"));
        when(this.localEvent.getSource()).thenReturn(updateDetails);

        MessageBuilder messageBuilder = mock(MessageBuilder.class);
        DestinationSpec destinationSpec = mock(DestinationSpec.class);
        when(destinationSpec.message(anyString())).thenReturn(messageBuilder);
        when(this.messageService.getDestinationSpec(ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory.TASK_DESTINATION)).thenReturn(Optional.of(destinationSpec));
        when(this.messageService.getDestinationSpec(ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandlerFactory.TASK_DESTINATION)).thenReturn(Optional.of(destinationSpec));

        // Business method
        testInstance.onEvent(this.localEvent);

        // Asserts
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(destinationSpec).message(messageCaptor.capture());
        String message = messageCaptor.getValue();
        assertThat(message).contains(StartConnectionTasksRevalidationAfterPropertyRemoval.class.getSimpleName());
        assertThat(message).contains(String.valueOf(PARTIAL_CONNECTION_TASK_ID));
        verify(messageBuilder).send();
    }

    @Test
    public void handlerPostsAsyncMessageWhenConnectionFunctionHasBeenAdded() {
        PartialConnectionTaskUpdateHandler testInstance = this.testInstance();
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        when(partialConnectionTask.getId()).thenReturn(PARTIAL_CONNECTION_TASK_ID);
        ConnectionFunction connectionFunction = mock(ConnectionFunction.class);
        when(connectionFunction.getId()).thenReturn(CONNECTION_FUNCTION_ID);
        when(partialConnectionTask.getConnectionFunction()).thenReturn(Optional.of(connectionFunction));

        PartialConnectionTaskUpdateDetails updateDetails = mockPartialConnectionTaskUpdateDetails(partialConnectionTask, Optional.empty(), Collections.emptyList());
        when(this.localEvent.getSource()).thenReturn(updateDetails);

        MessageBuilder messageBuilder = mock(MessageBuilder.class);
        DestinationSpec destinationSpec = mock(DestinationSpec.class);
        when(destinationSpec.message(anyString())).thenReturn(messageBuilder);
        when(this.messageService.getDestinationSpec(ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory.TASK_DESTINATION)).thenReturn(Optional.of(destinationSpec));
        when(this.messageService.getDestinationSpec(ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandlerFactory.TASK_DESTINATION)).thenReturn(Optional.of(destinationSpec));

        // Business method
        testInstance.onEvent(this.localEvent);

        // Asserts
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(destinationSpec).message(messageCaptor.capture());
        String message = messageCaptor.getValue();
        assertThat(message).contains(StartConnectionTasksRevalidationAfterConnectionFunctionModification.class.getSimpleName());
        assertThat(message).contains(String.valueOf(PARTIAL_CONNECTION_TASK_ID) + PARTIAL_CONNECTION_TASK_CONNECTION_FUNCTION_DELIMITER + String.valueOf(0L));
        verify(messageBuilder).send();
    }

    @Test
    public void handlerPostsAsyncMessageWhenConnectionFunctionHasBeenRemoved() {
        PartialConnectionTaskUpdateHandler testInstance = this.testInstance();
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        when(partialConnectionTask.getId()).thenReturn(PARTIAL_CONNECTION_TASK_ID);
        ConnectionFunction connectionFunction = mock(ConnectionFunction.class);
        when(connectionFunction.getId()).thenReturn(CONNECTION_FUNCTION_ID);
        when(partialConnectionTask.getConnectionFunction()).thenReturn(Optional.empty());

        PartialConnectionTaskUpdateDetails updateDetails = mockPartialConnectionTaskUpdateDetails(partialConnectionTask, Optional.of(connectionFunction), Collections.emptyList());
        when(this.localEvent.getSource()).thenReturn(updateDetails);

        MessageBuilder messageBuilder = mock(MessageBuilder.class);
        DestinationSpec destinationSpec = mock(DestinationSpec.class);
        when(destinationSpec.message(anyString())).thenReturn(messageBuilder);
        when(this.messageService.getDestinationSpec(ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory.TASK_DESTINATION)).thenReturn(Optional.of(destinationSpec));
        when(this.messageService.getDestinationSpec(ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandlerFactory.TASK_DESTINATION)).thenReturn(Optional.of(destinationSpec));

        // Business method
        testInstance.onEvent(this.localEvent);

        // Asserts
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(destinationSpec).message(messageCaptor.capture());
        String message = messageCaptor.getValue();
        assertThat(message).contains(StartConnectionTasksRevalidationAfterConnectionFunctionModification.class.getSimpleName());
        assertThat(message).contains(String.valueOf(PARTIAL_CONNECTION_TASK_ID) + PARTIAL_CONNECTION_TASK_CONNECTION_FUNCTION_DELIMITER + String.valueOf(CONNECTION_FUNCTION_ID));
        verify(messageBuilder).send();
    }

    @Test
    public void handlerPostsAsyncMessageWhenConnectionFunctionHasBeenModified() {
        PartialConnectionTaskUpdateHandler testInstance = this.testInstance();
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        when(partialConnectionTask.getId()).thenReturn(PARTIAL_CONNECTION_TASK_ID);
        ConnectionFunction connectionFunctionA = mock(ConnectionFunction.class);
        when(connectionFunctionA.getId()).thenReturn(CONNECTION_FUNCTION_ID);
        ConnectionFunction connectionFunctionB = mock(ConnectionFunction.class);
        when(connectionFunctionB.getId()).thenReturn(123L);
        when(partialConnectionTask.getConnectionFunction()).thenReturn(Optional.of(connectionFunctionA));

        PartialConnectionTaskUpdateDetails updateDetails = mockPartialConnectionTaskUpdateDetails(partialConnectionTask, Optional.of(connectionFunctionB), Collections.emptyList());
        when(this.localEvent.getSource()).thenReturn(updateDetails);

        MessageBuilder messageBuilder = mock(MessageBuilder.class);
        DestinationSpec destinationSpec = mock(DestinationSpec.class);
        when(destinationSpec.message(anyString())).thenReturn(messageBuilder);
        when(this.messageService.getDestinationSpec(ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory.TASK_DESTINATION)).thenReturn(Optional.of(destinationSpec));
        when(this.messageService.getDestinationSpec(ConnectionTaskValidatorAfterConnectionFunctionModificationMessageHandlerFactory.TASK_DESTINATION)).thenReturn(Optional.of(destinationSpec));

        // Business method
        testInstance.onEvent(this.localEvent);

        // Asserts
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(destinationSpec).message(messageCaptor.capture());
        String message = messageCaptor.getValue();
        assertThat(message).contains(StartConnectionTasksRevalidationAfterConnectionFunctionModification.class.getSimpleName());
        assertThat(message).contains(String.valueOf(PARTIAL_CONNECTION_TASK_ID) + PARTIAL_CONNECTION_TASK_CONNECTION_FUNCTION_DELIMITER + String.valueOf(123L));
        verify(messageBuilder).send();
    }

    private PartialConnectionTaskUpdateDetails mockPartialConnectionTaskUpdateDetails(PartialConnectionTask partialConnectionTask, Optional<ConnectionFunction> previousConnectionFunction, List<String> addedOrRemovedRequiredProperties) {
        PartialConnectionTaskUpdateDetails updateDetails = mock(PartialConnectionTaskUpdateDetails.class);
        when(updateDetails.getId()).thenReturn(PARTIAL_CONNECTION_TASK_ID);
        when(updateDetails.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(updateDetails.getPreviousConnectionFunction()).thenReturn(previousConnectionFunction);
        when(updateDetails.getAddedOrRemovedRequiredProperties()).thenReturn(addedOrRemovedRequiredProperties);
        return updateDetails;
    }

    private PartialConnectionTaskUpdateHandler testInstance() {
        return new PartialConnectionTaskUpdateHandler(this.messageService);
    }

}