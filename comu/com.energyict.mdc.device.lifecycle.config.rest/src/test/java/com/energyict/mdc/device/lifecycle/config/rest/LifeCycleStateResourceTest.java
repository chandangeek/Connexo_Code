package com.energyict.mdc.device.lifecycle.config.rest;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.jayway.jsonpath.JsonModel;
import org.junit.Test;
import org.mockito.Matchers;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LifeCycleStateResourceTest extends DeviceLifecycleConfigApplicationJerseyTest {

    @Test
    public void testLifeCycleStateJsonModel(){
        List<State> states = mockDefaultStates();
        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        when(stateMachine.getStates()).thenReturn(states);
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(dlc.getFiniteStateMachine()).thenReturn(stateMachine);
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(Matchers.anyLong())).thenReturn(Optional.of(dlc));

        String stringResponse = target("/devicelifecycles/1/states").request().get(String.class);
        JsonModel model = JsonModel.create(stringResponse);
        assertThat(model.<Number>get("$.total")).isEqualTo(3);
        assertThat(model.<List<?>>get("$.deviceLifeCycleStates")).isNotNull();
        assertThat(model.<List<?>>get("$.deviceLifeCycleStates")).hasSize(3);
        assertThat(model.<Number>get("$.deviceLifeCycleStates[0].id")).isEqualTo(1);
        assertThat(model.<String>get("$.deviceLifeCycleStates[0].name")).isEqualTo("Commisioned");
        assertThat(model.<Number>get("$.deviceLifeCycleStates[2].id")).isEqualTo(3);
        assertThat(model.<String>get("$.deviceLifeCycleStates[2].name")).isEqualTo("In stock");
    }

    @Test
    public void testEmptyLifeCycleList(){
        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        when(stateMachine.getStates()).thenReturn(Collections.emptyList());
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(dlc.getFiniteStateMachine()).thenReturn(stateMachine);
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(Matchers.anyLong())).thenReturn(Optional.of(dlc));

        String stringResponse = target("/devicelifecycles/1/states").request().get(String.class);
        JsonModel model = JsonModel.create(stringResponse);

        assertThat(model.<Number>get("$.total")).isEqualTo(0);
        assertThat(model.<List<?>>get("$.deviceLifeCycleStates")).isNotNull();
        assertThat(model.<List<?>>get("$.deviceLifeCycleStates")).isEmpty();
    }

    @Test
    public void testGetLifeCycleStateById(){
        List<State> states = mockDefaultStates();
        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        when(stateMachine.getStates()).thenReturn(states);
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(dlc.getFiniteStateMachine()).thenReturn(stateMachine);
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(Matchers.anyLong())).thenReturn(Optional.of(dlc));

        String stringResponse = target("/devicelifecycles/1/states/3").request().get(String.class);
        JsonModel model = JsonModel.create(stringResponse);
        assertThat(model.<Number>get("$.id")).isEqualTo(3);
        assertThat(model.<String>get("$.name")).isEqualTo("In stock");
    }

    @Test
    public void testGetUnexistedLifeCycleState(){
        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        when(stateMachine.getStates()).thenReturn(Collections.<State>emptyList());
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(dlc.getFiniteStateMachine()).thenReturn(stateMachine);
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(Matchers.anyLong())).thenReturn(Optional.of(dlc));

        Response response = target("/devicelifecycles/1/states/3").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }
}
