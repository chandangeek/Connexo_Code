package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.config.events.PartialConnectionTaskUpdateDetails;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.MessageService;
import com.google.common.collect.ImmutableMap;
import org.osgi.service.event.Event;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.energyict.mdc.device.data.impl.events.ConnectionTaskValidatorAfterPropertyRemovalMessageHandlerFactory.TASK_DESTINATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
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
    public void eventWithoutPropertiesIsIgnored() {
        PartialConnectionTaskUpdateHandler testInstance = this.testInstance();
        Event osgiEvent = spy(new Event(TOPIC, ImmutableMap.of("addedOrRemovedRequiredProperties", "")));
        when(this.localEvent.toOsgiEvent()).thenReturn(osgiEvent);

        // Business method
        testInstance.onEvent(this.localEvent);

        // Asserts
        verifyNoMoreInteractions(this.messageService);
    }

    @Test
    public void eventWithOtherPropertiesIsIgnored() {
        PartialConnectionTaskUpdateHandler testInstance = this.testInstance();
        Event osgiEvent = spy(new Event(TOPIC, ImmutableMap.of("numerical", BigDecimal.TEN)));
        when(this.localEvent.toOsgiEvent()).thenReturn(osgiEvent);

        // Business method
        testInstance.onEvent(this.localEvent);

        // Asserts
        verifyNoMoreInteractions(this.messageService);
    }

    @Test
    public void handlerPostsAsyncMessage() {
        PartialConnectionTaskUpdateHandler testInstance = this.testInstance();
        Event osgiEvent = new Event(TOPIC, ImmutableMap.of("addedOrRemovedRequiredProperties", "prop1,prop2"));
        when(this.localEvent.toOsgiEvent()).thenReturn(osgiEvent);
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        when(partialConnectionTask.getId()).thenReturn(PARTIAL_CONNECTION_TASK_ID);
        PartialConnectionTaskUpdateDetails updateDetails = mock(PartialConnectionTaskUpdateDetails.class);
        when(updateDetails.getId()).thenReturn(PARTIAL_CONNECTION_TASK_ID);
        when(updateDetails.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(this.localEvent.getSource()).thenReturn(updateDetails);
        MessageBuilder messageBuilder = mock(MessageBuilder.class);
        DestinationSpec destinationSpec = mock(DestinationSpec.class);
        when(destinationSpec.message(anyString())).thenReturn(messageBuilder);
        when(this.messageService.getDestinationSpec(TASK_DESTINATION)).thenReturn(Optional.of(destinationSpec));

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

    private PartialConnectionTaskUpdateHandler testInstance() {
        return new PartialConnectionTaskUpdateHandler(this.messageService);
    }

}