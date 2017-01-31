/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.StateChangeBusinessProcess;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointStateRemoveException;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsagePointLifeCycleStatesResourceTest extends UsagePointLifeCycleApplicationTest {

    @Mock
    private UsagePointLifeCycle lifeCycle;
    @Mock
    private UsagePointState state;
    @Mock
    private UsagePointStage stage;

    @Before
    public void before() {
        when(usagePointLifeCycleConfigurationService.findUsagePointLifeCycle(12L)).thenReturn(Optional.of(lifeCycle));
        when(lifeCycle.getStates()).thenReturn(Collections.singletonList(state));
        when(lifeCycle.getId()).thenReturn(12L);
        when(lifeCycle.getVersion()).thenReturn(4L);
        when(state.getLifeCycle()).thenReturn(lifeCycle);
        when(state.getStage()).thenReturn(stage);
        when(stage.getKey()).thenReturn(UsagePointStage.Key.OPERATIONAL);
    }

    @Test
    public void testGetLifeCycleStates() {
        when(state.getId()).thenReturn(4L);
        when(state.getName()).thenReturn("State");
        when(state.isInitial()).thenReturn(true);
        when(state.getVersion()).thenReturn(3L);

        String response = target("/lifecycle/12/states").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.states")).hasSize(1);
        assertThat(model.<Number>get("$.states[0].id")).isEqualTo(4);
        assertThat(model.<String>get("$.states[0].name")).isEqualTo("State");
        assertThat(model.<String>get("$.states[0].stage")).isEqualTo("OPERATIONAL");
        assertThat(model.<Number>get("$.states[0].version")).isEqualTo(3);
        assertThat(model.<Number>get("$.states[0].parent.id")).isEqualTo(12);
        assertThat(model.<Number>get("$.states[0].parent.version")).isEqualTo(4);
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
        assertThat(model.<String>get("$.stage")).isEqualTo("OPERATIONAL");
        assertThat(model.<Number>get("$.onEntry[0].id")).isEqualTo(1);
        assertThat(model.<Number>get("$.onExit[0].id")).isEqualTo(2);
        assertThat(model.<Number>get("$.parent.id")).isEqualTo(12);
        assertThat(model.<Number>get("$.parent.version")).isEqualTo(4);
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
        UsagePointState.UsagePointStateCreator builder = FakeBuilder.initBuilderStub(state, UsagePointState.UsagePointStateCreator.class);
        when(lifeCycle.newState(anyString())).thenReturn(builder);
        ProcessReference onEntry = mockProcessReference(1L, "processName 1", "deploymentId 1", "processId 1");
        StateChangeBusinessProcess onEntryProcess = onEntry.getStateChangeBusinessProcess();
        when(finiteStateMachineService.findStateChangeBusinessProcessById(1L)).thenReturn(Optional.of(onEntryProcess));
        ProcessReference onExit = mockProcessReference(2L, "processName 2", "deploymentId 2", "processId 2");
        StateChangeBusinessProcess onExitProcess = onExit.getStateChangeBusinessProcess();
        when(finiteStateMachineService.findStateChangeBusinessProcessById(2L)).thenReturn(Optional.of(onExitProcess));

        when(state.getId()).thenReturn(4L);
        when(state.getName()).thenReturn("State");
        when(state.isInitial()).thenReturn(false);
        when(state.getVersion()).thenReturn(1L);
        when(state.getOnEntryProcesses()).thenReturn(Collections.singletonList(onEntry));
        when(state.getOnExitProcesses()).thenReturn(Collections.singletonList(onExit));

        UsagePointLifeCycleStateInfo info = new UsagePointLifeCycleStateInfo();
        info.id = 4L;
        info.name = "State";
        info.onEntry = Collections.singletonList(new BusinessProcessInfo(1L, null, null, null));
        info.onExit = Collections.singletonList(new BusinessProcessInfo(2L, null, null, null));
        info.parent = new VersionInfo<>(12L, 4L);
        info.stage = UsagePointStage.Key.OPERATIONAL;
        Entity<UsagePointLifeCycleStateInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12/states").request().post(json);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.id")).isEqualTo(4);
        assertThat(model.<String>get("$.name")).isEqualTo("State");
        assertThat(model.<String>get("$.stage")).isEqualTo("OPERATIONAL");
        assertThat(model.<Number>get("$.version")).isEqualTo(1);
        assertThat(model.<Boolean>get("$.isInitial")).isEqualTo(false);
        assertThat(model.<Number>get("$.onEntry[0].id")).isEqualTo(1);
        assertThat(model.<Number>get("$.onExit[0].id")).isEqualTo(2);
        assertThat(model.<Number>get("$.parent.id")).isEqualTo(12);
        assertThat(model.<Number>get("$.parent.version")).isEqualTo(4);
    }

    @Test
    public void testNewStateProcessNotFound() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));
        UsagePointState.UsagePointStateCreator builder = FakeBuilder.initBuilderStub(state, UsagePointState.UsagePointStateCreator.class);
        when(lifeCycle.newState(anyString())).thenReturn(builder);
        when(finiteStateMachineService.findStateChangeBusinessProcessById(1L)).thenReturn(Optional.empty());

        UsagePointLifeCycleStateInfo info = new UsagePointLifeCycleStateInfo();
        info.id = 4L;
        info.name = "State";
        info.stage = UsagePointStage.Key.OPERATIONAL;
        info.onEntry = Collections.singletonList(new BusinessProcessInfo(1L, null, null, null));
        info.onExit = Collections.singletonList(new BusinessProcessInfo(2L, null, null, null));
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
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointStateByIdAndVersion(4L, 3L)).thenReturn(Optional.of(state));
        ProcessReference onEntry = mockProcessReference(1L, "processName 1", "deploymentId 1", "processId 1");
        StateChangeBusinessProcess onEntryProcess = onEntry.getStateChangeBusinessProcess();
        when(finiteStateMachineService.findStateChangeBusinessProcessById(1L)).thenReturn(Optional.of(onEntryProcess));
        ProcessReference onExit = mockProcessReference(2L, "processName 2", "deploymentId 2", "processId 2");
        StateChangeBusinessProcess onExitProcess = onExit.getStateChangeBusinessProcess();
        when(finiteStateMachineService.findStateChangeBusinessProcessById(2L)).thenReturn(Optional.of(onExitProcess));

        UsagePointState.UsagePointStateUpdater builder = FakeBuilder.initBuilderStub(state, UsagePointState.UsagePointStateUpdater.class, UsagePointState.UsagePointStateCreator.class);
        when(state.startUpdate()).thenReturn(builder);
        when(state.getId()).thenReturn(4L);
        when(state.getName()).thenReturn("State changed");
        when(state.isInitial()).thenReturn(false);
        when(state.getVersion()).thenReturn(3L);
        when(state.getOnEntryProcesses()).thenReturn(Collections.singletonList(onEntry));
        when(state.getOnExitProcesses()).thenReturn(Collections.singletonList(onExit));

        UsagePointLifeCycleStateInfo info = new UsagePointLifeCycleStateInfo();
        info.id = 4L;
        info.name = "State changed";
        info.onEntry = Collections.singletonList(new BusinessProcessInfo(1L, null, null, null));
        info.onExit = Collections.singletonList(new BusinessProcessInfo(2L, null, null, null));
        info.version = 3L;
        info.parent = new VersionInfo<>(12L, 4L);
        info.stage = UsagePointStage.Key.OPERATIONAL;
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
        assertThat(model.<String>get("$.stage")).isEqualTo("OPERATIONAL");
        verify(builder).setName("State changed");
        verify(builder).onEntry(onEntryProcess);
        verify(builder).onExit(onExitProcess);
    }

    @Test
    public void testEditStateProcessNotFound() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointStateByIdAndVersion(4L, 3L)).thenReturn(Optional.of(state));
        UsagePointState.UsagePointStateUpdater builder = FakeBuilder.initBuilderStub(state, UsagePointState.UsagePointStateUpdater.class, UsagePointState.UsagePointStateCreator.class);
        when(state.startUpdate()).thenReturn(builder);
        when(finiteStateMachineService.findStateChangeBusinessProcessById(1L)).thenReturn(Optional.empty());

        UsagePointLifeCycleStateInfo info = new UsagePointLifeCycleStateInfo();
        info.id = 4L;
        info.name = "State changed";
        info.onEntry = Collections.singletonList(new BusinessProcessInfo(1L, null, null, null));
        info.onExit = Collections.singletonList(new BusinessProcessInfo(2L, null, null, null));
        info.stage = UsagePointStage.Key.OPERATIONAL;
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

        UsagePointLifeCycleStateInfo info = new UsagePointLifeCycleStateInfo();
        info.id = 4L;
        info.name = "State";
        info.stage = UsagePointStage.Key.OPERATIONAL;
        info.onEntry = Collections.singletonList(new BusinessProcessInfo(1L, null, null, null));
        info.onExit = Collections.singletonList(new BusinessProcessInfo(2L, null, null, null));
        info.version = 2L;
        info.parent = new VersionInfo<>(12L, 3L);
        Entity<UsagePointLifeCycleStateInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12/states/4").request().put(json);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        assertThat(model.<String>get("$.message")).isEqualTo("Failed to save 'State'");
        assertThat(model.<String>get("$.error")).isEqualTo("State has changed since the page was last updated.");
    }

    @Test
    public void testEditStateConcurrentState() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointStateByIdAndVersion(4L, 2L)).thenReturn(Optional.empty());
        when(usagePointLifeCycleConfigurationService.findUsagePointState(4L)).thenReturn(Optional.of(state));

        UsagePointLifeCycleStateInfo info = new UsagePointLifeCycleStateInfo();
        info.id = 4L;
        info.name = "State";
        info.stage = UsagePointStage.Key.OPERATIONAL;
        info.onEntry = Collections.singletonList(new BusinessProcessInfo(1L, null, null, null));
        info.onExit = Collections.singletonList(new BusinessProcessInfo(2L, null, null, null));
        info.version = 2L;
        info.parent = new VersionInfo<>(12L, 4L);
        Entity<UsagePointLifeCycleStateInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12/states/4").request().put(json);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        assertThat(model.<String>get("$.message")).isEqualTo("Failed to save 'State'");
        assertThat(model.<String>get("$.error")).isEqualTo("State has changed since the page was last updated.");
    }

    @Test
    public void testSetInitialState() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointStateByIdAndVersion(4L, 3L)).thenReturn(Optional.of(state));

        UsagePointState.UsagePointStateUpdater builder = FakeBuilder.initBuilderStub(state, UsagePointState.UsagePointStateUpdater.class, UsagePointState.UsagePointStateCreator.class);
        when(state.startUpdate()).thenReturn(builder);
        when(state.getId()).thenReturn(4L);
        when(state.getName()).thenReturn("State");
        when(state.isInitial()).thenReturn(true);
        when(state.getVersion()).thenReturn(3L);

        UsagePointLifeCycleStateInfo info = new UsagePointLifeCycleStateInfo();
        info.id = 4L;
        info.name = "State";
        info.stage = UsagePointStage.Key.OPERATIONAL;
        info.version = 3L;
        info.parent = new VersionInfo<>(12L, 4L);
        Entity<UsagePointLifeCycleStateInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12/states/4/status").request().put(json);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.id")).isEqualTo(4);
        assertThat(model.<Boolean>get("$.isInitial")).isEqualTo(true);
        verify(builder).setInitial();
    }

    @Test
    public void testSetInitialStateConcurrent() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointStateByIdAndVersion(4L, 2L)).thenReturn(Optional.empty());
        when(usagePointLifeCycleConfigurationService.findUsagePointState(4L)).thenReturn(Optional.of(state));

        UsagePointLifeCycleStateInfo info = new UsagePointLifeCycleStateInfo();
        info.id = 4L;
        info.name = "State";
        info.stage = UsagePointStage.Key.OPERATIONAL;
        info.version = 2L;
        info.parent = new VersionInfo<>(12L, 4L);
        Entity<UsagePointLifeCycleStateInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12/states/4/status").request().put(json);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        assertThat(model.<String>get("$.message")).isEqualTo("Failed to save 'State'");
        assertThat(model.<String>get("$.error")).isEqualTo("State has changed since the page was last updated.");
    }

    @Test
    public void testRemoveState() {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointStateByIdAndVersion(4L, 3L)).thenReturn(Optional.of(state));

        UsagePointLifeCycleStateInfo info = new UsagePointLifeCycleStateInfo();
        info.id = 4L;
        info.name = "State";
        info.stage = UsagePointStage.Key.OPERATIONAL;
        info.version = 3L;
        info.parent = new VersionInfo<>(12L, 4L);
        Entity<UsagePointLifeCycleStateInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12/states/4").request().build(HttpMethod.DELETE, json).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(state).remove();
    }

    @Test
    public void testRemoveStateConcurrent() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointStateByIdAndVersion(4L, 3L)).thenReturn(Optional.empty());
        when(usagePointLifeCycleConfigurationService.findUsagePointState(4L)).thenReturn(Optional.of(state));

        UsagePointLifeCycleStateInfo info = new UsagePointLifeCycleStateInfo();
        info.id = 4L;
        info.name = "State";
        info.stage = UsagePointStage.Key.OPERATIONAL;
        info.version = 3L;
        info.parent = new VersionInfo<>(12L, 4L);
        Entity<UsagePointLifeCycleStateInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12/states/4").request().build(HttpMethod.DELETE, json).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<String>get("$.error")).isEqualTo("State has changed since the page was last updated.");
        verify(state, never()).remove();
    }

    @Test
    public void testRemoveStateFailCheck() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointStateByIdAndVersion(4L, 3L)).thenReturn(Optional.of(state));
        doThrow(UsagePointStateRemoveException.stateIsTheLastState(thesaurus)).when(state).remove();

        UsagePointLifeCycleStateInfo info = new UsagePointLifeCycleStateInfo();
        info.id = 4L;
        info.name = "State";
        info.stage = UsagePointStage.Key.OPERATIONAL;
        info.version = 3L;
        info.parent = new VersionInfo<>(12L, 4L);
        Entity<UsagePointLifeCycleStateInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12/states/4").request().build(HttpMethod.DELETE, json).invoke();
        assertThat(response.getStatus()).isEqualTo(422); // why? it should be bad request!
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<String>get("$.message")).isNotNull();
        assertThat(model.<Boolean>get("$.success")).isFalse();
        assertThat(model.<String>get("$.error")).isEqualTo("can.not.remove.last.state");
        verify(state).remove();
    }
}
