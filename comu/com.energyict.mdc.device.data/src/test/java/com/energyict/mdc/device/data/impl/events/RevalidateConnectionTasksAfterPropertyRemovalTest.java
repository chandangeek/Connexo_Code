package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTask;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.MessageService;

import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.energyict.mdc.device.data.impl.events.ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory.TASK_DESTINATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link RevalidateConnectionTasksAfterPropertyRemoval} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-16 (14:47)
 */
@RunWith(MockitoJUnitRunner.class)
public class RevalidateConnectionTasksAfterPropertyRemovalTest {

    private static final String CONNECTION_TASK_IDS = "1,2,3";

    @Mock
    private MessageService messageService;
    @Mock
    private ConnectionTaskService connectionTaskService;

    @Test
    public void publishUsesTheCorrectDestinationSpec() {
        RevalidateConnectionTasksAfterPropertyRemoval testInstance = this.publishTestInstance();
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
        RevalidateConnectionTasksAfterPropertyRemoval testInstance = this.publishTestInstance();
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
        assertThat(message).contains(RevalidateConnectionTasksAfterPropertyRemoval.class.getSimpleName());
    }

    @Test
    public void payloadIncludesPartialConnectionTaskIdForTheMessageParser() {
        RevalidateConnectionTasksAfterPropertyRemoval testInstance = this.publishTestInstance();
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
        assertThat(message).contains(CONNECTION_TASK_IDS);
    }

    @Test
    public void processFindsAllConnectionTasks() {
        RevalidateConnectionTasksAfterPropertyRemoval testInstance = this.processTestInstance();
        when(this.connectionTaskService.findConnectionTask(anyInt())).thenReturn(Optional.empty());

        // Business method
        testInstance.process();

        // Asserts
        verify(this.connectionTaskService).findConnectionTask(1);
        verify(this.connectionTaskService).findConnectionTask(2);
        verify(this.connectionTaskService).findConnectionTask(3);
    }

    @Test
    public void processRevalidatesAllConnectionTasks() {
        RevalidateConnectionTasksAfterPropertyRemoval testInstance = this.processTestInstance();
        ServerConnectionTask ct1 = mock(ServerConnectionTask.class);
        ServerConnectionTask ct2 = mock(ServerConnectionTask.class);
        ServerConnectionTask ct3 = mock(ServerConnectionTask.class);
        when(this.connectionTaskService.findConnectionTask(1)).thenReturn(Optional.of(ct1));
        when(this.connectionTaskService.findConnectionTask(2)).thenReturn(Optional.of(ct2));
        when(this.connectionTaskService.findConnectionTask(3)).thenReturn(Optional.of(ct3));

        // Business method
        testInstance.process();

        // Asserts
        verify(ct1).revalidatePropertiesAndAdjustStatus();
        verify(ct2).revalidatePropertiesAndAdjustStatus();
        verify(ct3).revalidatePropertiesAndAdjustStatus();
    }

    private RevalidateConnectionTasksAfterPropertyRemoval publishTestInstance() {
        return RevalidateConnectionTasksAfterPropertyRemoval.forPublishing(this.messageService).with(String.valueOf(CONNECTION_TASK_IDS));
    }

    private RevalidateConnectionTasksAfterPropertyRemoval processTestInstance() {
        return new RevalidateConnectionTasksAfterPropertyRemoval(this.messageService, this.connectionTaskService).with(CONNECTION_TASK_IDS);
    }

}