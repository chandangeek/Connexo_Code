/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.rest.util.IdWithLocalizedValueInfo;
import com.elster.jupiter.rest.util.LongIdWithNameInfo;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceProtocol;
import com.elster.jupiter.users.Group;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 6/8/16.
 */
public class EndPointConfigurationResourceTest extends WebServicesApplicationTest {

    private InboundEndPointConfiguration inboundEndPointConfiguration;
    private OutboundEndPointConfiguration outboundEndPointConfiguration;
    private InboundEndPointConfiguration inboundRestEndPointConfiguration;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        inboundEndPointConfiguration = mockInboundEndPointConfig(1L, 901L, "metering", "/cim", "someInboundService");
        outboundEndPointConfiguration = mockOutboundEndPointConfig(2L, 902L, "currency", "http://xe.xom/xe", "someOutboundService");
        inboundRestEndPointConfiguration = mockInboundEndPointConfig(3L, 903L, "metering/rest", "/cim", "someRestService");
        when(webServicesService.getWebService("someInboundService")).thenReturn(Optional.of(new WebService() {
            @Override
            public String getName() {
                return "someInboundService";
            }

            @Override
            public boolean isInbound() {
                return true;
            }

            @Override
            public WebServiceProtocol getProtocol() {
                return WebServiceProtocol.SOAP;
            }
        }));
        when(webServicesService.getWebService("someOutboundService")).thenReturn(Optional.of(new WebService() {
            @Override
            public String getName() {
                return "someOutboundService";
            }

            @Override
            public boolean isInbound() {
                return false;
            }

            @Override
            public WebServiceProtocol getProtocol() {
                return WebServiceProtocol.SOAP;
            }
        }));
        when(webServicesService.getWebService("someRestService")).thenReturn(Optional.of(new WebService() {
            @Override
            public String getName() {
                return "someRestService";
            }

            @Override
            public boolean isInbound() {
                return true;
            }

            @Override
            public WebServiceProtocol getProtocol() {
                return WebServiceProtocol.REST;
            }
        }));
    }

    @Test
    public void testGetAllEndpoints() throws Exception {
        Finder<EndPointConfiguration> finder = mockFinder(Arrays.asList(inboundEndPointConfiguration, outboundEndPointConfiguration));
        when(endPointConfigurationService.findEndPointConfigurations()).thenReturn(finder);
        Response response = target("/endpointconfigurations").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("total")).isEqualTo(2);
        assertThat(jsonModel.<Integer>get("endpoints[0].id")).isEqualTo(1);
        assertThat(jsonModel.<Integer>get("endpoints[0].version")).isEqualTo(901);
        assertThat(jsonModel.<String>get("endpoints[0].name")).isEqualTo("metering");
        assertThat(jsonModel.<String>get("endpoints[0].authenticationMethod.id")).isEqualTo("NONE");
        assertThat(jsonModel.<String>get("endpoints[0].username")).isNull();
        assertThat(jsonModel.<String>get("endpoints[0].password")).isNull();
        assertThat(jsonModel.<Boolean>get("endpoints[0].tracing")).isTrue();
        assertThat(jsonModel.<String>get("endpoints[0].traceFile")).isEqualTo("webservices.log");
        assertThat(jsonModel.<Integer>get("endpoints[1].id")).isEqualTo(2);
        assertThat(jsonModel.<Integer>get("endpoints[1].version")).isEqualTo(902);
        assertThat(jsonModel.<String>get("endpoints[1].name")).isEqualTo("currency");
        assertThat(jsonModel.<Boolean>get("endpoints[1].tracing")).isTrue();
        assertThat(jsonModel.<String>get("endpoints[1].traceFile")).isEqualTo("webservices.log");
        assertThat(jsonModel.<String>get("endpoints[1].username")).isEqualTo("username");
        assertThat(jsonModel.<String>get("endpoints[1].password")).isEqualTo("password");
    }

    @Test
    public void testGetInboundEndpointByName() throws Exception {
        Response response = target("/endpointconfigurations/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("id")).isEqualTo(1);
        assertThat(jsonModel.<Integer>get("version")).isEqualTo(901);
        assertThat(jsonModel.<String>get("name")).isEqualTo("metering");
        assertThat(jsonModel.<String>get("authenticationMethod.id")).isEqualTo("NONE");
        assertThat(jsonModel.<Boolean>get("tracing")).isEqualTo(Boolean.TRUE);
        assertThat(jsonModel.<Boolean>get("active")).isEqualTo(Boolean.TRUE);
        assertThat(jsonModel.<Boolean>get("available")).isEqualTo(Boolean.TRUE);
        assertThat(jsonModel.<Boolean>get("httpCompression")).isEqualTo(Boolean.TRUE);
        assertThat(jsonModel.<String>get("username")).isNull();
        assertThat(jsonModel.<String>get("password")).isNull();
        assertThat(jsonModel.<Boolean>get("tracing")).isTrue();
        assertThat(jsonModel.<String>get("traceFile")).isEqualTo("webservices.log");
        assertThat(jsonModel.<String>get("webServiceName")).isEqualTo("someInboundService");
        assertThat(jsonModel.<String>get("url")).isEqualTo("/cim");
        assertThat(jsonModel.<String>get("previewUrl")).isEqualTo("http://localhost:9998/soap/cim");
        assertThat(jsonModel.<String>get("logLevel.id")).isEqualTo("INFO");
        assertThat(jsonModel.<String>get("logLevel.localizedValue")).isEqualTo("Information");
    }

    @Test
    public void testGetInboundRestEndpointById() throws Exception {
        Response response = target("/endpointconfigurations/3").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("id")).isEqualTo(3);
        assertThat(jsonModel.<Integer>get("version")).isEqualTo(903);
        assertThat(jsonModel.<String>get("name")).isEqualTo("metering/rest");
        assertThat(jsonModel.<String>get("authenticationMethod.id")).isEqualTo("NONE");
        assertThat(jsonModel.<Boolean>get("tracing")).isEqualTo(Boolean.TRUE);
        assertThat(jsonModel.<Boolean>get("active")).isEqualTo(Boolean.TRUE);
        assertThat(jsonModel.<Boolean>get("available")).isEqualTo(Boolean.TRUE);
        assertThat(jsonModel.<Boolean>get("httpCompression")).isEqualTo(Boolean.TRUE);
        assertThat(jsonModel.<String>get("username")).isNull();
        assertThat(jsonModel.<String>get("password")).isNull();
        assertThat(jsonModel.<Boolean>get("tracing")).isTrue();
        assertThat(jsonModel.<String>get("traceFile")).isEqualTo("webservices.log");
        assertThat(jsonModel.<String>get("webServiceName")).isEqualTo("someRestService");
        assertThat(jsonModel.<String>get("url")).isEqualTo("/cim");
        assertThat(jsonModel.<String>get("previewUrl")).isEqualTo("http://localhost:9998/rest/cim");
        assertThat(jsonModel.<String>get("logLevel.id")).isEqualTo("INFO");
        assertThat(jsonModel.<String>get("logLevel.localizedValue")).isEqualTo("Information");
    }

    @Test
    public void testCreateEndpoint() throws Exception {
        EndPointConfigurationInfo info = new EndPointConfigurationInfo();
        info.name = "new endpoint";
        info.webServiceName = "someInboundService";
        info.logLevel = new IdWithLocalizedValueInfo<>();
        info.logLevel.id = "FINEST";
        info.httpCompression = true;
        info.schemaValidation = true;
        info.tracing = false;
        info.authenticationMethod = new IdWithLocalizedValueInfo<>(EndPointAuthentication.NONE, null);

        info.url = "/srv";
        info.active = false;

        InboundEndPointConfiguration endPointConfig = mockInboundEndPointConfig(10, 11, "new", "/url", "someInboundService");
        EndPointConfigurationService.InboundEndPointConfigBuilder builder = FakeBuilder.initBuilderStub(endPointConfig, EndPointConfigurationService.InboundEndPointConfigBuilder.class);
        when(endPointConfigurationService.newInboundEndPointConfiguration(anyString(), anyString(), anyString())).thenReturn(builder);

        Response response = target("/endpointconfigurations").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testCreateEndpointWithAuthentication() throws Exception {
        EndPointConfigurationInfo info = new EndPointConfigurationInfo();

        Group group = mock(Group.class);
        when(group.getId()).thenReturn(1L);
        info.name = "new endpoint";
        info.webServiceName = "someInboundService";
        info.logLevel = new IdWithLocalizedValueInfo<>();
        info.logLevel.id = "FINEST";
        info.httpCompression = true;
        info.schemaValidation = true;
        info.tracing = false;
        info.authenticationMethod = new IdWithLocalizedValueInfo<>(EndPointAuthentication.BASIC_AUTHENTICATION, null);
        info.url = "/srv";
        info.group = new LongIdWithNameInfo(group.getId(), "none");
        info.active = false;
        when(userService.getGroup(1L)).thenReturn(Optional.of(group));

        InboundEndPointConfiguration endPointConfig = mockInboundEndPointConfig(10, 11, "new", "/url", "someInboundService");
        EndPointConfigurationService.InboundEndPointConfigBuilder builder = FakeBuilder.initBuilderStub(endPointConfig, EndPointConfigurationService.InboundEndPointConfigBuilder.class);
        when(endPointConfigurationService.newInboundEndPointConfiguration(anyString(), anyString(), anyString())).thenReturn(builder);

        Response response = target("/endpointconfigurations").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testUpdateInActiveInboundEndPoint() throws Exception {
        when(inboundEndPointConfiguration.isActive()).thenReturn(false);

        EndPointConfigurationInfo info = new EndPointConfigurationInfo();
        info.version = 901L;
        info.webServiceName = "someInboundService";
        info.name = "new endpoint";
        info.logLevel = new IdWithLocalizedValueInfo<>();
        info.logLevel.id = "SEVERE";
        info.httpCompression = true;
        info.schemaValidation = true;
        info.tracing = true;
        info.traceFile = "yyy";
        info.authenticationMethod = new IdWithLocalizedValueInfo<>(EndPointAuthentication.NONE, null);
        info.active = false;
        info.url = "/srv";

        Response response = target("/endpointconfigurations/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(inboundEndPointConfiguration).setAuthenticationMethod(EndPointAuthentication.NONE);
        verify(inboundEndPointConfiguration).setHttpCompression(true);
        verify(inboundEndPointConfiguration).setSchemaValidation(true);
        verify(inboundEndPointConfiguration).setTracing(true);
        verify(inboundEndPointConfiguration).setTraceFile("yyy");
        verify(inboundEndPointConfiguration).setName("new endpoint");
        verify(inboundEndPointConfiguration).setLogLevel(LogLevel.SEVERE);
        verify(inboundEndPointConfiguration).setUrl("/srv");
        verify(inboundEndPointConfiguration).save();

    }

    @Test
    public void testUpdateInActiveWithGroupInboundEndPoint() throws Exception {
        when(inboundEndPointConfiguration.isActive()).thenReturn(false);
        Group group = mock(Group.class);
        when(userService.getGroup(123L)).thenReturn(Optional.of(group));

        EndPointConfigurationInfo info = new EndPointConfigurationInfo();
        info.version = 901L;
        info.webServiceName = "someInboundService";
        info.group = new LongIdWithNameInfo(123L, null);
        info.name = "new endpoint";
        info.logLevel = new IdWithLocalizedValueInfo<>();
        info.logLevel.id = "SEVERE";
        info.httpCompression = true;
        info.schemaValidation = true;
        info.tracing = true;
        info.traceFile = "yyy";
        info.authenticationMethod = new IdWithLocalizedValueInfo<>(EndPointAuthentication.BASIC_AUTHENTICATION, null);
        info.active = false;
        info.url = "/srv";

        Response response = target("/endpointconfigurations/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(inboundEndPointConfiguration).setAuthenticationMethod(EndPointAuthentication.BASIC_AUTHENTICATION);
        verify(inboundEndPointConfiguration).setGroup(group);
        verify(inboundEndPointConfiguration).setHttpCompression(true);
        verify(inboundEndPointConfiguration).setSchemaValidation(true);
        verify(inboundEndPointConfiguration).setTracing(true);
        verify(inboundEndPointConfiguration).setTraceFile("yyy");
        verify(inboundEndPointConfiguration).setName("new endpoint");
        verify(inboundEndPointConfiguration).setLogLevel(LogLevel.SEVERE);
        verify(inboundEndPointConfiguration).setUrl("/srv");
        verify(inboundEndPointConfiguration).save();

    }

    @Test
    public void testUpdateInActiveWithAuthenticationWithoutGroupInboundEndPoint() throws Exception {
        when(inboundEndPointConfiguration.isActive()).thenReturn(false);

        EndPointConfigurationInfo info = new EndPointConfigurationInfo();
        info.version = 901L;
        info.webServiceName = "someInboundService";
        info.name = "new endpoint";
        info.logLevel = new IdWithLocalizedValueInfo<>();
        info.logLevel.id = "SEVERE";
        info.httpCompression = true;
        info.schemaValidation = true;
        info.tracing = true;
        info.traceFile = "yyy";
        info.authenticationMethod = new IdWithLocalizedValueInfo<>(EndPointAuthentication.BASIC_AUTHENTICATION, null);
        info.group = null;
        info.active = false;
        info.url = "/srv";

        Response response = target("/endpointconfigurations/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(inboundEndPointConfiguration).setGroup(null);
    }

    @Test
    public void testUpdateInActiveOutboundEndPoint() throws Exception {
        when(outboundEndPointConfiguration.isActive()).thenReturn(false);

        EndPointConfigurationInfo info = new EndPointConfigurationInfo();
        info.version = 902L;
        info.webServiceName = "someOutboundService";
        info.name = "new endpoint";
        info.logLevel = new IdWithLocalizedValueInfo<>("SEVERE", null);
        info.httpCompression = true;
        info.schemaValidation = true;
        info.tracing = true;
        info.traceFile = "xxx";
        info.authenticationMethod = new IdWithLocalizedValueInfo<>(EndPointAuthentication.BASIC_AUTHENTICATION, null);
        info.username = "u";
        info.password = "p";
        info.active = false;
        info.url = "/srv";

        Response response = target("/endpointconfigurations/2").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(outboundEndPointConfiguration).setUsername("u");
        verify(outboundEndPointConfiguration).setPassword("p");
        verify(outboundEndPointConfiguration).setHttpCompression(true);
        verify(outboundEndPointConfiguration).setSchemaValidation(true);
        verify(outboundEndPointConfiguration).setTracing(true);
        verify(outboundEndPointConfiguration).setTraceFile("xxx");
        verify(outboundEndPointConfiguration).setName("new endpoint");
        verify(outboundEndPointConfiguration).setLogLevel(LogLevel.SEVERE);
        verify(outboundEndPointConfiguration).setUrl("/srv");
        verify(outboundEndPointConfiguration).save();

    }
}
