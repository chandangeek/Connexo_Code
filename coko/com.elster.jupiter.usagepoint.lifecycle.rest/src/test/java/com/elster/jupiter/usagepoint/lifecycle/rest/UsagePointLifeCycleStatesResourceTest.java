/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */


package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.StageSet;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsagePointLifeCycleStatesResourceTest extends UsagePointLifeCycleApplicationTest {

    @Mock
    private UsagePointLifeCycle lifeCycle;
    @Mock
    private State state;
    @Mock
    private Stage stage;


    @Before
    public void before() {
        when(usagePointLifeCycleConfigurationService.findUsagePointLifeCycle(12L)).thenReturn(Optional.of(lifeCycle));
        when(lifeCycle.getId()).thenReturn(12L);
        when(lifeCycle.getVersion()).thenReturn(4L);
        List<State> states = new ArrayList<>();
        states.add(state);
        when(lifeCycle.getStates()).thenReturn(states);
        when(state.getStage()).thenReturn(Optional.of(stage));

    }

    @Test
    public void testGetLifeCycleStates() {
        when(state.getId()).thenReturn(4L);
        when(state.getName()).thenReturn("State");
        when(state.getVersion()).thenReturn(3L);

        String response = target("/lifecycle/12/states").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.states")).hasSize(1);
        assertThat(model.<Number>get("$.states[0].id")).isEqualTo(4);
        assertThat(model.<String>get("$.states[0].name")).isEqualTo("State");
        assertThat(model.<Number>get("$.states[0].version")).isEqualTo(3);
    }

    @Test
    public void testGetStateById() {
        when(usagePointLifeCycleConfigurationService.findUsagePointState(4)).thenReturn(Optional.of(state));
        when(state.getId()).thenReturn(4L);
        when(state.getName()).thenReturn("State");
        when(state.isInitial()).thenReturn(true);
        when(state.getVersion()).thenReturn(3L);
        List<ProcessReference> onEntry = Collections.singletonList(mockProcessReference(1L, "processName 1", "deploymentId 1", "processId 1"));
        when(state.getOnEntryProcesses()).thenReturn(onEntry);
        List<ProcessReference> onExit = Collections.singletonList(mockProcessReference(2L, "processName 2", "deploymentId 2", "processId 2"));
        when(state.getOnExitProcesses()).thenReturn(onExit);

        String response = target("/lifecycle/12/states/4").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.id")).isEqualTo(4);
        assertThat(model.<String>get("$.name")).isEqualTo("State");
        assertThat(model.<Number>get("$.version")).isEqualTo(3);
        assertThat(model.<Boolean>get("$.isInitial")).isEqualTo(true);
        assertThat(model.<Number>get("$.onEntry[0].id")).isEqualTo(1);
        assertThat(model.<Number>get("$.onExit[0].id")).isEqualTo(2);
    }

    @Test
    public void testGetStateByIdUnExisting() throws Exception {
        when(usagePointLifeCycleConfigurationService.findUsagePointState(4)).thenReturn(Optional.empty());

        Response response = target("/lifecycle/12/states/4").request().get();
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(model.<String>get("$.message")).isEqualTo("No usage point state with id 4");
        assertThat(model.<String>get("$.error")).isEqualTo("no.such.life.cycle.state");
    }

    @Test
    public void testNewState() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));
        when(state.getStage()).thenReturn(Optional.of(stage));
        when(stage.getId()).thenReturn(1L);
        when(stage.getName()).thenReturn("Stage");
        StageSet defaultStageSet = mock(StageSet.class);
        when(usagePointLifeCycleConfigurationService.getDefaultStageSet()).thenReturn(defaultStageSet);
        when(defaultStageSet.getStageByName(anyString())).thenReturn(Optional.of(stage));
        UsagePointTransition.UsagePointTransitionCreator builder = FakeBuilder.initBuilderStub(state, UsagePointTransition.UsagePointTransitionCreator.class);
        when(lifeCycle.newTransition(anyString(), any(), any())).thenReturn(builder);
        ProcessReference onEntry = mockProcessReference(1L, "processName 1", "deploymentId 1", "processId 1");
        BpmProcessDefinition onEntryProcess = onEntry.getStateChangeBusinessProcess();
        when(bpmService.findBpmProcessDefinition(onEntryProcess.getId())).thenReturn(Optional.of(onEntryProcess));
        ProcessReference onExit = mockProcessReference(2L, "processName 2", "deploymentId 2", "processId 2");
        BpmProcessDefinition onExitProcess = onExit.getStateChangeBusinessProcess();
        when(bpmService.findBpmProcessDefinition(onExitProcess.getId())).thenReturn(Optional.of(onExitProcess));

        when(state.getId()).thenReturn(4L);
        when(state.getName()).thenReturn("State");
        when(state.isInitial()).thenReturn(false);
        when(state.getVersion()).thenReturn(1L);
        when(state.getOnEntryProcesses()).thenReturn(Collections.singletonList(onEntry));
        when(state.getOnExitProcesses()).thenReturn(Collections.singletonList(onExit));

        FiniteStateMachineUpdater lifeCycleUpdater = mock(FiniteStateMachineUpdater.class);
        FiniteStateMachineBuilder.StateBuilder stateBuilder = mock(FiniteStateMachineBuilder.StateBuilder.class);
        when(lifeCycle.getUpdater()).thenReturn(lifeCycleUpdater);
        when(lifeCycleUpdater.newCustomState(anyString(), any())).thenReturn(stateBuilder);
        when(stateBuilder.complete()).thenReturn(state);
        UsagePointLifeCycleStateInfo info = new UsagePointLifeCycleStateInfo();
        info.id = 4L;
        info.name = "State";
        info.stage = new IdWithNameInfo("Stage", "Stage");
        info.onEntry = Collections.singletonList(new BusinessProcessInfo(1L, null, null));
        info.onExit = Collections.singletonList(new BusinessProcessInfo(2L, null, null));
        info.parent = new VersionInfo<>(12L, 4L);
        Entity<UsagePointLifeCycleStateInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12/states").request().post(json);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.id")).isEqualTo(4);
        assertThat(model.<String>get("$.name")).isEqualTo("State");
        assertThat(model.<Number>get("$.version")).isEqualTo(1);
        assertThat(model.<Boolean>get("$.isInitial")).isEqualTo(false);
        assertThat(model.<Number>get("$.onEntry[0].id")).isEqualTo(1);
        assertThat(model.<Number>get("$.onExit[0].id")).isEqualTo(2);
    }

    @Test
    public void testNewStateProcessNotFound() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));
        when(state.getStage()).thenReturn(Optional.of(stage));
        when(stage.getId()).thenReturn(1L);
        when(stage.getName()).thenReturn("Stage");
        StageSet defaultStageSet = mock(StageSet.class);
        when(usagePointLifeCycleConfigurationService.getDefaultStageSet()).thenReturn(defaultStageSet);
        when(defaultStageSet.getStageByName(anyString())).thenReturn(Optional.of(stage));
        UsagePointTransition.UsagePointTransitionCreator builder = FakeBuilder.initBuilderStub(state, UsagePointTransition.UsagePointTransitionCreator.class);
        when(lifeCycle.newTransition(anyString(), any(), any())).thenReturn(builder);
        when(bpmService.findBpmProcessDefinition(anyLong())).thenReturn(Optional.empty());

        FiniteStateMachineUpdater lifeCycleUpdater = mock(FiniteStateMachineUpdater.class);
        FiniteStateMachineBuilder.StateBuilder stateBuilder = mock(FiniteStateMachineBuilder.StateBuilder.class);
        when(lifeCycle.getUpdater()).thenReturn(lifeCycleUpdater);
        when(lifeCycleUpdater.newCustomState(anyString(), any())).thenReturn(stateBuilder);
        when(stateBuilder.complete()).thenReturn(state);

        UsagePointLifeCycleStateInfo info = new UsagePointLifeCycleStateInfo();
        info.id = 4L;
        info.name = "State";
        info.stage = new IdWithNameInfo("Stage", "Stage");
        info.onEntry = Collections.singletonList(new BusinessProcessInfo(1L, null, null));
        info.onExit = Collections.singletonList(new BusinessProcessInfo(2L, null, null));
        info.parent = new VersionInfo<>(12L, 4L);
        Entity<UsagePointLifeCycleStateInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12/states").request().post(json);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(model.<String>get("$.message")).isEqualTo("No business process with id 1");
        assertThat(model.<String>get("$.error")).isEqualTo("no.such.state.process");
    }

    @Test
    public void testEditState() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));
        when(usagePointLifeCycleConfigurationService.findUsagePointState(anyLong())).thenReturn(Optional.of(state));
        ProcessReference onEntry = mockProcessReference(1L, "processName 1", "deploymentId 1", "processId 1");
        BpmProcessDefinition onEntryProcess = onEntry.getStateChangeBusinessProcess();
        when(bpmService.findBpmProcessDefinition(onEntryProcess.getId())).thenReturn(Optional.of(onEntryProcess));
        ProcessReference onExit = mockProcessReference(2L, "processName 2", "deploymentId 2", "processId 2");
        BpmProcessDefinition onExitProcess = onExit.getStateChangeBusinessProcess();
        when(bpmService.findBpmProcessDefinition(onExitProcess.getId())).thenReturn(Optional.of(onExitProcess));
        when(state.getId()).thenReturn(4L);
        when(state.getName()).thenReturn("State changed");
        when(state.isInitial()).thenReturn(false);
        when(state.getVersion()).thenReturn(3L);
        when(state.getOnEntryProcesses()).thenReturn(Collections.singletonList(onEntry));
        when(state.getOnExitProcesses()).thenReturn(Collections.singletonList(onExit));

        FiniteStateMachine finiteStateMachine = mock(FiniteStateMachine.class);
        FiniteStateMachineUpdater finiteStateMachineUpdater = mock(FiniteStateMachineUpdater.class);
        FiniteStateMachineUpdater.StateUpdater builder = mock(FiniteStateMachineUpdater.StateUpdater.class);
        when(state.getFiniteStateMachine()).thenReturn(finiteStateMachine);
        when(finiteStateMachine.startUpdate()).thenReturn(finiteStateMachineUpdater);
        when(finiteStateMachineUpdater.state(anyString())).thenReturn(builder);
        when(builder.complete()).thenReturn(state);

        StageSet defaultStageSet = mock(StageSet.class);
        when(usagePointLifeCycleConfigurationService.getDefaultStageSet()).thenReturn(defaultStageSet);
        when(defaultStageSet.getStageByName(anyString())).thenReturn(Optional.of(stage));
