package com.energyict.mdc.device.data.impl.events;

import com.energyict.mdc.device.data.impl.ServerDeviceDataService;
import com.energyict.mdc.scheduling.model.ComSchedule;

import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Thesaurus;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ComScheduleDeletionHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-03 (14:43)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComScheduleDeletionHandlerTest {

    @Mock
    private ServerDeviceDataService deviceDataService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ComSchedule comSchedule;
    @Mock
    private LocalEvent event;

    private ComScheduleDeletionHandler eventHandler;

    @Before
    public void createEvent () {
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn(ComScheduleDeletionHandler.TOPIC);
        when(this.event.getSource()).thenReturn(this.comSchedule);
        when(this.event.getType()).thenReturn(eventType);
    }

    @Before
    public void createEventHandler () {
        this.eventHandler = new ComScheduleDeletionHandler(this.deviceDataService, this.thesaurus);
    }

    @Test
    public void testNotUsed() {
        when(this.deviceDataService.hasComTaskExecutions(this.comSchedule)).thenReturn(false);

        // Business method
        this.eventHandler.onEvent(this.event);

        // Asserts
        verify(this.event).getSource();
        verify(this.deviceDataService).hasComTaskExecutions(this.comSchedule);
    }

    @Test(expected = VetoDeleteComScheduleException.class)
    public void testInUse() {
        when(this.deviceDataService.hasComTaskExecutions(this.comSchedule)).thenReturn(true);

        // Business method
        this.eventHandler.onEvent(this.event);

        // Asserts
        verify(this.event).getSource();
        verify(this.deviceDataService).hasComTaskExecutions(this.comSchedule);
    }

    @Test
    public void testOtherTopic() {
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn("Wrong topic");
        when(this.event.getSource()).thenReturn(this.comSchedule);
        when(this.event.getType()).thenReturn(eventType);

        // Business method
        this.eventHandler.onEvent(this.event);

        // Asserts
        verify(this.event, never()).getSource();
        verifyZeroInteractions(this.deviceDataService);
    }

}