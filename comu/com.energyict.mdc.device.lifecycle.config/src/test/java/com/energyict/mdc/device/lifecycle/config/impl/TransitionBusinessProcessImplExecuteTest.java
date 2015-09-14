package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.TransitionBusinessProcess;
import com.energyict.mdc.device.lifecycle.config.TransitionBusinessProcessStartEvent;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.State;
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
 * Test the execute methods of the {@link TransitionBusinessProcessImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-30 (13:18)
 */
@RunWith(MockitoJUnitRunner.class)
public class TransitionBusinessProcessImplExecuteTest {

    private static final long DEVICE_ID = 97L;
    private static final long STATE_ID = 101L;
    private static final String NAME = "name";
    private static final String DEPLOYMENT_ID = "deploymentId";
    private static final String PROCESS_ID = "processId";

    @Mock
    private DataModel dataModel;
    @Mock
    private EventService eventService;
    @Mock
    private State state;

    @Before
    public void initializeMocks() {
        when(this.dataModel.getInstance(TransitionBusinessProcessStartEventImpl.class)).thenReturn(new TransitionBusinessProcessStartEventImpl(this.eventService));
        when(this.state.getId()).thenReturn(STATE_ID);
    }

    @Test
    public void executePublishesEventToEventService() {
        TransitionBusinessProcess testInstance = this.getTestInstance();

        // Business method
        testInstance.executeOn(DEVICE_ID, this.state);

        // Asserts
        ArgumentCaptor<TransitionBusinessProcessStartEvent> eventArgumentCaptor = ArgumentCaptor.forClass(TransitionBusinessProcessStartEvent.class);
        verify(this.eventService).postEvent(eq(TransitionBusinessProcessStartEvent.TOPIC), eventArgumentCaptor.capture());
        TransitionBusinessProcessStartEvent event = eventArgumentCaptor.getValue();
        assertThat(event.deploymentId()).isEqualTo(DEPLOYMENT_ID);
        assertThat(event.processId()).isEqualTo(PROCESS_ID);
        assertThat(event.deviceId()).isEqualTo(DEVICE_ID);
        assertThat(event.state()).isEqualTo(this.state);
    }

    private TransitionBusinessProcessImpl getTestInstance() {
        TransitionBusinessProcessImpl businessProcess = new TransitionBusinessProcessImpl(dataModel);
        businessProcess.initialize(NAME, DEPLOYMENT_ID, PROCESS_ID);
        return businessProcess;
    }

}