//        when(thesaurus.getString(anyString(), anyString())).thenReturn("Stage");
//        doReturn("Stage").when(thesaurus.getString(anyString(), anyString()));

        UsagePointLifeCycleStateInfo info = new UsagePointLifeCycleStateInfo();
        info.id = 4L;
        info.name = "State changed";
        info.stage = new IdWithNameInfo("Stage", "Stage");
        info.onEntry = Collections.singletonList(new BusinessProcessInfo(1L, null, null));
        info.onExit = Collections.singletonList(new BusinessProcessInfo(2L, null, null));
        info.version = 3L;
        info.parent = new VersionInfo<>(12L, 4L);
        Entity<UsagePointLifeCycleStateInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12/states/4").request().put(json);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.id")).isEqualTo(4);
        assertThat(model.<String>get("$.name")).isEqualTo("State changed");
        assertThat(model.<Number>get("$.version")).isEqualTo(3);
        assertThat(model.<Boolean>get("$.isInitial")).isEqualTo(false);
        assertThat(model.<Number>get("$.onEntry[0].id")).isEqualTo(1);
        assertThat(model.<Number>get("$.onExit[0].id")).isEqualTo(2);
        assertThat(model.<Number>get("$.parent.id")).isEqualTo(12);
        assertThat(model.<Number>get("$.parent.version")).isEqualTo(4);
    }

    @Test
    public void testEditStateProcessNotFound() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));
        when(usagePointLifeCycleConfigurationService.findUsagePointState(anyLong())).thenReturn(Optional.of(state));
        when(bpmService.findBpmProcessDefinition(anyLong())).thenReturn(Optional.empty());

        when(state.getId()).thenReturn(4L);
        when(state.getName()).thenReturn("State changed");
        when(state.isInitial()).thenReturn(false);
        when(state.getVersion()).thenReturn(3L);

        FiniteStateMachine finiteStateMachine = mock(FiniteStateMachine.class);
        FiniteStateMachineUpdater finiteStateMachineUpdater = mock(FiniteStateMachineUpdater.class);
        FiniteStateMachineUpdater.StateUpdater builder = mock(FiniteStateMachineUpdater.StateUpdater.class);
        when(state.getFiniteStateMachine()).thenReturn(finiteStateMachine);
        when(finiteStateMachine.startUpdate()).thenReturn(finiteStateMachineUpdater);
        when(finiteStateMachineUpdater.state(anyString())).thenReturn(builder);
        when(builder.complete()).thenReturn(state);

        StageSet defaultStageSet = mock(StageSet.class);
        when(usagePointLifeCycleConfigurationService.getDefaultStageSet()).thenReturn(defaultStageSet);
        when(defaultStageSet.getStageByName(anyString())).thenReturn(Optional.of(stage));

        UsagePointLifeCycleStateInfo info = new UsagePointLifeCycleStateInfo();
        info.id = 4L;
        info.name = "State changed";
        info.stage = new IdWithNameInfo("Stage", "Stage");
        info.onEntry = Collections.singletonList(new BusinessProcessInfo(1L, null, null));
        info.onExit = Collections.singletonList(new BusinessProcessInfo(2L, null, null));
        info.version = 3L;
        info.parent = new VersionInfo<>(12L, 4L);
        Entity<UsagePointLifeCycleStateInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12/states/4").request().put(json);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(model.<String>get("$.message")).isEqualTo("No business process with id 1");
        assertThat(model.<String>get("$.error")).isEqualTo("no.such.state.process");
    }

    @Test
    public void testEditStateConcurrentParent() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 3L)).thenReturn(Optional.empty());
        when(usagePointLifeCycleConfigurationService.findUsagePointLifeCycle(12L)).thenReturn(Optional.of(lifeCycle));
        when(usagePointLifeCycleConfigurationService.findUsagePointState(4L)).thenReturn(Optional.of(state));

        when(bpmService.findBpmProcessDefinition(anyLong())).thenReturn(Optional.empty());

        when(state.getId()).thenReturn(4L);
        when(state.getName()).thenReturn("State changed");
        when(state.isInitial()).thenReturn(false);
        when(state.getVersion()).thenReturn(3L);

        FiniteStateMachine finiteStateMachine = mock(FiniteStateMachine.class);
        FiniteStateMachineUpdater finiteStateMachineUpdater = mock(FiniteStateMachineUpdater.class);
        FiniteStateMachineUpdater.StateUpdater builder = mock(FiniteStateMachineUpdater.StateUpdater.class);
        when(state.getFiniteStateMachine()).thenReturn(finiteStateMachine);
        when(finiteStateMachine.startUpdate()).thenReturn(finiteStateMachineUpdater);
        when(finiteStateMachineUpdater.state(anyString())).thenReturn(builder);
        when(builder.complete()).thenReturn(state);

        StageSet defaultStageSet = mock(StageSet.class);
        when(usagePointLifeCycleConfigurationService.getDefaultStageSet()).thenReturn(defaultStageSet);
        when(defaultStageSet.getStageByName(anyString())).thenReturn(Optional.of(stage));

        UsagePointLifeCycleStateInfo info = new UsagePointLifeCycleStateInfo();
        info.id = 4L;
        info.name = "State";
        info.stage = new IdWithNameInfo("Stage", "Stage");
        info.onEntry = Collections.singletonList(new BusinessProcessInfo(1L, null, null));
        info.onExit = Collections.singletonList(new BusinessProcessInfo(2L, null, null));
        info.version = 2L;
        info.parent = new VersionInfo<>(12L, 3L);
        Entity<UsagePointLifeCycleStateInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12/states/4").request().put(json);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testSetInitialState() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointStateByIdAndVersion(4L, 3L)).thenReturn(Optional.of(state));

        FiniteStateMachine finiteStateMachine = mock(FiniteStateMachine.class);
        FiniteStateMachineUpdater finiteStateMachineUpdater = mock(FiniteStateMachineUpdater.class);
        FiniteStateMachineUpdater.StateUpdater builder = mock(FiniteStateMachineUpdater.StateUpdater.class);
        when(state.getFiniteStateMachine()).thenReturn(finiteStateMachine);
        when(finiteStateMachine.startUpdate()).thenReturn(finiteStateMachineUpdater);
        when(finiteStateMachineUpdater.state(anyString())).thenReturn(builder);
        when(builder.complete()).thenReturn(state);

        when(state.getId()).thenReturn(4L);
        when(state.getName()).thenReturn("State");
        when(state.isInitial()).thenReturn(true);
        when(state.getVersion()).thenReturn(3L);

        UsagePointLifeCycleStateInfo info = new UsagePointLifeCycleStateInfo();
        info.id = 4L;
        info.name = "State";
        info.stage = new IdWithNameInfo("Stage", "Stage");
        info.version = 3L;
        info.parent = new VersionInfo<>(12L, 4L);
        Entity<UsagePointLifeCycleStateInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12/states/4/status").request().put(json);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.id")).isEqualTo(4);
        assertThat(model.<Boolean>get("$.isInitial")).isEqualTo(true);
    }

    @Test
    public void testRemoveState() {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointStateByIdAndVersion(4L, 3L)).thenReturn(Optional.of(state));

        UsagePointLifeCycleStateInfo info = new UsagePointLifeCycleStateInfo();
        info.id = 4L;
        info.name = "State";
        info.stage = new IdWithNameInfo("Stage", "Stage");
        info.version = 3L;
        info.parent = new VersionInfo<>(12L, 4L);
        Entity<UsagePointLifeCycleStateInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12/states/4").request().build(HttpMethod.DELETE, json).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void testRemoveStateConcurrent() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointStateByIdAndVersion(4L, 3L)).thenReturn(Optional.empty());
        when(usagePointLifeCycleConfigurationService.findUsagePointState(4L)).thenReturn(Optional.of(state));

        UsagePointLifeCycleStateInfo info = new UsagePointLifeCycleStateInfo();
        info.id = 4L;
        info.name = "State";
        info.stage = new IdWithNameInfo("Stage", "Stage");
        info.version = 3L;
        info.parent = new VersionInfo<>(12L, 4L);
        Entity<UsagePointLifeCycleStateInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12/states/4").request().build(HttpMethod.DELETE, json).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<String>get("$.error")).isEqualTo("State has changed since the page was last updated.");
    }

}