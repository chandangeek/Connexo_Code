package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeRequest;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.usagepoint.lifecycle.rest.impl.UsagePointStateChangeRequestInfo;
import com.elster.jupiter.users.User;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsagePointStateChangeRequestResourceTest extends UsagePointLifeCycleApplicationTest {
    public static final String APPLICATION = "TST";
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private UsagePointState fromState;
    @Mock
    private UsagePointState toState;
    @Mock
    private UsagePointTransition transition;

    @Before
    public void before() {
        when(fromState.getId()).thenReturn(56L);
        when(fromState.getName()).thenReturn("From");
        when(fromState.getVersion()).thenReturn(1L);

        when(toState.getId()).thenReturn(57L);
        when(toState.getName()).thenReturn("To");
        when(toState.getVersion()).thenReturn(1L);

        when(transition.getId()).thenReturn(6L);
        when(transition.getVersion()).thenReturn(7L);
        when(transition.getFrom()).thenReturn(fromState);
        when(transition.getTo()).thenReturn(toState);
        when(transition.getName()).thenReturn("Transition");

        when(usagePoint.getId()).thenReturn(21L);
        when(usagePoint.getVersion()).thenReturn(5L);
        when(usagePoint.getName()).thenReturn("UsagePoint");
        when(usagePoint.getState()).thenReturn(fromState);

        when(meteringService.findUsagePointByName("UsagePoint")).thenReturn(Optional.of(usagePoint));
        when(meteringService.findAndLockUsagePointByNameAndVersion("UsagePoint", 5L)).thenReturn(Optional.of(usagePoint));
        when(usagePointLifeCycleConfigurationService.findUsagePointTransition(6L)).thenReturn(Optional.of(transition));
    }

    private UsagePointStateChangeRequest mockUsagePointChangeRequest() {
        User originator = mock(User.class);
        Instant now = Instant.now();
        UsagePointStateChangeRequest changeRequest = mock(UsagePointStateChangeRequest.class);
        when(changeRequest.getId()).thenReturn(1L);
        when(changeRequest.getOriginator()).thenReturn(originator);
        when(changeRequest.getUsagePoint()).thenReturn(usagePoint);
        when(changeRequest.getFromStateName()).thenReturn("From");
        when(changeRequest.getToStateName()).thenReturn("To");
        when(changeRequest.getStatus()).thenReturn(UsagePointStateChangeRequest.Status.COMPLETED);
        when(changeRequest.getStatusName()).thenReturn("Completed");
        when(changeRequest.getType()).thenReturn(UsagePointStateChangeRequest.Type.STATE_CHANGE);
        when(changeRequest.getTypeName()).thenReturn("State change");
        when(changeRequest.getScheduleTime()).thenReturn(now.minus(1, ChronoUnit.HOURS));
        when(changeRequest.getTransitionTime()).thenReturn(now);
        return changeRequest;
    }

    @Test
    public void testGetAvailableTransitions() {
        when(usagePointLifeCycleService.getAvailableTransitions(fromState, APPLICATION)).thenReturn(Collections.singletonList(transition));
        String response = target("/usagepoint/UsagePoint/transitions").request().header("X-CONNEXO-APPLICATION-NAME", APPLICATION).get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.transitions")).hasSize(1);
        assertThat(model.<Number>get("$.transitions[0].id")).isEqualTo(6);
        assertThat(model.<String>get("$.transitions[0].name")).isEqualTo("Transition");
    }

    @Test
    public void testGetAvailableTransitionsNoUsagePoint() throws Exception {
        when(meteringService.findUsagePointByName("UsagePoint")).thenReturn(Optional.empty());
        Response response = target("/usagepoint/UsagePoint/transitions").request().header("X-CONNEXO-APPLICATION-NAME", "TST").get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(model.<String>get("$.error")).isEqualTo("no.such.usage.point");
    }

    @Test
    public void testGetPropertiesForTransition() {
        String response = target("/usagepoint/UsagePoint/transitions/6").request().header("X-CONNEXO-APPLICATION-NAME", APPLICATION).get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.id")).isEqualTo(6);
        assertThat(model.<String>get("$.name")).isEqualTo("Transition");
        assertThat(model.<List>get("$.properties")).isNotNull();
    }

    @Test
    public void testGetPropertiesForTransitionNoTransition() throws Exception {
        when(usagePointLifeCycleConfigurationService.findUsagePointTransition(6L)).thenReturn(Optional.empty());
        Response response = target("/usagepoint/UsagePoint/transitions/6").request().header("X-CONNEXO-APPLICATION-NAME", APPLICATION).get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(model.<String>get("$.error")).isEqualTo("no.such.life.cycle.transition");
    }

    @Test
    public void testPerformTransitionConcurrent() throws Exception {
        when(meteringService.findAndLockUsagePointByNameAndVersion("UsagePoint", 5L)).thenReturn(Optional.empty());
        UsagePointTransitionInfo info = new UsagePointTransitionInfo();
        info.usagePoint = new UsagePointStateChangeRequestInfo.UsagePointInfo();
        info.usagePoint.version = 5L;
        info.usagePoint.name = "UsagePoint";
        Response response = target("/usagepoint/UsagePoint/transitions/6").request().header("X-CONNEXO-APPLICATION-NAME", APPLICATION).put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<String>get("$.message")).isEqualTo("Failed to save 'UsagePoint'");
        assertThat(model.<String>get("$.error")).isEqualTo("UsagePoint has changed since the page was last updated.");
    }

    @Test
    public void testPerformTransition() throws Exception {
        UsagePointStateChangeRequest changeRequest = mockUsagePointChangeRequest();
        when(usagePointLifeCycleService.performTransition(usagePoint, transition, APPLICATION, Collections.emptyMap())).thenReturn(changeRequest);
        UsagePointTransitionInfo info = new UsagePointTransitionInfo();
        info.usagePoint = new UsagePointStateChangeRequestInfo.UsagePointInfo();
        info.usagePoint.version = 5L;
        info.usagePoint.name = "UsagePoint";
        info.id = 5;
        info.transitionNow = true;
        Response response = target("/usagepoint/UsagePoint/transitions/6").request().header("X-CONNEXO-APPLICATION-NAME", APPLICATION).put(Entity.json(info));
        verify(usagePointLifeCycleService).performTransition(usagePoint, transition, APPLICATION, Collections.emptyMap());

        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.id")).isEqualTo(1);
        assertThat(model.<String>get("$.fromStateName")).isEqualTo("From");
        assertThat(model.<String>get("$.toStateName")).isEqualTo("To");
        assertThat(model.<Number>get("$.user.id")).isNotNull();
        assertThat(model.<String>get("$.status.id")).isEqualTo("COMPLETED");
        assertThat(model.<String>get("$.status.displayValue")).isEqualTo("Completed");
        assertThat(model.<String>get("$.usagePoint.name")).isEqualTo("UsagePoint");
        assertThat(model.<Number>get("$.usagePoint.version")).isEqualTo(5);
        assertThat(model.<List>get("$.microChecks")).isNotNull();
    }

    @Test
    public void testGetHistory() throws Exception {
        UsagePointStateChangeRequest changeRequest = mockUsagePointChangeRequest();
        when(usagePointLifeCycleService.getHistory(usagePoint)).thenReturn(Collections.singletonList(changeRequest));

        Response response = target("/usagepoint/UsagePoint/transitions/history").request().header("X-CONNEXO-APPLICATION-NAME", APPLICATION).get();

        verify(usagePointLifeCycleService).getHistory(usagePoint);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.history[0].id")).isEqualTo(1);
        assertThat(model.<String>get("$.history[0].fromStateName")).isEqualTo("From");
        assertThat(model.<String>get("$.history[0].toStateName")).isEqualTo("To");
        assertThat(model.<Number>get("$.history[0].user.id")).isNotNull();
        assertThat(model.<String>get("$.history[0].status.id")).isEqualTo("COMPLETED");
        assertThat(model.<String>get("$.history[0].status.displayValue")).isEqualTo("Completed");
        assertThat(model.<String>get("$.history[0].usagePoint.name")).isEqualTo("UsagePoint");
        assertThat(model.<Number>get("$.history[0].usagePoint.version")).isEqualTo(5);
        assertThat(model.<List>get("$.history[0].microChecks")).isNotNull();
    }

    @Test
    public void testCancelScheduledChangeRequest() throws Exception {
        UsagePointStateChangeRequest changeRequest = mockUsagePointChangeRequest();
        when(changeRequest.getStatus()).thenReturn(UsagePointStateChangeRequest.Status.CANCELLED);
        when(changeRequest.getStatusName()).thenReturn("Aborted");
        when(usagePointLifeCycleService.getHistory(usagePoint)).thenReturn(Collections.singletonList(changeRequest));

        Response response = target("/usagepoint/UsagePoint/transitions/history/1").request().put(Entity.json(null));

        verify(changeRequest).cancel();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
}
