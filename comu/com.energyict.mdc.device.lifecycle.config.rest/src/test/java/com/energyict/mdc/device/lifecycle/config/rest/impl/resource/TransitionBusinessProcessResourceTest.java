package com.energyict.mdc.device.lifecycle.config.rest.impl.resource;

import com.elster.jupiter.fsm.*;
import com.energyict.mdc.device.lifecycle.config.rest.DeviceLifeCycleConfigApplicationJerseyTest;
import com.jayway.jsonpath.JsonModel;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 10/07/2015
 * Time: 10:29
 */
public class TransitionBusinessProcessResourceTest extends DeviceLifeCycleConfigApplicationJerseyTest {

    @Test
    public void testTransitionBusinessProcessJsonModel(){
        List<StateChangeBusinessProcess> processes = mockProcesses();
        when(finiteStateMachineService.findStateChangeBusinessProcesses()).thenReturn(processes);

        String stringResponse = target("/devicelifecycles/statechangebusinessprocesses").request().get(String.class);
        JsonModel model = JsonModel.create(stringResponse);
        assertThat(model.<Number>get("$.total")).isEqualTo(3);
        assertThat(model.<List<?>>get("$.stateChangeBusinessProcesses")).isNotNull();
        assertThat(model.<List<?>>get("$.stateChangeBusinessProcesses")).hasSize(3);
        assertThat(model.<Number>get("$.stateChangeBusinessProcesses[0].id")).isEqualTo(1);
        assertThat(model.<Number>get("$.stateChangeBusinessProcesses[0].name")).isEqualTo("firstName");
        assertThat(model.<String>get("$.stateChangeBusinessProcesses[0].deploymentId")).isEqualTo("deploymentId 1");
        assertThat(model.<Number>get("$.stateChangeBusinessProcesses[0].processId")).isEqualTo("processId 1");
        assertThat(model.<Number>get("$.stateChangeBusinessProcesses[1].id")).isEqualTo(2);
        assertThat(model.<Number>get("$.stateChangeBusinessProcesses[1].name")).isEqualTo("secondName");
        assertThat(model.<String>get("$.stateChangeBusinessProcesses[1].deploymentId")).isEqualTo("deploymentId 2");
        assertThat(model.<Number>get("$.stateChangeBusinessProcesses[1].processId")).isEqualTo("processId 2");
        assertThat(model.<Number>get("$.stateChangeBusinessProcesses[2].id")).isEqualTo(3);
        assertThat(model.<Number>get("$.stateChangeBusinessProcesses[2].name")).isEqualTo("lastName");
        assertThat(model.<String>get("$.stateChangeBusinessProcesses[2].deploymentId")).isEqualTo("deploymentId 3");
        assertThat(model.<Number>get("$.stateChangeBusinessProcesses[2].processId")).isEqualTo("processId 3");
    }

    private List<StateChangeBusinessProcess> mockProcesses(){
        List<StateChangeBusinessProcess> processes = new ArrayList<>(3);
        processes.add(mockStateChangeBusinessProcess(1L, "firstName", "deploymentId 1", "processId 1"));
        processes.add(mockStateChangeBusinessProcess(2L, "secondName","deploymentId 2", "processId 2"));
        processes.add(mockStateChangeBusinessProcess(3L, "lastName", "deploymentId 3", "processId 3"));
        return processes;
    }
}
