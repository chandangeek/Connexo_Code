package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.metering.config.MetrologyConfiguration;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Path("metrologyconfigurations")
public class MetrologyConfigurationResourceTest extends MultisensePublicApiJerseyTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        MetrologyConfiguration metrologyConfiguration = mockMetrologyConfiguration(31, "metro", 1);
    }

    @Test
    public void testAllGetMetrologysPaged() throws Exception {
        MetrologyConfiguration metrologyConfiguration1 = mockMetrologyConfiguration(31, "metro", 1);
        MetrologyConfiguration metrologyConfiguration2 = mockMetrologyConfiguration(32, "metro", 1);
        when(metrologyConfigurationService.findAllMetrologyConfigurations()).thenReturn(Arrays.asList(metrologyConfiguration1, metrologyConfiguration2));
        Response response = target("/metrologyconfigurations").queryParam("start", 0)
                .queryParam("limit", 10)
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/metrologyconfigurations?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(2);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(31);
        assertThat(model.<String>get("data[0].name")).isEqualTo("metro");
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/metrologyconfigurations/31");
    }

    @Test
    public void testGetSingleMetrologyWithFields() throws Exception {
        Response response = target("/metrologyconfigurations/31").queryParam("fields", "id,name").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(31);
        assertThat(model.<Integer>get("$.version")).isNull();
        assertThat(model.<String>get("$.name")).isEqualTo("metro");
        assertThat(model.<String>get("$.link")).isNull();
    }

    @Test
    public void testGetSingleMetrologyAllFields() throws Exception {
        Response response = target("/metrologyconfigurations/31").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(31);
        assertThat(model.<Integer>get("$.version")).isEqualTo(1);
        assertThat(model.<String>get("$.name")).isEqualTo("metro");
        assertThat(model.<String>get("$.link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("$.link.href")).isEqualTo("http://localhost:9998/metrologyconfigurations/31");
    }

    @Test
    public void testMetrologyFields() throws Exception {
        Response response = target("/metrologyconfigurations").request("application/json")
                .method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(8);
        assertThat(model.<List<String>>get("$")).containsOnly("id", "link", "version", "modTime", "name", "userName", "version", "active", "createTime");
    }

}
