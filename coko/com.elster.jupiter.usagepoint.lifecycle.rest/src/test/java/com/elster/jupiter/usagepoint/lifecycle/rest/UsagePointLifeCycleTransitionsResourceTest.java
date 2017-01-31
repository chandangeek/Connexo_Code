/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroAction;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsagePointLifeCycleTransitionsResourceTest extends UsagePointLifeCycleApplicationTest {
    @Mock
    private UsagePointLifeCycle lifeCycle;
    @Mock
    private UsagePointState fromState;
    @Mock
    private UsagePointState toState;
    @Mock
    private UsagePointTransition transition;
    @Mock
    private MicroAction microAction;
    @Mock
    private MicroCheck microCheck;
    @Mock
    private UsagePointStage stage;

    @Before
    public void before() {
        when(usagePointLifeCycleConfigurationService.findUsagePointLifeCycle(12L)).thenReturn(Optional.of(lifeCycle));
        when(usagePointLifeCycleConfigurationService.findUsagePointTransition(6L)).thenReturn(Optional.of(transition));
        when(usagePointLifeCycleConfigurationService.findUsagePointState(56L)).thenReturn(Optional.of(fromState));
        when(usagePointLifeCycleConfigurationService.findUsagePointState(57L)).thenReturn(Optional.of(toState));
        when(lifeCycle.getStates()).thenReturn(Arrays.asList(fromState, toState));
        when(lifeCycle.getTransitions()).thenReturn(Collections.singletonList(transition));
        when(lifeCycle.getId()).thenReturn(12L);
        when(lifeCycle.getVersion()).thenReturn(4L);

        when(fromState.getLifeCycle()).thenReturn(lifeCycle);
        when(fromState.getId()).thenReturn(56L);
        when(fromState.getName()).thenReturn("From");
        when(fromState.getVersion()).thenReturn(1L);
        when(fromState.getStage()).thenReturn(stage);

        when(toState.getLifeCycle()).thenReturn(lifeCycle);
        when(toState.getId()).thenReturn(57L);
        when(toState.getName()).thenReturn("To");
        when(toState.getVersion()).thenReturn(1L);
        when(toState.getStage()).thenReturn(stage);

        when(stage.getKey()).thenReturn(UsagePointStage.Key.OPERATIONAL);

        when(transition.getId()).thenReturn(6L);
        when(transition.getVersion()).thenReturn(7L);
        when(transition.getFrom()).thenReturn(fromState);
        when(transition.getTo()).thenReturn(toState);
        when(transition.getLifeCycle()).thenReturn(lifeCycle);
        when(transition.getName()).thenReturn("Transition");
        when(transition.getLevels()).thenReturn(EnumSet.of(UsagePointTransition.Level.FOUR));
        when(transition.getChecks()).thenReturn(Collections.singleton(microCheck));
        when(transition.getActions()).thenReturn(Collections.singleton(microAction));

        when(microAction.getKey()).thenReturn("actionKey");
        when(microAction.getName()).thenReturn("actionName");
        when(microAction.getDescription()).thenReturn("actionDescription");
        when(microAction.getCategory()).thenReturn("categoryKey");
        when(microAction.getCategoryName()).thenReturn("categoryName");

        when(microCheck.getKey()).thenReturn("checkKey");
        when(microCheck.getName()).thenReturn("checkName");
        when(microCheck.getDescription()).thenReturn("checkDescription");
        when(microCheck.getCategory()).thenReturn("categoryKey");
        when(microCheck.getCategoryName()).thenReturn("categoryName");
    }

    private UsagePointLifeCycleTransitionInfo getInfo() {
        UsagePointLifeCycleTransitionInfo info = new UsagePointLifeCycleTransitionInfo();
        UsagePointLifeCyclePrivilegeInfo privilegeInfo = new UsagePointLifeCyclePrivilegeInfo();
        privilegeInfo.privilege = UsagePointTransition.Level.FOUR.name();
        info.id = 6L;
        info.privileges.add(privilegeInfo);
        MicroActionAndCheckInfo actionInfo = new MicroActionAndCheckInfo();
        actionInfo.key = "actionKey";
        actionInfo.checked = true;
        info.microActions.add(actionInfo);
        MicroActionAndCheckInfo checkInfo = new MicroActionAndCheckInfo();
        checkInfo.key = "checkKey";
        checkInfo.checked = true;
        info.microChecks.add(checkInfo);
        info.fromState = new UsagePointLifeCycleStateInfo();
        info.fromState.id = 56L;
        info.toState = new UsagePointLifeCycleStateInfo();
        info.toState.id = 57L;
        info.parent = new VersionInfo<>(lifeCycle.getId(), lifeCycle.getVersion());
        info.name = "Transition";
        info.version = 7L;
        return info;
    }

    @Test
    public void testGetAllTransitions() {
        String response = target("/lifecycle/12/transitions").request().get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.transitions")).hasSize(1);
        assertThat(model.<Number>get("$.transitions[0].id")).isEqualTo(6);
        assertThat(model.<Number>get("$.transitions[0].fromState.id")).isEqualTo(56);
        assertThat(model.<String>get("$.transitions[0].fromState.name")).isEqualTo("From");
        assertThat(model.<Number>get("$.transitions[0].toState.id")).isEqualTo(57);
        assertThat(model.<String>get("$.transitions[0].toState.name")).isEqualTo("To");
        assertThat(model.<String>get("$.transitions[0].name")).isEqualTo("Transition");
        assertThat(model.<Number>get("$.transitions[0].version")).isEqualTo(7);
        assertThat(model.<Number>get("$.transitions[0].parent.id")).isEqualTo(12);
        assertThat(model.<Number>get("$.transitions[0].parent.version")).isEqualTo(4);
    }

    @Test
    public void testGetAllTransitionsNoLifeCycleFound() throws Exception {
        when(usagePointLifeCycleConfigurationService.findUsagePointLifeCycle(12L)).thenReturn(Optional.empty());

        Response response = target("/lifecycle/12/transitions").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(model.<String>get("$.error")).isEqualTo("no.such.life.cycle");
    }

    @Test
    public void testGetTransitionById() {
        String response = target("/lifecycle/12/transitions/6").request().get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.id")).isEqualTo(6);
        assertThat(model.<Number>get("$.fromState.id")).isEqualTo(56);
        assertThat(model.<String>get("$.fromState.name")).isEqualTo("From");
        assertThat(model.<Number>get("$.toState.id")).isEqualTo(57);
        assertThat(model.<String>get("$.toState.name")).isEqualTo("To");
        assertThat(model.<String>get("$.name")).isEqualTo("Transition");
        assertThat(model.<String>get("$.privileges[0].privilege")).isEqualTo("FOUR");
        assertThat(model.<String>get("$.privileges[0].name")).isEqualTo("FOUR");
        assertThat(model.<String>get("$.microActions[0].key")).isEqualTo("actionKey");
        assertThat(model.<String>get("$.microActions[0].name")).isEqualTo("actionName");
        assertThat(model.<String>get("$.microActions[0].description")).isEqualTo("actionDescription");
        assertThat(model.<String>get("$.microActions[0].category.id")).isEqualTo("categoryKey");
        assertThat(model.<String>get("$.microActions[0].category.name")).isEqualTo("categoryName");
        assertThat(model.<String>get("$.microChecks[0].key")).isEqualTo("checkKey");
        assertThat(model.<String>get("$.microChecks[0].name")).isEqualTo("checkName");
        assertThat(model.<String>get("$.microChecks[0].description")).isEqualTo("checkDescription");
        assertThat(model.<String>get("$.microChecks[0].category.id")).isEqualTo("categoryKey");
        assertThat(model.<String>get("$.microChecks[0].category.name")).isEqualTo("categoryName");
        assertThat(model.<Number>get("$.version")).isEqualTo(7);
        assertThat(model.<Number>get("$.parent.id")).isEqualTo(12);
        assertThat(model.<Number>get("$.parent.version")).isEqualTo(4);
    }

    @Test
    public void testGetTransitionByIdNoLifeCycleFound() throws Exception {
        when(usagePointLifeCycleConfigurationService.findUsagePointTransition(6L)).thenReturn(Optional.empty());

        Response response = target("/lifecycle/12/transitions/6").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(model.<String>get("$.error")).isEqualTo("no.such.life.cycle.transition");
    }

    @Test
    public void testNewTransition() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));
        UsagePointTransition.UsagePointTransitionCreator builder = FakeBuilder.initBuilderStub(transition, UsagePointTransition.UsagePointTransitionCreator.class);
        when(lifeCycle.newTransition("Transition", fromState, toState)).thenReturn(builder);

        Response response = target("/lifecycle/12/transitions").request().post(Entity.json(getInfo()));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.id")).isEqualTo(6);
        assertThat(model.<Number>get("$.fromState.id")).isEqualTo(56);
        assertThat(model.<String>get("$.fromState.name")).isEqualTo("From");
        assertThat(model.<Number>get("$.toState.id")).isEqualTo(57);
        assertThat(model.<String>get("$.toState.name")).isEqualTo("To");
        assertThat(model.<String>get("$.name")).isEqualTo("Transition");
        assertThat(model.<String>get("$.privileges[0].privilege")).isEqualTo("FOUR");
        assertThat(model.<String>get("$.privileges[0].name")).isEqualTo("FOUR");
        assertThat(model.<String>get("$.microActions[0].key")).isEqualTo("actionKey");
        assertThat(model.<String>get("$.microActions[0].name")).isEqualTo("actionName");
        assertThat(model.<String>get("$.microActions[0].description")).isEqualTo("actionDescription");
        assertThat(model.<String>get("$.microActions[0].category.id")).isEqualTo("categoryKey");
        assertThat(model.<String>get("$.microActions[0].category.name")).isEqualTo("categoryName");
        assertThat(model.<String>get("$.microChecks[0].key")).isEqualTo("checkKey");
        assertThat(model.<String>get("$.microChecks[0].name")).isEqualTo("checkName");
        assertThat(model.<String>get("$.microChecks[0].description")).isEqualTo("checkDescription");
        assertThat(model.<String>get("$.microChecks[0].category.id")).isEqualTo("categoryKey");
        assertThat(model.<String>get("$.microChecks[0].category.name")).isEqualTo("categoryName");
        assertThat(model.<Number>get("$.version")).isEqualTo(7);
        assertThat(model.<Number>get("$.parent.id")).isEqualTo(12);
        assertThat(model.<Number>get("$.parent.version")).isEqualTo(4);

        verify(builder).withActions(Collections.singleton(microAction.getKey()));
        verify(builder).withChecks(Collections.singleton(microCheck.getKey()));
        verify(builder).withLevels(EnumSet.of(UsagePointTransition.Level.FOUR));
        verify(builder).complete();
    }

    @Test
    public void testEditTransition() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointTransitionByIdAndVersion(6L, 7L)).thenReturn(Optional.of(transition));
        UsagePointTransition.UsagePointTransitionUpdater builder = FakeBuilder.initBuilderStub(transition, UsagePointTransition.UsagePointTransitionUpdater.class, UsagePointTransition.UsagePointTransitionCreator.class);
        when(transition.startUpdate()).thenReturn(builder);

        Response response = target("/lifecycle/12/transitions/6").request().put(Entity.json(getInfo()));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.id")).isEqualTo(6);
        assertThat(model.<Number>get("$.fromState.id")).isEqualTo(56);
        assertThat(model.<String>get("$.fromState.name")).isEqualTo("From");
        assertThat(model.<Number>get("$.toState.id")).isEqualTo(57);
        assertThat(model.<String>get("$.toState.name")).isEqualTo("To");
        assertThat(model.<String>get("$.name")).isEqualTo("Transition");
        assertThat(model.<String>get("$.privileges[0].privilege")).isEqualTo("FOUR");
        assertThat(model.<String>get("$.privileges[0].name")).isEqualTo("FOUR");
        assertThat(model.<String>get("$.microActions[0].key")).isEqualTo("actionKey");
        assertThat(model.<String>get("$.microActions[0].name")).isEqualTo("actionName");
        assertThat(model.<String>get("$.microActions[0].description")).isEqualTo("actionDescription");
        assertThat(model.<String>get("$.microActions[0].category.id")).isEqualTo("categoryKey");
        assertThat(model.<String>get("$.microActions[0].category.name")).isEqualTo("categoryName");
        assertThat(model.<String>get("$.microChecks[0].key")).isEqualTo("checkKey");
        assertThat(model.<String>get("$.microChecks[0].name")).isEqualTo("checkName");
        assertThat(model.<String>get("$.microChecks[0].description")).isEqualTo("checkDescription");
        assertThat(model.<String>get("$.microChecks[0].category.id")).isEqualTo("categoryKey");
        assertThat(model.<String>get("$.microChecks[0].category.name")).isEqualTo("categoryName");
        assertThat(model.<Number>get("$.version")).isEqualTo(7);
        assertThat(model.<Number>get("$.parent.id")).isEqualTo(12);
        assertThat(model.<Number>get("$.parent.version")).isEqualTo(4);

        verify(builder).withActions(Collections.singleton(microAction.getKey()));
        verify(builder).withChecks(Collections.singleton(microCheck.getKey()));
        verify(builder).withLevels(EnumSet.of(UsagePointTransition.Level.FOUR));
        verify(builder).complete();
    }

    @Test
    public void testEditTransitionConcurrent() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointTransitionByIdAndVersion(6L, 7L)).thenReturn(Optional.empty());

        Response response = target("/lifecycle/12/transitions/6").request().put(Entity.json(getInfo()));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<String>get("$.message")).isEqualTo("Failed to save 'Transition'");
        assertThat(model.<String>get("$.error")).isEqualTo("Transition has changed since the page was last updated.");
    }

    @Test
    public void testDeleteTransition() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointTransitionByIdAndVersion(6L, 7L)).thenReturn(Optional.of(transition));

        Response response = target("/lifecycle/12/transitions/6").request().build(HttpMethod.DELETE, Entity.json(getInfo())).invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());

        verify(transition).remove();
    }

    @Test
    public void testDeleteTransitionConcurrent() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointTransitionByIdAndVersion(6L, 7L)).thenReturn(Optional.empty());

        Response response = target("/lifecycle/12/transitions/6").request().build(HttpMethod.DELETE, Entity.json(getInfo())).invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<String>get("$.message")).isEqualTo("Failed to save 'Transition'");
        assertThat(model.<String>get("$.error")).isEqualTo("Transition has changed since the page was last updated.");
    }
}
