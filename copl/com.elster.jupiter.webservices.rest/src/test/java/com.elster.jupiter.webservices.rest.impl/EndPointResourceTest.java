package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 6/8/16.
 */
public class EndPointResourceTest extends WebServicesApplicationTest {

    private InboundEndPointConfiguration inboundEndPointConfiguration;
    private OutboundEndPointConfiguration outboundEndPointConfiguration;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        inboundEndPointConfiguration = mockInboundEndPointConfig(1L, 901L, "metering", "/cim", "CIM");
        outboundEndPointConfiguration = mockOutboundEndPointConfig(2L, 902L, "currency", "http://xe.xom/xe", "XE");

    }

    @Test
    public void testGetAllEndpoints() throws Exception {
        Finder<EndPointConfiguration> finder = mockFinder(Arrays.asList(inboundEndPointConfiguration, outboundEndPointConfiguration));
        when(endPointConfigurationService.findEndPointConfigurations()).thenReturn(finder);
        Response response = target("/endpoints").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("total")).isEqualTo(2);
        assertThat(jsonModel.<Integer>get("endpoints[0].id")).isEqualTo(1);
        assertThat(jsonModel.<Integer>get("endpoints[0].version")).isEqualTo(901);
        assertThat(jsonModel.<String>get("endpoints[0].name")).isEqualTo("metering");
        assertThat(jsonModel.<Boolean>get("endpoints[0].authenticated")).isEqualTo(Boolean.TRUE);
        assertThat(jsonModel.<String>get("endpoints[0].username")).isNull();
        assertThat(jsonModel.<String>get("endpoints[0].password")).isNull();
        assertThat(jsonModel.<Integer>get("endpoints[1].id")).isEqualTo(2);
        assertThat(jsonModel.<Integer>get("endpoints[1].version")).isEqualTo(902);
        assertThat(jsonModel.<String>get("endpoints[1].name")).isEqualTo("currency");
        assertThat(jsonModel.<Boolean>get("endpoints[1].authenticated")).isNull();
        assertThat(jsonModel.<String>get("endpoints[1].username")).isEqualTo("username");
        assertThat(jsonModel.<String>get("endpoints[1].password")).isEqualTo("password");
    }

    @Test
    public void testGetInboundEndpointByName() throws Exception {
        Response response = target("/endpoints/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("id")).isEqualTo(1);
        assertThat(jsonModel.<Integer>get("version")).isEqualTo(901);
        assertThat(jsonModel.<String>get("name")).isEqualTo("metering");
        assertThat(jsonModel.<Boolean>get("authenticated")).isEqualTo(Boolean.TRUE);
        assertThat(jsonModel.<Boolean>get("tracing")).isEqualTo(Boolean.TRUE);
        assertThat(jsonModel.<Boolean>get("active")).isEqualTo(Boolean.TRUE);
        assertThat(jsonModel.<Boolean>get("httpCompression")).isEqualTo(Boolean.TRUE);
        assertThat(jsonModel.<String>get("username")).isNull();
        assertThat(jsonModel.<String>get("password")).isNull();
        assertThat(jsonModel.<String>get("webServiceName")).isEqualTo("CIM");
        assertThat(jsonModel.<String>get("url")).isEqualTo("/cim");
        assertThat(jsonModel.<String>get("logLevel.id")).isEqualTo("INFO");
        assertThat(jsonModel.<String>get("logLevel.displayValue")).isEqualTo("Info");
    }

    @Test
    public void testCreateEndpoint() throws Exception {
        EndPointConfigurationInfo info = new EndPointConfigurationInfo();
        info.type = EndPointConfigType.Inbound;
        info.name = "new endpoint";
        info.logLevel = new IdWithDisplayValueInfo<>();
        info.logLevel.id = "FINEST";
        info.httpCompression = true;
        info.authenticated = false;
        info.url = "/srv";

        InboundEndPointConfiguration endPointConfig = mockInboundEndPointConfig(10, 11, "new", "/url", "new service");
        EndPointConfigurationService.InboundEndPointConfigBuilder builder = FakeBuilder.initBuilderStub(endPointConfig, EndPointConfigurationService.InboundEndPointConfigBuilder.class);
        when(endPointConfigurationService.newInboundEndPointConfiguration(anyString(), anyString(), anyString())).thenReturn(builder);

        Response response = target("/endpoints").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }


}
