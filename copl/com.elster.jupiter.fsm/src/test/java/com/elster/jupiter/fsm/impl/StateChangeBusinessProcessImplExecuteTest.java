package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateChangeBusinessProcess;

import java.util.Map;

import org.junit.*;
import org.junit.runner.*;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Tests the execute methods of the {@link StateChangeBusinessProcessImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-30 (08:24)
 */
@RunWith(MockitoJUnitRunner.class)
public class StateChangeBusinessProcessImplExecuteTest {

    private static final String DEPLOYMENT_ID = "com.elster.jupiter.fsm.impl.test";
    private static final String PROCESS_ID = "StateChangeBusinessProcessImplExecuteTest";

    @Mock
    private BpmService bpmService;
    @Mock
    private State state;

    @Test
    public void executeOnEntryDelegatesCorrectlyToBpmService() {
        StateChangeBusinessProcessImpl testInstance = this.getTestInstance();

        // Business method
        testInstance.executeOnEntry("executeOnEntryDelegatesCorrectlyToBpmService", this.state);

        // Asserts
        ArgumentCaptor<Map> parametersArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(this.bpmService).startProcess(eq(DEPLOYMENT_ID), eq(PROCESS_ID), parametersArgumentCaptor.capture());
        Map<String, Object> parameters = parametersArgumentCaptor.getValue();
        assertThat(parameters).containsOnlyKeys(
                StateChangeBusinessProcess.STATE_ID_BPM_PARAMETER_NAME,
                StateChangeBusinessProcess.SOURCE_ID_BPM_PARAMETER_NAME,
                StateChangeBusinessProcess.CHANGE_TYPE_BPM_PARAMETER_NAME);
        assertThat(parameters.get(StateChangeBusinessProcess.CHANGE_TYPE_BPM_PARAMETER_NAME)).isEqualTo("entry");
    }

    @Test
    public void executeOnExitDelegatesCorrectlyToBpmService() {
        StateChangeBusinessProcessImpl testInstance = this.getTestInstance();

        // Business method
        testInstance.executeOnExit("executeOnExitDelegatesCorrectlyToBpmService", this.state);

        // Asserts
        ArgumentCaptor<Map> parametersArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(this.bpmService).startProcess(eq(DEPLOYMENT_ID), eq(PROCESS_ID), parametersArgumentCaptor.capture());
        Map<String, Object> parameters = parametersArgumentCaptor.getValue();
        assertThat(parameters).containsOnlyKeys(
                StateChangeBusinessProcess.STATE_ID_BPM_PARAMETER_NAME,
                StateChangeBusinessProcess.SOURCE_ID_BPM_PARAMETER_NAME,
                StateChangeBusinessProcess.CHANGE_TYPE_BPM_PARAMETER_NAME);
        assertThat(parameters.get(StateChangeBusinessProcess.CHANGE_TYPE_BPM_PARAMETER_NAME)).isEqualTo("exit");
    }

    private StateChangeBusinessProcessImpl getTestInstance() {
        StateChangeBusinessProcessImpl testInstance = new StateChangeBusinessProcessImpl(this.bpmService);
        testInstance.initialize(DEPLOYMENT_ID, PROCESS_ID);
        return testInstance;
    }

}