package com.energyict.mdc.device.lifecycle.config.bpmhandler;

import com.energyict.mdc.device.lifecycle.config.TransitionBusinessProcess;
import com.energyict.mdc.device.lifecycle.config.TransitionBusinessProcessStartEvent;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.fsm.State;

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
 * Tests the {@link TransitionBusinessProcessStartEventTopicHandler} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class TransitionBusinessProcessStartEventTopicHandlerTest {

    private static final long STATE_ID = 97L;
    public static final String DEPLOYMENT_ID = "DEPL_ID";
    public static final String PROCESS_ID = "PROC_ID";
    public static final long DEVICE_ID = 101L;

    @Mock
    private BpmService bpmService;
    @Mock
    private LocalEvent localEvent;
    @Mock
    private TransitionBusinessProcessStartEvent event;
    @Mock
    private State state;

    @Before
    public void initializeMocks() {
        when(this.localEvent.getSource()).thenReturn(this.event);
        when(this.event.state()).thenReturn(this.state);
        when(this.state.getId()).thenReturn(STATE_ID);
        when(this.event.deploymentId()).thenReturn(DEPLOYMENT_ID);
        when(this.event.processId()).thenReturn(PROCESS_ID);
        when(this.event.deviceId()).thenReturn(DEVICE_ID);
    }

    @Test
    public void topicMatcherIsNotNull() {
        TransitionBusinessProcessStartEventTopicHandler topicHandler = this.getTestInstance();

        // Business method
        String topicMatcher = topicHandler.getTopicMatcher();

        // Asserts
        assertThat(topicMatcher).isNotNull();
    }

    @Test
    public void handlerExtractsInformationFromEvent() {
        TransitionBusinessProcessStartEventTopicHandler topicHandler = this.getTestInstance();

        // Business method
        topicHandler.handle(this.localEvent);

        // Asserts
        verify(this.localEvent).getSource();
        verify(this.event).deploymentId();
        verify(this.event).processId();
        verify(this.event).deviceId();
        verify(this.event).state();
    }

    @Test
    public void handlerDelegatesToBpmService() {
        TransitionBusinessProcessStartEventTopicHandler topicHandler = this.getTestInstance();

        // Business method
        topicHandler.handle(this.localEvent);

        // Asserts
        ArgumentCaptor<Map> parameterArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(this.bpmService).startProcess(eq(DEPLOYMENT_ID), eq(PROCESS_ID), parameterArgumentCaptor.capture());
        Map<String, Object> parameters = parameterArgumentCaptor.getValue();
        assertThat(parameters).containsKeys(
                TransitionBusinessProcess.DEVICE_ID_BPM_PARAMETER_NAME,
                TransitionBusinessProcess.STATE_ID_BPM_PARAMETER_NAME);
        assertThat(parameters.get(TransitionBusinessProcess.DEVICE_ID_BPM_PARAMETER_NAME)).isEqualTo(DEVICE_ID);
        assertThat(parameters.get(TransitionBusinessProcess.STATE_ID_BPM_PARAMETER_NAME)).isEqualTo(this.state.getId());
    }

    private TransitionBusinessProcessStartEventTopicHandler getTestInstance() {
        return new TransitionBusinessProcessStartEventTopicHandler(this.bpmService);
    }

}