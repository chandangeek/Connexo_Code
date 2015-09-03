package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateChangeBusinessProcessStartEvent;
import com.elster.jupiter.orm.DataModel;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the execute methods of the {@link StateChangeBusinessProcessImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-30 (08:24)
 */
@RunWith(MockitoJUnitRunner.class)
public class StateChangeBusinessProcessImplExecuteTest {

    private static final String NAME = "Something readable for StateChangeBusinessProcessImplExecuteTest";
    private static final String DEPLOYMENT_ID = "com.elster.jupiter.fsm.impl.test";
    private static final String PROCESS_ID = "StateChangeBusinessProcessImplExecuteTest";

    @Mock
    private DataModel dataModel;
    @Mock
    private EventService eventService;
    @Mock
    private State state;

    @Before
    public void initializeMocks() {
        when(this.dataModel.getInstance(StateChangeBusinessProcessStartEventImpl.class)).thenReturn(new StateChangeBusinessProcessStartEventImpl(this.eventService));
    }

    @Test
    public void executeOnEntryPublishesEventToEventService() {
        StateChangeBusinessProcessImpl testInstance = this.getTestInstance();

        // Business method
        String expectedSourceId = "executeOnEntryPublishesEventToEventService";
        testInstance.executeOnEntry(expectedSourceId, this.state);

        // Asserts
        ArgumentCaptor<StateChangeBusinessProcessStartEvent> eventArgumentCaptor = ArgumentCaptor.forClass(StateChangeBusinessProcessStartEvent.class);
        verify(this.eventService).postEvent(eq(StateChangeBusinessProcessStartEvent.TOPIC), eventArgumentCaptor.capture());
        StateChangeBusinessProcessStartEvent event = eventArgumentCaptor.getValue();
        assertThat(event.deploymentId()).isEqualTo(DEPLOYMENT_ID);
        assertThat(event.processId()).isEqualTo(PROCESS_ID);
        assertThat(event.sourceId()).isEqualTo(expectedSourceId);
        assertThat(event.state()).isEqualTo(this.state);
        assertThat(event.type()).isEqualTo(StateChangeBusinessProcessStartEvent.Type.ENTRY);
    }

    @Test
    public void executeOnExitPublishesEventToEventService() {
        StateChangeBusinessProcessImpl testInstance = this.getTestInstance();

        // Business method
        String expectedSourceId = "executeOnExitPublishesEventToEventService";
        testInstance.executeOnExit(expectedSourceId, this.state);

        // Asserts
        ArgumentCaptor<StateChangeBusinessProcessStartEvent> eventArgumentCaptor = ArgumentCaptor.forClass(StateChangeBusinessProcessStartEvent.class);
        verify(this.eventService).postEvent(eq(StateChangeBusinessProcessStartEvent.TOPIC), eventArgumentCaptor.capture());
        StateChangeBusinessProcessStartEvent event = eventArgumentCaptor.getValue();
        assertThat(event.deploymentId()).isEqualTo(DEPLOYMENT_ID);
        assertThat(event.processId()).isEqualTo(PROCESS_ID);
        assertThat(event.sourceId()).isEqualTo(expectedSourceId);
        assertThat(event.state()).isEqualTo(this.state);
        assertThat(event.type()).isEqualTo(StateChangeBusinessProcessStartEvent.Type.EXIT);
    }

    private StateChangeBusinessProcessImpl getTestInstance() {
        StateChangeBusinessProcessImpl testInstance = new StateChangeBusinessProcessImpl(this.dataModel);
        testInstance.initialize(NAME, DEPLOYMENT_ID, PROCESS_ID);
        return testInstance;
    }

}