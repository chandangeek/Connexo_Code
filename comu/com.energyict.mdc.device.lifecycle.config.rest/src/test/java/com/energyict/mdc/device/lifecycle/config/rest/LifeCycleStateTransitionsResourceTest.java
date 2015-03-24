package com.energyict.mdc.device.lifecycle.config.rest;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.jayway.jsonpath.JsonModel;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LifeCycleStateTransitionsResourceTest extends DeviceLifecycleConfigApplicationJerseyTest {

    @Test
    @Ignore //TODO remove ignore after adding StateTransition name
    public void testLifeCycleTransitionJsonModel(){
        List<StateTransition> transitions = mockDefaultTransitions();
        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        when(stateMachine.getTransitions()).thenReturn(transitions);
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(dlc.getFiniteStateMachine()).thenReturn(stateMachine);
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(Matchers.anyLong())).thenReturn(Optional.of(dlc));

        String stringResponse = target("/devicelifecycles/1/transitions").request().get(String.class);
        JsonModel model = JsonModel.create(stringResponse);
        assertThat(model.<Number>get("$.total")).isEqualTo(2);
        assertThat(model.<List<?>>get("$.deviceLifeCycleTransitions")).isNotNull();
        assertThat(model.<List<?>>get("$.deviceLifeCycleTransitions")).hasSize(2);
        assertThat(model.<Number>get("$.deviceLifeCycleTransitions[0].id")).isEqualTo(1);
        assertThat(model.<String>get("$.deviceLifeCycleTransitions[0].name")).isEqualTo("To decommisioned");
    }

    @Test
    public void testEmptyLifeCycleTransitionsList(){
        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        when(stateMachine.getStates()).thenReturn(Collections.emptyList());
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(dlc.getFiniteStateMachine()).thenReturn(stateMachine);
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(Matchers.anyLong())).thenReturn(Optional.of(dlc));

        String stringResponse = target("/devicelifecycles/1/transitions").request().get(String.class);
        JsonModel model = JsonModel.create(stringResponse);

        assertThat(model.<Number>get("$.total")).isEqualTo(0);
        assertThat(model.<List<?>>get("$.deviceLifeCycleTransitions")).isNotNull();
        assertThat(model.<List<?>>get("$.deviceLifeCycleTransitions")).isEmpty();
    }

    @Test
    @Ignore //TODO remove ignore after adding StateTransition name
    public void testGetLifeCycleTransitionById(){
        List<StateTransition> transitions = mockDefaultTransitions();
        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        when(stateMachine.getTransitions()).thenReturn(transitions);
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(dlc.getFiniteStateMachine()).thenReturn(stateMachine);
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(Matchers.anyLong())).thenReturn(Optional.of(dlc));

        String stringResponse = target("/devicelifecycles/1/transitions/1").request().get(String.class);
        JsonModel model = JsonModel.create(stringResponse);
        assertThat(model.<Number>get("$.id")).isEqualTo(1);
        assertThat(model.<String>get("$.name")).isEqualTo("To decommisioned");
    }

    @Test
    public void testGetUnexistedLifeCycleTransition(){
        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        when(stateMachine.getTransitions()).thenReturn(Collections.<StateTransition>emptyList());
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(dlc.getFiniteStateMachine()).thenReturn(stateMachine);
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(Matchers.anyLong())).thenReturn(Optional.of(dlc));

        Response response = target("/devicelifecycles/1/transitions/7").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }
}
