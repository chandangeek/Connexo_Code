/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeRequest;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class UsagePointLifeCycleStateResourceTest extends PlatformPublicApiJerseyTest {


    UsagePoint usagePoint;
    @Mock
    State state;
    @Mock
    Stage  stageUnderConstruction;
    @Mock
    UsagePointTransition usagePointTransition;

    private final Instant effectiveTimestamp = Instant.ofEpochMilli(1490875811000L);

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        usagePoint = mockUsagePoint(MRID, 1, ServiceKind.ELECTRICITY);
        when(meteringService.findAndLockUsagePointByIdAndVersion(41,1)).thenReturn(Optional.of(usagePoint));

        when(state.getId()).thenReturn(1L);
        when(state.getStage()).thenReturn(Optional.of(stageUnderConstruction));
        when(state.getVersion()).thenReturn(1L);
        when(stageUnderConstruction.getName()).thenReturn(UsagePointStage.PRE_OPERATIONAL.getKey());
        when(usagePointLifeCycleConfigurationService.findUsagePointState(1L)).thenReturn(Optional.of(state));
        when(usagePointLifeCycleConfigurationService.getUsagePointStates()).thenReturn(Arrays.asList(state));
        when(usagePointLifeCycleConfigurationService.findUsagePointTransition(1L)).thenReturn(Optional.of(usagePointTransition));
        UsagePointStateChangeRequest usagePointStateChangeRequest = mock(UsagePointStateChangeRequest.class);
        when(usagePointStateChangeRequest.getStatus()).thenReturn(UsagePointStateChangeRequest.Status.COMPLETED);
        when(usagePointLifeCycleService.performTransition(usagePoint, usagePointTransition, "INS", Collections.emptyMap())).thenReturn(usagePointStateChangeRequest);
        when(usagePointLifeCycleService.scheduleTransition(usagePoint, usagePointTransition, effectiveTimestamp, "INS", Collections.emptyMap())).thenReturn(usagePointStateChangeRequest);
    }

    @Test
    public void testGetUsagePointLifeCycleState() throws Exception {

        // Business method
        Response response = target("/usagepointlifecyclestate/1").request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(1);
        assertThat(model.<String>get("$.link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("$.link.href")).isEqualTo("http://localhost:9998/usagepointlifecyclestate/1");
        assertThat(model.<String>get("$.stage")).isEqualTo("mtr.usagepointstage.preoperational");
    }

    @Test
    public void testGetUsagePointLifeCycleStates() throws Exception {
        // Business method
        Response response = target("/usagepointlifecyclestate/").queryParam("limit", 10).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("data")).hasSize(1);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(1);
        assertThat(model.<Integer>get("data[0].version")).isEqualTo(1);
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/usagepointlifecyclestate/1");
    }

    @Test
    public void testUsagePointLifeCycleStateFields() throws Exception {
        // Business method
        Response response = target("/usagepointlifecyclestate").request("application/json").method("PROPFIND", Response.class);

        // Asserts
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(6);
        assertThat(model.<List<String>>get("$")).containsOnly("id", "isInitial", "link", "name", "stage", "version");
    }
    @Test
    public void testScheduleTransition() throws Exception {
        UsagePointTransitionInfo info = new UsagePointTransitionInfo();
        info.id = 1L;
        info.effectiveTimestamp = effectiveTimestamp;

        // Business method
        Response response = target("/usagepointlifecyclestate/" + MRID + "/transition").request().post(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(usagePointLifeCycleService).scheduleTransition(usagePoint, usagePointTransition, effectiveTimestamp, "INS", Collections.emptyMap());
    }
    @Test
    public void testPerformTransition() throws Exception {
        UsagePointTransitionInfo info = new UsagePointTransitionInfo();
        info.transitionNow = true;
        info.id = 1L;

        // Business method
        Response response = target("/usagepointlifecyclestate/" + MRID + "/transition").request().post(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(usagePointLifeCycleService).performTransition(usagePoint, usagePointTransition, "INS", Collections.emptyMap());
    }
}
