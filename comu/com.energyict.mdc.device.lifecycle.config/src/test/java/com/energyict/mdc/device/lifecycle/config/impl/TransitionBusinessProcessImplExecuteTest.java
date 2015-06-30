package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.TransitionBusinessProcess;

import com.elster.jupiter.bpm.BpmService;
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
 * Test the execute methods of the {@link TransitionBusinessProcessImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-30 (13:18)
 */
@RunWith(MockitoJUnitRunner.class)
public class TransitionBusinessProcessImplExecuteTest {

    private static final long DEVICE_ID = 97L;
    private static final long STATE_ID = 101L;
    private static final String DEPLOYMENT_ID = "deploymentId";
    private static final String PROCESS_ID = "processId";

    @Mock
    private BpmService bpmService;
    @Mock
    private State state;

    @Before
    public void initializeMocks() {
        when(this.state.getId()).thenReturn(STATE_ID);
    }
    @Test
    public void createWithoutConstraint() {
        TransitionBusinessProcess testInstance = this.getTestInstance();

        // Business method
        testInstance.executeOn(DEVICE_ID, this.state);

        // Asserts
        ArgumentCaptor<Map> parametersArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(this.bpmService).startProcess(eq(DEPLOYMENT_ID), eq(PROCESS_ID), parametersArgumentCaptor.capture());
        Map<String, Object> parameters = parametersArgumentCaptor.getValue();
        assertThat(parameters).containsOnlyKeys(TransitionBusinessProcess.DEVICE_ID_BPM_PARAMETER_NAME, TransitionBusinessProcess.STATE_ID_BPM_PARAMETER_NAME);
        assertThat(parameters.get(TransitionBusinessProcess.DEVICE_ID_BPM_PARAMETER_NAME)).isEqualTo(DEVICE_ID);
        assertThat(parameters.get(TransitionBusinessProcess.STATE_ID_BPM_PARAMETER_NAME)).isEqualTo(STATE_ID);
    }

    private TransitionBusinessProcessImpl getTestInstance() {
        TransitionBusinessProcessImpl businessProcess = new TransitionBusinessProcessImpl(this.bpmService);
        businessProcess.initialize(DEPLOYMENT_ID, PROCESS_ID);
        return businessProcess;
    }

}