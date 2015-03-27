package com.energyict.mdc.device.lifecycle.config.rest;

import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.rest.response.DeviceLifeCycleStateInfo;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.State;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.mockito.Matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceLifeCycleStateResourceTest extends DeviceLifeCycleConfigApplicationJerseyTest {

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
        assertThat(model.<String>get("$.deviceLifeCycleStates[0].name")).isEqualTo("Commissioned");
        assertThat(model.<Number>get("$.deviceLifeCycleStates[2].id")).isEqualTo(3);
        assertThat(model.<String>get("$.deviceLifeCycleStates[2].name")).isEqualTo("In stock");
    }

    @Test
    public void testEmptyLifeCycleStateList(){
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

    @Test
        public void testAddNewDeviceLifeCycleState(){
        State newState = mockSimpleState(1L, "New state");
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(Matchers.anyLong())).thenReturn(Optional.of(dlc));
        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        when(stateMachine.getState("New state")).thenReturn(Optional.of(newState));
        when(dlc.getFiniteStateMachine()).thenReturn(stateMachine);
        FiniteStateMachineUpdater fsmUpdater = mock(FiniteStateMachineUpdater.class);
        when(stateMachine.startUpdate()).thenReturn(fsmUpdater);
        FiniteStateMachineBuilder.StateBuilder stateBuilder = mock(FiniteStateMachineBuilder.StateBuilder.class);
        when(fsmUpdater.newCustomState(Matchers.anyString())).thenReturn(stateBuilder);
        when(fsmUpdater.complete()).thenReturn(stateMachine);

        when(stateBuilder.complete()).thenReturn(newState);

        DeviceLifeCycleStateInfo entity = new DeviceLifeCycleStateInfo();
        entity.name = "New state";
        Response response = target("/devicelifecycles/1/states").request().post(Entity.json(entity));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testEditDeviceLifeCycleState(){
        State stateForEdit = mockSimpleState(1L, "Custom state");
        when(stateForEdit.isCustom()).thenReturn(true);
        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        when(stateMachine.getStates()).thenReturn(Collections.singletonList(stateForEdit));
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(dlc.getFiniteStateMachine()).thenReturn(stateMachine);
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(Matchers.anyLong())).thenReturn(Optional.of(dlc));

        FiniteStateMachineUpdater fsmUpdater = mock(FiniteStateMachineUpdater.class);
        when(stateMachine.startUpdate()).thenReturn(fsmUpdater);
        FiniteStateMachineUpdater.StateUpdater stateUpdater = mock(FiniteStateMachineUpdater.StateUpdater.class);
        when(fsmUpdater.state(Matchers.anyLong())).thenReturn(stateUpdater);
        when(stateUpdater.complete()).thenReturn(stateForEdit);

        DeviceLifeCycleStateInfo entity = new DeviceLifeCycleStateInfo();
        entity.name = "Eddited custom state";
        Response response = target("/devicelifecycles/1/states/1").request().put(Entity.json(entity));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(stateUpdater, times(1)).setName(Matchers.anyString());
    }

    @Test
    public void testEditDefaultDeviceLifeCycleState(){
        State stateForEdit = mockSimpleState(1L, "Custom state");
        when(stateForEdit.isCustom()).thenReturn(false);
        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        when(stateMachine.getStates()).thenReturn(Collections.singletonList(stateForEdit));
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(dlc.getFiniteStateMachine()).thenReturn(stateMachine);
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(Matchers.anyLong())).thenReturn(Optional.of(dlc));

        FiniteStateMachineUpdater fsmUpdater = mock(FiniteStateMachineUpdater.class);
        when(stateMachine.startUpdate()).thenReturn(fsmUpdater);
        FiniteStateMachineUpdater.StateUpdater stateUpdater = mock(FiniteStateMachineUpdater.StateUpdater.class);
        when(fsmUpdater.state(Matchers.anyLong())).thenReturn(stateUpdater);
        when(stateUpdater.complete()).thenReturn(stateForEdit);

        DeviceLifeCycleStateInfo entity = new DeviceLifeCycleStateInfo();
        entity.name = "Eddited custom state";
        Response response = target("/devicelifecycles/1/states/1").request().put(Entity.json(entity));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(stateUpdater, times(0)).setName(Matchers.anyString());
    }
}
