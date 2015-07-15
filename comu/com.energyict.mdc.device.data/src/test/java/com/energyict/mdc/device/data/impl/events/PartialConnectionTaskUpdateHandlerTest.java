package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.impl.EventType;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.LocalEvent;
import com.google.common.collect.ImmutableMap;
import org.osgi.service.event.Event;

import java.math.BigDecimal;

import org.junit.*;
import org.junit.runner.*;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
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

    public static final long PARTIAL_CONNECTION_TASK_ID = 97L;
    @Mock
    private EventService eventService;
    @Mock
    private LocalEvent localEvent;
    @Mock
    private com.elster.jupiter.events.EventType eventType;

    @Before
    public void initializeMocks() {
        when(this.localEvent.getType()).thenReturn(this.eventType);
        when(this.eventType.getTopic()).thenReturn("event.type.topic");
    }

    @Test
    public void handlerGetsTheEventType() {
        PartialConnectionTaskUpdateHandler testInstance = this.testInstance();

        // Business method
        testInstance.onEvent(this.localEvent);

        // Asserts
        verify(this.localEvent).getType();
    }

    @Test
    public void eventThatDoesNotMatchTheEventIsIgnored() {
        PartialConnectionTaskUpdateHandler testInstance = this.testInstance();

        // Business method
        testInstance.onEvent(this.localEvent);

        // Asserts
        verifyNoMoreInteractions(this.eventService);
    }

    @Test
    public void eventWithoutPropertiesIsIgnored() {
        PartialConnectionTaskUpdateHandler testInstance = this.testInstance();
        String topic = "com/energyict/mdc/device/config/partialscheduledconnectiontask/UPDATE";
        when(this.eventType.getTopic()).thenReturn(topic);
        Event osgiEvent = spy(new Event(topic, ImmutableMap.of("removedRequiredProperties", "")));
        when(this.localEvent.toOsgiEvent()).thenReturn(osgiEvent);

        // Business method
        testInstance.onEvent(this.localEvent);

        // Asserts
        verifyNoMoreInteractions(this.eventService);
    }

    @Test
    public void eventWithOtherPropertiesIsIgnored() {
        PartialConnectionTaskUpdateHandler testInstance = this.testInstance();
        String topic = "com/energyict/mdc/device/config/partialscheduledconnectiontask/UPDATE";
        when(this.eventType.getTopic()).thenReturn(topic);
        Event osgiEvent = spy(new Event(topic, ImmutableMap.of("numerical", BigDecimal.TEN)));
        when(this.localEvent.toOsgiEvent()).thenReturn(osgiEvent);

        // Business method
        testInstance.onEvent(this.localEvent);

        // Asserts
        verifyNoMoreInteractions(this.eventService);
    }

    @Test
    public void handlerPostsAsyncMessage() {
        PartialConnectionTaskUpdateHandler testInstance = this.testInstance();
        String topic = "com/energyict/mdc/device/config/partialscheduledconnectiontask/UPDATE";
        when(this.eventType.getTopic()).thenReturn(topic);
        Event osgiEvent = new Event(topic, ImmutableMap.of("removedRequiredProperties", "prop1,prop2"));
        when(this.localEvent.toOsgiEvent()).thenReturn(osgiEvent);
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        when(partialConnectionTask.getId()).thenReturn(PARTIAL_CONNECTION_TASK_ID);
        when(this.localEvent.getSource()).thenReturn(partialConnectionTask);

        // Business method
        testInstance.onEvent(this.localEvent);

        // Asserts
        ArgumentCaptor<RevalidateAllConnectionTasksAfterPropertyRemoval> messageCaptor = ArgumentCaptor.forClass(RevalidateAllConnectionTasksAfterPropertyRemoval.class);
        verify(this.eventService).postEvent(eq(EventType.CONNECTIONTASK_CHECK_ALL_ACTIVE.topic()), messageCaptor.capture());
        RevalidateAllConnectionTasksAfterPropertyRemoval message = messageCaptor.getValue();
        assertThat(message.getPartialConnectionTaskId()).isEqualTo(PARTIAL_CONNECTION_TASK_ID);
    }

    private PartialConnectionTaskUpdateHandler testInstance() {
        return new PartialConnectionTaskUpdateHandler(this.eventService);
    }

}