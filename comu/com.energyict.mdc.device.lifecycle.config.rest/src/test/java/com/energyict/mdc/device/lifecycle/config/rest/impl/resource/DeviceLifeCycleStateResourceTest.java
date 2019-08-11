/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.impl.resource;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.StageSet;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.rest.DeviceLifeCycleConfigApplicationJerseyTest;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleStateInfo;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
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
        assertThat(model.<String>get("$.deviceLifeCycleStates[0].name")).isEqualTo("Commissioning");
        assertThat(model.<Number>get("$.deviceLifeCycleStates[2].id")).isEqualTo(3);
        assertThat(model.<String>get("$.deviceLifeCycleStates[2].name")).isEqualTo("In stock");
        assertThat(model.<Boolean>get("$.deviceLifeCycleStates[2].isCustom")).isEqualTo(false);
        assertThat(model.<Boolean>get("$.deviceLifeCycleStates[2].isInitial")).isEqualTo(false);
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
    public void testLifeCycleStateWithOnEntryProcessesJsonModel(){
        List<State> states = mockDefaultStatesWithOnEntryProcessesForDecommissioned();

        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        when(stateMachine.getStates()).thenReturn(states);
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(dlc.getFiniteStateMachine()).thenReturn(stateMachine);
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(Matchers.anyLong())).thenReturn(Optional.of(dlc));

        String stringResponse = target("/devicelifecycles/1/states").request().get(String.class);
        JsonModel model = JsonModel.create(stringResponse);
        assertThat(model.<Number>get("$.total")).isEqualTo(3);
        assertThat(model.<List>get("$.deviceLifeCycleStates[0].onEntry").size()).isEqualTo(0);
        assertThat(model.<List>get("$.deviceLifeCycleStates[0].onExit").size()).isEqualTo(0);
        assertThat(model.<String>get("$.deviceLifeCycleStates[1].name")).isEqualTo("Decommissioned");
        assertThat(model.<List>get("$.deviceLifeCycleStates[1].onEntry").size()).isEqualTo(2);
        assertThat(model.<List>get("$.deviceLifeCycleStates[1].onExit").size()).isEqualTo(0);
        assertThat(model.<Number>get("$.deviceLifeCycleStates[1].onEntry[0].id")).isEqualTo(1);
        assertThat(model.<String>get("$.deviceLifeCycleStates[1].onEntry[0].name")).isEqualTo("nameOnEntry1");
        assertThat(model.<String>get("$.deviceLifeCycleStates[1].onEntry[0].version")).isEqualTo("1.0");
        assertThat(model.<Number>get("$.deviceLifeCycleStates[1].onEntry[1].id")).isEqualTo(2);
        assertThat(model.<String>get("$.deviceLifeCycleStates[1].onEntry[1].name")).isEqualTo("nameOnEntry2");
        assertThat(model.<String>get("$.deviceLifeCycleStates[1].onEntry[1].version")).isEqualTo("1.0");
        assertThat(model.<List>get("$.deviceLifeCycleStates[2].onEntry").size()).isEqualTo(0);
        assertThat(model.<List>get("$.deviceLifeCycleStates[2].onExit").size()).isEqualTo(0);
    }

    @Test
    public void testLifeCycleStateWithOnExitProcessesJsonModel(){
        List<State> states = mockDefaultStatesWithOnExitProcessesForInStock();

        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        when(stateMachine.getStates()).thenReturn(states);
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(dlc.getFiniteStateMachine()).thenReturn(stateMachine);
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(Matchers.anyLong())).thenReturn(Optional.of(dlc));

        String stringResponse = target("/devicelifecycles/1/states").request().get(String.class);
        JsonModel model = JsonModel.create(stringResponse);
        assertThat(model.<Number>get("$.total")).isEqualTo(3);
        assertThat(model.<List>get("$.deviceLifeCycleStates[0].onEntry").size()).isEqualTo(0);
        assertThat(model.<List>get("$.deviceLifeCycleStates[0].onExit").size()).isEqualTo(0);
        assertThat(model.<List>get("$.deviceLifeCycleStates[1].onEntry").size()).isEqualTo(0);
        assertThat(model.<List>get("$.deviceLifeCycleStates[1].onExit").size()).isEqualTo(0);
        assertThat(model.<String>get("$.deviceLifeCycleStates[2].name")).isEqualTo("In stock");
        assertThat(model.<List>get("$.deviceLifeCycleStates[2].onEntry").size()).isEqualTo(0);
        assertThat(model.<List>get("$.deviceLifeCycleStates[2].onExit").size()).isEqualTo(3);
        assertThat(model.<Number>get("$.deviceLifeCycleStates[2].onExit[0].id")).isEqualTo(1);
        assertThat(model.<String>get("$.deviceLifeCycleStates[2].onExit[0].name")).isEqualTo("nameOnExit1");
        assertThat(model.<String>get("$.deviceLifeCycleStates[2].onExit[0].version")).isEqualTo("1.0");
        assertThat(model.<Number>get("$.deviceLifeCycleStates[2].onExit[1].id")).isEqualTo(2);
        assertThat(model.<String>get("$.deviceLifeCycleStates[2].onExit[1].name")).isEqualTo("nameOnExit2");
        assertThat(model.<String>get("$.deviceLifeCycleStates[2].onExit[1].version")).isEqualTo("1.0");
        assertThat(model.<Number>get("$.deviceLifeCycleStates[2].onExit[2].id")).isEqualTo(3);
        assertThat(model.<String>get("$.deviceLifeCycleStates[2].onExit[2].name")).isEqualTo("nameOnExit3");
        assertThat(model.<String>get("$.deviceLifeCycleStates[2].onExit[2].version")).isEqualTo("1.0");
    }

    @Test
    public void testLifeCycleStateWithOnEntryAndOnExitProcessesJsonModel(){
        List<State> states = mockDefaultStatesWithOnEntryAndOnExitProcessesForCommissioning();

        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        when(stateMachine.getStates()).thenReturn(states);
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(dlc.getFiniteStateMachine()).thenReturn(stateMachine);
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(Matchers.anyLong())).thenReturn(Optional.of(dlc));

        String stringResponse = target("/devicelifecycles/1/states").request().get(String.class);
        JsonModel model = JsonModel.create(stringResponse);
        assertThat(model.<Number>get("$.total")).isEqualTo(3);
        assertThat(model.<String>get("$.deviceLifeCycleStates[0].name")).isEqualTo("Commissioning");
        assertThat(model.<List>get("$.deviceLifeCycleStates[0].onEntry").size()).isEqualTo(2);
        assertThat(model.<List>get("$.deviceLifeCycleStates[0].onExit").size()).isEqualTo(1);
        assertThat(model.<Number>get("$.deviceLifeCycleStates[0].onEntry[0].id")).isEqualTo(1);
        assertThat(model.<String>get("$.deviceLifeCycleStates[0].onEntry[0].name")).isEqualTo("nameOnEntry1");
        assertThat(model.<String>get("$.deviceLifeCycleStates[0].onEntry[0].version")).isEqualTo("1.0");
        assertThat(model.<Number>get("$.deviceLifeCycleStates[0].onEntry[1].id")).isEqualTo(2);
        assertThat(model.<String>get("$.deviceLifeCycleStates[0].onEntry[1].name")).isEqualTo("nameOnEntry2");
        assertThat(model.<String>get("$.deviceLifeCycleStates[0].onEntry[1].version")).isEqualTo("1.0");
        assertThat(model.<Number>get("$.deviceLifeCycleStates[0].onExit[0].id")).isEqualTo(3);
        assertThat(model.<String>get("$.deviceLifeCycleStates[0].onExit[0].name")).isEqualTo("nameOnExit1");
        assertThat(model.<String>get("$.deviceLifeCycleStates[0].onExit[0].version")).isEqualTo("1.0");

        assertThat(model.<List>get("$.deviceLifeCycleStates[1].onEntry").size()).isEqualTo(0);
        assertThat(model.<List>get("$.deviceLifeCycleStates[1].onExit").size()).isEqualTo(0);

        assertThat(model.<List>get("$.deviceLifeCycleStates[2].onEntry").size()).isEqualTo(0);
        assertThat(model.<List>get("$.deviceLifeCycleStates[2].onExit").size()).isEqualTo(0);

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
    public void testAddNewDeviceLifeCycleState() {
        State newState = mockSimpleState(1L, "New state");
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(Matchers.anyLong())).thenReturn(Optional.of(dlc));
        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        when(stateMachine.getState("New state")).thenReturn(Optional.of(newState));
        StageSet stageSet = mock(StageSet.class);
        Stage stage = mock(Stage.class);
        when(stageSet.getStageByName(anyString())).thenReturn(Optional.of(stage));
        when(stateMachine.getStageSet()).thenReturn(Optional.of(stageSet));
        when(dlc.getFiniteStateMachine()).thenReturn(stateMachine);
        FiniteStateMachineUpdater fsmUpdater = mock(FiniteStateMachineUpdater.class);
        when(stateMachine.startUpdate()).thenReturn(fsmUpdater);
        FiniteStateMachineBuilder.StateBuilder stateBuilder = mock(FiniteStateMachineBuilder.StateBuilder.class);
        when(fsmUpdater.newCustomState(Matchers.anyString(), anyObject())).thenReturn(stateBuilder);
        when(fsmUpdater.complete()).thenReturn(stateMachine);

        when(stateBuilder.complete()).thenReturn(newState);

        DeviceLifeCycleStateInfo entity = new DeviceLifeCycleStateInfo();
        entity.name = "New state";
        entity.stage = new IdWithNameInfo(EndDeviceStage.OPERATIONAL.getKey(), EndDeviceStage.OPERATIONAL.getKey());
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
        StageSet stageSet = mock(StageSet.class);
        Stage stage = mock(Stage.class);
        when(stageSet.getStageByName(anyString())).thenReturn(Optional.of(stage));
        when(stateMachine.getStageSet()).thenReturn(Optional.of(stageSet));

        FiniteStateMachineUpdater fsmUpdater = mock(FiniteStateMachineUpdater.class);
        when(stateMachine.startUpdate()).thenReturn(fsmUpdater);
        FiniteStateMachineUpdater.StateUpdater stateUpdater = mock(FiniteStateMachineUpdater.StateUpdater.class);
        when(fsmUpdater.state(Matchers.anyLong())).thenReturn(stateUpdater);
        when(stateUpdater.complete()).thenReturn(stateForEdit);

        DeviceLifeCycleStateInfo info = new DeviceLifeCycleStateInfo();
        info.name = "Eddited custom state";
        info.version = stateForEdit.getVersion();
        info.parent = new VersionInfo<>(dlc.getId(), dlc.getVersion());
        info.stage = new IdWithNameInfo(EndDeviceStage.OPERATIONAL.getKey(), EndDeviceStage.OPERATIONAL.getKey());
        Response response = target("/devicelifecycles/1/states/1").request().put(Entity.json(info));
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
        StageSet stageSet = mock(StageSet.class);
        Stage stage = mock(Stage.class);
        when(stageSet.getStageByName(anyString())).thenReturn(Optional.of(stage));
        when(stateMachine.getStageSet()).thenReturn(Optional.of(stageSet));

        FiniteStateMachineUpdater fsmUpdater = mock(FiniteStateMachineUpdater.class);
        when(stateMachine.startUpdate()).thenReturn(fsmUpdater);
        FiniteStateMachineUpdater.StateUpdater stateUpdater = mock(FiniteStateMachineUpdater.StateUpdater.class);
        when(fsmUpdater.state(Matchers.anyLong())).thenReturn(stateUpdater);
        when(stateUpdater.complete()).thenReturn(stateForEdit);

        DeviceLifeCycleStateInfo info = new DeviceLifeCycleStateInfo();
        info.name = "Eddited custom state";
        info.version = stateForEdit.getVersion();
        info.parent = new VersionInfo<>(dlc.getId(), dlc.getVersion());
        info.stage = new IdWithNameInfo(EndDeviceStage.OPERATIONAL.getKey(), EndDeviceStage.OPERATIONAL.getKey());
        Response response = target("/devicelifecycles/1/states/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(stateUpdater, times(0)).setName(Matchers.anyString());
    }

    @Test
    public void testSetInitialDeviceLifeCycleState(){
        State stateForEdit = mockSimpleState(1L, "Custom state");
        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        when(stateMachine.getStates()).thenReturn(Collections.singletonList(stateForEdit));
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(dlc.getFiniteStateMachine()).thenReturn(stateMachine);

        FiniteStateMachineUpdater fsmUpdater = mock(FiniteStateMachineUpdater.class);
        when(stateMachine.startUpdate()).thenReturn(fsmUpdater);

        DeviceLifeCycleStateInfo info = new DeviceLifeCycleStateInfo();
        info.version = stateForEdit.getVersion();
        info.parent = new VersionInfo<>(dlc.getId(), dlc.getVersion());

        Response response = target("/devicelifecycles/1/states/1/status").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(fsmUpdater, times(1)).complete(stateForEdit);
    }

    @Test
    public void testDeleteStateDeviceLifeCycleIsInUse(){
        State stateForDelete = mockSimpleState(1L, "Custom state");
        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(dlc.getFiniteStateMachine()).thenReturn(stateMachine);

        DeviceType deviceType = mock(DeviceType.class);
        when(deviceConfigurationService.findDeviceTypesUsingDeviceLifeCycle(dlc)).thenReturn(Collections.singletonList(deviceType));

        DeviceLifeCycleStateInfo info = new DeviceLifeCycleStateInfo();
        info.id = stateForDelete.getId();
        info.version = stateForDelete.getVersion();
        info.parent = new VersionInfo<>(dlc.getId(), dlc.getVersion());

        Response response = target("/devicelifecycles/1/states/1").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verify(deviceConfigurationService, times(1)).findDeviceTypesUsingDeviceLifeCycle(dlc);
        verify(stateForDelete, times(0)).isInitial();
    }

    @Test
    public void testDeleteStateHasTransitions(){
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        FiniteStateMachine finiteStateMachine = mock(FiniteStateMachine.class);
        when(dlc.getFiniteStateMachine()).thenReturn(finiteStateMachine);
        State stateA = mockSimpleState(1L, "State A");
        State stateB = mockSimpleState(1L, "State B");
        AuthorizedTransitionAction transitionAtoB = mockSimpleAction(1L, "A to B", stateA, stateB);
        when(transitionAtoB.getDeviceLifeCycle()).thenReturn(dlc);
        AuthorizedTransitionAction transitionBtoA = mockSimpleAction(2L, "B to A", stateB, stateA);
        when(transitionBtoA.getDeviceLifeCycle()).thenReturn(dlc);
        StateTransition stateTransitionAtoB = transitionAtoB.getStateTransition();
        StateTransition stateTransitionBtoA = transitionBtoA.getStateTransition();
        when(finiteStateMachine.getTransitions()).thenReturn(Arrays.asList(stateTransitionAtoB, stateTransitionBtoA));
        when(dlc.getAuthorizedActions()).thenReturn(Arrays.asList(transitionAtoB, transitionBtoA));
        when(finiteStateMachine.getStates()).thenReturn(Arrays.asList(stateA, stateB));
        when(finiteStateMachineService.findAndLockStateByIdAndVersion(Matchers.anyLong(), Matchers.anyLong())).thenReturn(Optional.of(stateA));
        when(deviceConfigurationService.findDeviceTypesUsingDeviceLifeCycle(dlc)).thenReturn(Collections.emptyList());

        DeviceLifeCycleStateInfo info = new DeviceLifeCycleStateInfo();
        info.id = stateA.getId();
        info.version = stateA.getVersion();
        info.parent = new VersionInfo<>(dlc.getId(), dlc.getVersion());

        Response response = target("/devicelifecycles/1/states/1").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verify(deviceConfigurationService, times(1)).findDeviceTypesUsingDeviceLifeCycle(dlc);
    }

    @Test
    public void testDeleteStateTheLastState(){
        State stateForDelete = mockSimpleState(1L, "Custom state");
        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        when(stateMachine.getStates()).thenReturn(Collections.singletonList(stateForDelete));
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(dlc.getFiniteStateMachine()).thenReturn(stateMachine);
        when(stateMachine.getTransitions()).thenReturn(Collections.emptyList());
        when(deviceConfigurationService.findDeviceTypesUsingDeviceLifeCycle(dlc)).thenReturn(Collections.emptyList());

        DeviceLifeCycleStateInfo info = new DeviceLifeCycleStateInfo();
        info.id = stateForDelete.getId();
        info.version = stateForDelete.getVersion();
        info.parent = new VersionInfo<>(dlc.getId(), dlc.getVersion());

        Response response = target("/devicelifecycles/1/states/1").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verify(deviceConfigurationService, times(1)).findDeviceTypesUsingDeviceLifeCycle(dlc);
        verify(stateMachine, times(1)).getStates();
        verify(stateMachine, times(1)).getTransitions();
        verify(stateForDelete, times(0)).isInitial();
    }

    @Test
    public void testDeleteStateInitial(){
        State stateForDelete = mockSimpleState(1L, "Custom state");
        State anotherOneState = mockSimpleState(2L, "Another one state");
        when(stateForDelete.isInitial()).thenReturn(true);
        FiniteStateMachine stateMachine = mock(FiniteStateMachine.class);
        when(stateMachine.getStates()).thenReturn(Arrays.asList(stateForDelete, anotherOneState));
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        when(dlc.getFiniteStateMachine()).thenReturn(stateMachine);
        when(stateMachine.getTransitions()).thenReturn(Collections.emptyList());
        when(deviceConfigurationService.findDeviceTypesUsingDeviceLifeCycle(dlc)).thenReturn(Collections.emptyList());

        DeviceLifeCycleStateInfo info = new DeviceLifeCycleStateInfo();
        info.id = stateForDelete.getId();
        info.version = stateForDelete.getVersion();
        info.parent = new VersionInfo<>(dlc.getId(), dlc.getVersion());

        Response response = target("/devicelifecycles/1/states/1").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();;
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verify(deviceConfigurationService, times(1)).findDeviceTypesUsingDeviceLifeCycle(dlc);
        verify(stateMachine, times(1)).getStates();
        verify(stateMachine, times(1)).getTransitions();
        verify(stateForDelete, times(1)).isInitial();
    }
}
