/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.fsm.StateChangeBusinessProcess;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroAction;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleRemoveException;

import com.jayway.jsonpath.JsonModel;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsagePointLifeCycleResourceTest extends UsagePointLifeCycleApplicationTest {
    @Mock
    private UsagePointLifeCycle lifeCycle;

    @Test
    public void testGetAllLifeCycles() {
        Finder<UsagePointLifeCycle> finder = FakeBuilder.initBuilderStub(Collections.singletonList(lifeCycle), Finder.class);
        when(usagePointLifeCycleConfigurationService.getUsagePointLifeCycles()).thenReturn(finder);
        when(lifeCycle.getId()).thenReturn(12L);
        when(lifeCycle.getName()).thenReturn("Life cycle");
        when(lifeCycle.getVersion()).thenReturn(4L);

        String response = target("/lifecycle").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.lifeCycles")).hasSize(1);
        assertThat(model.<Number>get("$.lifeCycles[0].id")).isEqualTo(12);
        assertThat(model.<Number>get("$.lifeCycles[0].version")).isEqualTo(4);
        assertThat(model.<String>get("$.lifeCycles[0].name")).isEqualTo("Life cycle");
        assertThat(model.<Boolean>get("$.lifeCycles[0].isDefault")).isEqualTo(false);
    }

    @Test
    public void testGetEmptyList() {
        Finder<UsagePointLifeCycle> finder = FakeBuilder.initBuilderStub(Collections.emptyList(), Finder.class);
        when(usagePointLifeCycleConfigurationService.getUsagePointLifeCycles()).thenReturn(finder);

        String response = target("/lifecycle").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(0);
        assertThat(model.<List>get("$.lifeCycles")).hasSize(0);
    }

    @Test
    public void testGetLifeCycleById() {
        when(usagePointLifeCycleConfigurationService.findUsagePointLifeCycle(12L)).thenReturn(Optional.of(lifeCycle));
        UsagePointState state = mock(UsagePointState.class);
        when(state.getId()).thenReturn(4L);
        when(state.getName()).thenReturn("State");
        when(state.isInitial()).thenReturn(true);
        when(state.getLifeCycle()).thenReturn(lifeCycle);
        when(state.getVersion()).thenReturn(3L);
        when(state.getStage()).thenReturn(mock(UsagePointStage.class));
        when(lifeCycle.getId()).thenReturn(12L);
        when(lifeCycle.getName()).thenReturn("Life cycle");
        when(lifeCycle.getVersion()).thenReturn(4L);
        when(lifeCycle.getStates()).thenReturn(Collections.singletonList(state));

        String response = target("/lifecycle/12").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.id")).isEqualTo(12);
        assertThat(model.<Number>get("$.version")).isEqualTo(4);
        assertThat(model.<String>get("$.name")).isEqualTo("Life cycle");
        assertThat(model.<Number>get("$.states[0].id")).isEqualTo(4);
        assertThat(model.<String>get("$.states[0].name")).isEqualTo("State");
        assertThat(model.<Number>get("$.states[0].version")).isEqualTo(3);
        assertThat(model.<Number>get("$.states[0].parent.id")).isEqualTo(12);
        assertThat(model.<Number>get("$.states[0].parent.version")).isEqualTo(4);
    }

    @Test
    public void testGetLifeCycleByIdUnExisting() throws Exception {
        when(usagePointLifeCycleConfigurationService.findUsagePointLifeCycle(12L)).thenReturn(Optional.empty());

        Response response = target("/lifecycle/12").request().get();
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(model.<String>get("$.message")).isEqualTo("No life cycle with id 12");
        assertThat(model.<String>get("$.error")).isEqualTo("no.such.life.cycle");
    }

    @Test
    public void testUpdateLifeCycle() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));
        when(lifeCycle.getId()).thenReturn(12L);
        when(lifeCycle.getName()).thenReturn("Life cycle changed");
        when(lifeCycle.getVersion()).thenReturn(4L);

        UsagePointLifeCycleInfo info = new UsagePointLifeCycleInfo();
        info.id = 12L;
        info.version = 4L;
        info.name = "Life cycle changed";
        Entity<UsagePointLifeCycleInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12").request().put(json);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.id")).isEqualTo(12);
        assertThat(model.<Number>get("$.version")).isEqualTo(4);
        assertThat(model.<String>get("$.name")).isEqualTo("Life cycle changed");
    }

    @Test
    public void testUpdateLifeCycleConcurrent() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 3L)).thenReturn(Optional.empty());
        when(usagePointLifeCycleConfigurationService.findUsagePointLifeCycle(12L)).thenReturn(Optional.of(lifeCycle));

        UsagePointLifeCycleInfo info = new UsagePointLifeCycleInfo();
        info.id = 12L;
        info.version = 3L;
        info.name = "Life cycle";
        Entity<UsagePointLifeCycleInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12").request().put(json);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        assertThat(model.<String>get("$.message")).isEqualTo("Failed to save 'Life cycle'");
        assertThat(model.<String>get("$.error")).isEqualTo("Life cycle has changed since the page was last updated.");
    }

    @Test
    public void testDeleteLifeCycle() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));

        UsagePointLifeCycleInfo info = new UsagePointLifeCycleInfo();
        info.id = 12L;
        info.version = 4L;
        info.name = "Life cycle";
        Entity<UsagePointLifeCycleInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12").request().build(HttpMethod.DELETE, json).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(lifeCycle).remove();
    }

    @Test
    public void testDeleteLifeCycleConcurrent() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 3L)).thenReturn(Optional.empty());
        when(usagePointLifeCycleConfigurationService.findUsagePointLifeCycle(12L)).thenReturn(Optional.of(lifeCycle));
        when(lifeCycle.getId()).thenReturn(12L);
        when(lifeCycle.getName()).thenReturn("Life cycle");
        when(lifeCycle.getVersion()).thenReturn(4L);

        UsagePointLifeCycleInfo info = new UsagePointLifeCycleInfo();
        info.id = 12L;
        info.version = 3L;
        info.name = "Life cycle";
        Entity<UsagePointLifeCycleInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12").request().build(HttpMethod.DELETE, json).invoke();
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        assertThat(model.<String>get("$.error")).isEqualTo("Life cycle has changed since the page was last updated.");
        verify(lifeCycle, never()).remove();
    }

    @Test
    public void testDeleteLifeCycleFailCheck() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 3L)).thenReturn(Optional.of(lifeCycle));
        doThrow(UsagePointLifeCycleRemoveException.lifeCycleIsDefault(thesaurus)).when(lifeCycle).remove();

        UsagePointLifeCycleInfo info = new UsagePointLifeCycleInfo();
        info.id = 12L;
        info.version = 3L;
        info.name = "Life cycle";
        Entity<UsagePointLifeCycleInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12").request().build(HttpMethod.DELETE, json).invoke();
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(response.getStatus()).isEqualTo(422); // why? it should be bad request!
        assertThat(model.<String>get("$.message")).isNotNull();
        assertThat(model.<Boolean>get("$.success")).isFalse();
        assertThat(model.<String>get("$.error")).isEqualTo("can.not.remove.default.life.cycle");
        verify(lifeCycle).remove();
    }

    @Test
    public void testCreateLifeCycle() throws Exception {
        when(usagePointLifeCycleConfigurationService.newUsagePointLifeCycle("Life cycle")).thenReturn(lifeCycle);
        when(lifeCycle.getId()).thenReturn(12L);
        when(lifeCycle.getName()).thenReturn("Life cycle");
        when(lifeCycle.getVersion()).thenReturn(1L);

        UsagePointLifeCycleInfo info = new UsagePointLifeCycleInfo();
        info.id = 12L;
        info.version = 4L;
        info.name = "Life cycle";
        Entity<UsagePointLifeCycleInfo> json = Entity.json(info);

        Response response = target("/lifecycle").request().post(json);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.id")).isEqualTo(12);
        assertThat(model.<Number>get("$.version")).isEqualTo(1);
        assertThat(model.<String>get("$.name")).isEqualTo("Life cycle");
    }

    @Test
    public void testCreateLifeCycleConstraintViolation() throws Exception {
        ConstraintViolation violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("msg");
        Path nodePath = mock(Path.class);
        when(nodePath.toString()).thenReturn("name");
        when(violation.getPropertyPath()).thenReturn(nodePath);
        ConstraintViolationException violationException = mock(ConstraintViolationException.class);
        when(violationException.getConstraintViolations()).thenReturn(Collections.singleton(violation));
        when(usagePointLifeCycleConfigurationService.newUsagePointLifeCycle(null)).thenThrow(violationException);

        UsagePointLifeCycleInfo info = new UsagePointLifeCycleInfo();
        info.id = 12L;
        Entity<UsagePointLifeCycleInfo> json = Entity.json(info);

        Response response = target("/lifecycle").request().post(json);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(model.<String>get("$.errors[0].id")).isEqualTo("name");
        assertThat(model.<String>get("$.errors[0].msg")).isEqualTo("msg");
    }

    @Test
    public void testCloneLifeCycle() throws Exception {
        UsagePointLifeCycle clone = mock(UsagePointLifeCycle.class);
        when(clone.getId()).thenReturn(13L);
        when(clone.getName()).thenReturn("Clone");
        when(clone.getVersion()).thenReturn(1L);
        when(usagePointLifeCycleConfigurationService.findUsagePointLifeCycle(12L)).thenReturn(Optional.of(lifeCycle));
        when(usagePointLifeCycleConfigurationService.cloneUsagePointLifeCycle("Clone", lifeCycle)).thenReturn(clone);

        UsagePointLifeCycleInfo info = new UsagePointLifeCycleInfo();
        info.id = 12L;
        info.name = "Clone";
        Entity<UsagePointLifeCycleInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12/clone").request().post(json);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.id")).isEqualTo(13);
        assertThat(model.<Number>get("$.version")).isEqualTo(1);
        assertThat(model.<String>get("$.name")).isEqualTo("Clone");
    }

    @Test
    public void testMarkLifeCycleDefault() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));
        when(lifeCycle.getId()).thenReturn(12L);
        when(lifeCycle.getName()).thenReturn("Life cycle");
        when(lifeCycle.isDefault()).thenReturn(true);
        when(lifeCycle.getVersion()).thenReturn(4L);

        UsagePointLifeCycleInfo info = new UsagePointLifeCycleInfo();
        info.id = 12L;
        info.version = 4L;
        info.name = "Life cycle";
        info.isDefault = true;
        Entity<UsagePointLifeCycleInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12/default").request().put(json);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.id")).isEqualTo(12);
        assertThat(model.<Number>get("$.version")).isEqualTo(4);
        assertThat(model.<Boolean>get("$.isDefault")).isEqualTo(true);
    }

    @Test
    public void testMarkLifeCycleDefaultConcurrent() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 3L)).thenReturn(Optional.empty());
        when(usagePointLifeCycleConfigurationService.findUsagePointLifeCycle(12L)).thenReturn(Optional.of(lifeCycle));

        UsagePointLifeCycleInfo info = new UsagePointLifeCycleInfo();
        info.id = 12L;
        info.version = 3L;
        info.name = "Life cycle";
        info.isDefault = true;
        Entity<UsagePointLifeCycleInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12").request().put(json);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        assertThat(model.<String>get("$.message")).isEqualTo("Failed to save 'Life cycle'");
        assertThat(model.<String>get("$.error")).isEqualTo("Life cycle has changed since the page was last updated.");
    }

    @Test
    public void testGetAllProcesses() {
        List<StateChangeBusinessProcess> processes = Arrays.asList(
                mockProcess(1L, "processName 1", "deploymentId 1", "processId 1"),
                mockProcess(2L, "processName 2", "deploymentId 2", "processId 2"));
        when(finiteStateMachineService.findStateChangeBusinessProcesses()).thenReturn(processes);

        String response = target("/lifecycle/processes").request().get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(2);
        assertThat(model.<Number>get("$.processes[0].id")).isEqualTo(1);
        assertThat(model.<String>get("$.processes[0].name")).isEqualTo("processName 1");
        assertThat(model.<String>get("$.processes[0].deploymentId")).isEqualTo("deploymentId 1");
        assertThat(model.<String>get("$.processes[0].processId")).isEqualTo("processId 1");
        assertThat(model.<Number>get("$.processes[1].id")).isEqualTo(2);
        assertThat(model.<String>get("$.processes[1].name")).isEqualTo("processName 2");
        assertThat(model.<String>get("$.processes[1].deploymentId")).isEqualTo("deploymentId 2");
        assertThat(model.<String>get("$.processes[1].processId")).isEqualTo("processId 2");
    }

    @Test
    public void testGetAllPrivileges() {
        String response = target("/lifecycle/privileges").request().get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(UsagePointTransition.Level.values().length);
        assertThat(model.<Number>get("$.privileges[0].privilege")).isEqualTo(UsagePointTransition.Level.ONE.name());
        assertThat(model.<String>get("$.privileges[0].name")).isEqualTo(UsagePointTransition.Level.ONE.name());
    }

    @Test
    public void testGetAllMicroActions() {
        UsagePointState usagePointState = mock(UsagePointState.class);
        when(usagePointLifeCycleConfigurationService.findUsagePointState(23L)).thenReturn(Optional.of(usagePointState));
        MicroAction microAction = mock(MicroAction.class);
        when(usagePointLifeCycleConfigurationService.getMicroActions()).thenReturn(Collections.singleton(microAction));
        when(microAction.getKey()).thenReturn("actionKey");
        when(microAction.getName()).thenReturn("actionName");
        when(microAction.getDescription()).thenReturn("actionDescription");
        when(microAction.getCategory()).thenReturn("categoryKey");
        when(microAction.getCategoryName()).thenReturn("categoryName");

        String response = target("/lifecycle/microActions").queryParam("fromState", "23").queryParam("toState", "23").request().get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<String>get("$.microActions[0].key")).isEqualTo("actionKey");
        assertThat(model.<String>get("$.microActions[0].name")).isEqualTo("actionName");
        assertThat(model.<String>get("$.microActions[0].description")).isEqualTo("actionDescription");
        assertThat(model.<String>get("$.microActions[0].category.id")).isEqualTo("categoryKey");
        assertThat(model.<String>get("$.microActions[0].category.name")).isEqualTo("categoryName");
        assertThat(model.<Boolean>get("$.microActions[0].isRequired")).isEqualTo(false);
    }


    @Test
    public void testGetAllMicroChecks() {
        UsagePointState usagePointState = mock(UsagePointState.class);
        when(usagePointLifeCycleConfigurationService.findUsagePointState(23L)).thenReturn(Optional.of(usagePointState));
        MicroCheck microCheck = mock(MicroCheck.class);
        when(usagePointLifeCycleConfigurationService.getMicroChecks()).thenReturn(Collections.singleton(microCheck));
        when(microCheck.getKey()).thenReturn("checkKey");
        when(microCheck.getName()).thenReturn("checkName");
        when(microCheck.getDescription()).thenReturn("checkDescription");
        when(microCheck.getCategory()).thenReturn("categoryKey");
        when(microCheck.getCategoryName()).thenReturn("categoryName");

        String response = target("/lifecycle/microChecks").queryParam("fromState", "23").queryParam("toState", "23").request().get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<String>get("$.microChecks[0].key")).isEqualTo("checkKey");
        assertThat(model.<String>get("$.microChecks[0].name")).isEqualTo("checkName");
        assertThat(model.<String>get("$.microChecks[0].description")).isEqualTo("checkDescription");
        assertThat(model.<String>get("$.microChecks[0].category.id")).isEqualTo("categoryKey");
        assertThat(model.<String>get("$.microChecks[0].category.name")).isEqualTo("categoryName");
        assertThat(model.<Boolean>get("$.microChecks[0].isRequired")).isEqualTo(false);
    }
}
