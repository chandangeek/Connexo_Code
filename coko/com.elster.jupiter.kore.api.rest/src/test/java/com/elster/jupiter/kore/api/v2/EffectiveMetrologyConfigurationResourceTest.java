/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


public class EffectiveMetrologyConfigurationResourceTest extends PlatformPublicApiJerseyTest {

    @Mock
    EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfiguration(41, "metro", 1);
        UsagePoint usagePoint = mockUsagePoint(MRID, 1, ServiceKind.ELECTRICITY);
        when(effectiveMetrologyConfiguration.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(effectiveMetrologyConfiguration.getUsagePoint()).thenReturn(usagePoint);
        when(effectiveMetrologyConfiguration.getRange()).thenReturn(Range.atLeast(Instant.ofEpochMilli(1468933329000L)));
        when(usagePoint.getEffectiveMetrologyConfiguration(any(Instant.class))).thenReturn(Optional.of(effectiveMetrologyConfiguration));
    }

    @Test
    public void testGetSingleMetrologyWithFields() throws Exception {
        // Business method
        Response response = target("/usagepoints/" + MRID + "/metrologyconfigurations/1468933329000")
                .queryParam("fields", "id,name")
                .request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Long>get("$.id")).isEqualTo(1468933329000L);
        assertThat(model.<Integer>get("$.version")).isNull();
        assertThat(model.<String>get("$.link")).isNull();
    }

    @Test
    public void testGetSingleMetrologyAllFields() throws Exception {
        // Business method
        Response response = target("/usagepoints/" + MRID + "/metrologyconfigurations/1468933329000").request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Long>get("$.id")).isEqualTo(1468933329000L);
        assertThat(model.<Integer>get("$.metrologyConfiguration.id")).isEqualTo(41);
        assertThat(model.<Integer>get("$.metrologyConfiguration.version")).isEqualTo(1);
        assertThat(model.<String>get("$.link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("$.link.href")).isEqualTo("http://localhost:9998/usagepoints/" + MRID + "/metrologyconfigurations/1468933329000");
    }

    @Test
    public void testMetrologyFields() throws Exception {
        // Business method
        Response response = target("/usagepoints/" + MRID + "/metrologyconfigurations").request("application/json").method("PROPFIND", Response.class);

        // Asserts
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(4);
        assertThat(model.<List<String>>get("$")).containsOnly("id", "link", "metrologyConfiguration", "purposes");
    }
}
