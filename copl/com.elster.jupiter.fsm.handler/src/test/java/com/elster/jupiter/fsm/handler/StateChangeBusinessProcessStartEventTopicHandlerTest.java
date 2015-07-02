package com.elster.jupiter.fsm.handler;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateChangeBusinessProcess;
import com.elster.jupiter.fsm.StateChangeBusinessProcessStartEvent;

import java.util.Map;

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
 * Tests the {@link StateChangeBusinessProcessStartEventTopicHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-02 (11:36)
 */
@RunWith(MockitoJUnitRunner.class)
public class StateChangeBusinessProcessStartEventTopicHandlerTest {

    private static final long STATE_ID = 97L;
    public static final String DEPLOYMENT_ID = "DEPL_ID";
    public static final String PROCESS_ID = "PROC_ID";
    public static final String SOURCE_ID = "SRC_ID";

    @Mock
    private BpmService bpmService;
    @Mock
    private LocalEvent localEvent;
    @Mock
    private StateChangeBusinessProcessStartEvent event;
    @Mock
    private State state;

    @Before
    public void initializeMocks() {
        when(this.localEvent.getSource()).thenReturn(this.event);
        when(this.event.state()).thenReturn(this.state);
        when(this.state.getId()).thenReturn(STATE_ID);
        when(this.event.deploymentId()).thenReturn(DEPLOYMENT_ID);
        when(this.event.processId()).thenReturn(PROCESS_ID);
        when(this.event.sourceId()).thenReturn(SOURCE_ID);
        when(this.event.type()).thenReturn(StateChangeBusinessProcessStartEvent.Type.ENTRY);
    }

    @Test
    public void topicMatcherIsNotNull() {
        StateChangeBusinessProcessStartEventTopicHandler topicHandler = this.getTestInstance();

        // Business method
        String topicMatcher = topicHandler.getTopicMatcher();

        // Asserts
        assertThat(topicMatcher).isNotNull();
    }

    @Test
    public void handlerExtractsInformationFromEvent() {
        StateChangeBusinessProcessStartEventTopicHandler topicHandler = this.getTestInstance();

        // Business method
        topicHandler.handle(this.localEvent);

        // Asserts
        verify(this.localEvent).getSource();
        verify(this.event).deploymentId();
        verify(this.event).processId();
        verify(this.event).sourceId();
        verify(this.event).state();
        verify(this.event).type();
    }

    @Test
    public void handlerDelegatesToBpmService() {
        StateChangeBusinessProcessStartEventTopicHandler topicHandler = this.getTestInstance();

        // Business method
        topicHandler.handle(this.localEvent);

        // Asserts
        ArgumentCaptor<Map> parameterArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(this.bpmService).startProcess(eq(DEPLOYMENT_ID), eq(PROCESS_ID), parameterArgumentCaptor.capture());
        Map<String, Object> parameters = parameterArgumentCaptor.getValue();
        assertThat(parameters).containsKeys(
                StateChangeBusinessProcess.SOURCE_ID_BPM_PARAMETER_NAME,
                StateChangeBusinessProcess.STATE_ID_BPM_PARAMETER_NAME,
                StateChangeBusinessProcess.CHANGE_TYPE_BPM_PARAMETER_NAME);
        assertThat(parameters.get(StateChangeBusinessProcess.SOURCE_ID_BPM_PARAMETER_NAME)).isEqualTo(SOURCE_ID);
        assertThat(parameters.get(StateChangeBusinessProcess.STATE_ID_BPM_PARAMETER_NAME)).isEqualTo(this.state.getId());
        assertThat(parameters.get(StateChangeBusinessProcess.CHANGE_TYPE_BPM_PARAMETER_NAME)).isEqualTo(StateChangeBusinessProcessStartEvent.Type.ENTRY.parameterValue());
    }

    private StateChangeBusinessProcessStartEventTopicHandler getTestInstance() {
        return new StateChangeBusinessProcessStartEventTopicHandler(this.bpmService);
    }

}