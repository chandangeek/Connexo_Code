/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.util.IntervalInfo;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeRequest;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class UsagePointLifeCycleStateResourceTest extends PlatformPublicApiJerseyTest {


    UsagePoint usagePoint;
    @Mock
    UsagePointState state;
    @Mock
    UsagePointStage stageUnderConstruction;
    @Mock
    UsagePointStage stageActive;
    @Mock
    UsagePointTransition usagePointTransition;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        usagePoint = mockUsagePoint(MRID, 1, ServiceKind.ELECTRICITY);
        when(meteringService.findAndLockUsagePointByIdAndVersion(41,1)).thenReturn(Optional.of(usagePoint));

        when(state.getId()).thenReturn(1L);
        when(state.getStage()).thenReturn(stageUnderConstruction);
        when(state.getVersion()).thenReturn(1L);
        when(stageUnderConstruction.getKey()).thenReturn(UsagePointStage.Key.PRE_OPERATIONAL);
        when(usagePointLifeCycleConfigurationService.findUsagePointState(1L)).thenReturn(Optional.of(state));
        Finder finder = mock(Finder.class);
        when(usagePointLifeCycleConfigurationService.getUsagePointStates()).thenReturn(finder);
        when(finder.from(any())).thenReturn(finder);
        when(finder.stream()).thenReturn(Stream.of(state));
        when(usagePointLifeCycleConfigurationService.findUsagePointTransition(1L)).thenReturn(Optional.of(usagePointTransition));
        UsagePointStateChangeRequest usagePointStateChangeRequest = mock(UsagePointStateChangeRequest.class);
        when(usagePointStateChangeRequest.getStatus()).thenReturn(UsagePointStateChangeRequest.Status.COMPLETED);
        when(usagePointLifeCycleService.performTransition(usagePoint, usagePointTransition, "INS", Collections.emptyMap())).thenReturn(usagePointStateChangeRequest);

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
        assertThat(model.<String>get("$.stage")).isEqualTo("PRE_OPERATIONAL");
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
    public void testPerformTransition() throws Exception {
        UsagePointTransitionInfo info = new UsagePointTransitionInfo();
        info.transitionNow = true;
        info.id = 1L;
        info.effectiveTimestamp = Instant.ofEpochMilli(1490875811000L);

        // Business method
        Response response = target("/usagepointlifecyclestate/" + MRID + "/transition").request().post(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
}
