package com.elster.jupiter.kore.api.rest.impl;

import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.rest.util.hypermedia.Relation;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
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
        UsagePoint usagePoint = mockUsagePoint(123L, "testUP", 1, ServiceKind.ELECTRICITY);
        when(effectiveMetrologyConfiguration.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(effectiveMetrologyConfiguration.getUsagePoint()).thenReturn(usagePoint);
        when(effectiveMetrologyConfiguration.getRange()).thenReturn(Range.atLeast(Instant.ofEpochMilli(1468933329000L)));
        when(usagePoint.getEffectiveMetrologyConfiguration(any(Instant.class))).thenReturn(Optional.of(effectiveMetrologyConfiguration));

    }

    @Test
    public void testGetSingleMetrologyWithFields() throws Exception {
        Response response = target("usagepoints/123/metrologyconfigurations/1468933329000").queryParam("fields", "id,name")
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        Assertions.assertThat(model.<Long>get("$.id")).isEqualTo(1468933329000L);
        Assertions.assertThat(model.<Integer>get("$.version")).isNull();
        Assertions.assertThat(model.<String>get("$.link")).isNull();
    }

    @Test
    public void testGetSingleMetrologyAllFields() throws Exception {
        Response response = target("usagepoints/123/metrologyconfigurations/1468933329000").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        Assertions.assertThat(model.<Long>get("$.id")).isEqualTo(1468933329000L);
        Assertions.assertThat(model.<Integer>get("$.metrologyConfiguration.id")).isEqualTo(41);
        Assertions.assertThat(model.<Integer>get("$.metrologyConfiguration.version")).isEqualTo(1);
        Assertions.assertThat(model.<String>get("$.link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        Assertions.assertThat(model.<String>get("$.link.href"))
                .isEqualTo("http://localhost:9998/usagepoints/123/metrologyconfigurations/1468933329000");
    }

    @Test
    public void testMetrologyFields() throws Exception {
        Response response = target("usagepoints/123/metrologyconfigurations").request("application/json")
                .method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        Assertions.assertThat(model.<List>get("$")).hasSize(4);
        Assertions.assertThat(model.<List<String>>get("$"))
                .containsOnly("id", "link", "metrologyConfiguration", "purposes");
    }

}
