package com.energyict.mdc.rest.impl;

import com.energyict.cbo.TimeDuration;
import com.energyict.mdc.servers.ComServer;
import com.energyict.mdc.servers.OnlineComServer;
import com.energyict.mdc.services.ComServerService;
import com.energyict.mdc.shadow.servers.ComServerShadow;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import org.fest.assertions.data.MapEntry;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ComServerResourceTest extends JerseyTest {

    ComServerService comServerService;



    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        comServerService = mock(ComServerService.class);
        ComServerServiceHolder.setComServerService(comServerService);
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(ComServerResource.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {

        config.register(JacksonJsonProvider.class);
        config.register(JacksonJaxbJsonProvider.class);
        super.configureClient(config);
    }

    @Test
    public void guardGetComServersJavaScriptMappings() {
        OnlineComServer mock = mock(OnlineComServer.class);
        List<ComServer<? extends ComServerShadow>> comServers = new ArrayList<>();
        comServers.add(mock);
        when(comServerService.findAll()).thenReturn(comServers);
        when(mock.getName()).thenReturn("Test");
        when(mock.getQueryApiPostUri()).thenReturn("/test");
        when(mock.getId()).thenReturn(1);
        when(mock.getEventRegistrationUri()).thenReturn("/event/registration/uri");
        when(mock.getNumberOfStoreTaskThreads()).thenReturn(2);
        when(mock.getStoreTaskQueueSize()).thenReturn(3);
        when(mock.getStoreTaskThreadPriority()).thenReturn(4);
        when(mock.getChangesInterPollDelay()).thenReturn(new TimeDuration("6 seconds"));
        when(mock.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.ERROR);
        when(mock.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(mock.getSchedulingInterPollDelay()).thenReturn(new TimeDuration("7 minutes"));

        final Map<String, Object> response = target("/comservers").request().get(Map.class);
        assertThat(response).describedAs("Should contain field 'comServers'").containsKey("comServers").hasSize(1);
        List<Map<String, Object>> comServers1 = (List<Map<String, Object>>) response.get("comServers");
        assertThat(comServers1).describedAs("Expected only 1 comServer").hasSize(1);
        Map<String, Object> comServer1 = comServers1.get(0);
        assertThat(comServer1)
                .contains(MapEntry.entry("id", 1))
                .contains(MapEntry.entry("name", "Test"))
                .contains(MapEntry.entry("queryAPIPostUri", "/test"))
                .contains(MapEntry.entry("eventRegistrationUri", "/event/registration/uri"))
                .contains(MapEntry.entry("numberOfStoreTaskThreads", 2))
                .contains(MapEntry.entry("storeTaskQueueSize", 3))
                .contains(MapEntry.entry("storeTaskThreadPriority", 4))
                .containsKey("changesInterPollDelay")
                .containsKey("schedulingInterPollDelay")
                .contains(MapEntry.entry("communicationLogLevel", "ERROR"))
                .contains(MapEntry.entry("serverLogLevel", "INFO"))
                ;

        Map<String, Object> changesInterPollDelay = (Map<String, Object>) comServer1.get("changesInterPollDelay");
        assertThat(changesInterPollDelay).hasSize(2)
                .contains(MapEntry.entry("count", 6))
                .contains(MapEntry.entry("timeUnit", "seconds"));
        Map<String, Object> schedulingInterPollDelay = (Map<String, Object>) comServer1.get("schedulingInterPollDelay");
        assertThat(schedulingInterPollDelay).hasSize(2)
                .contains(MapEntry.entry("count", 7))
                .contains(MapEntry.entry("timeUnit", "minutes"));
        System.out.println(response);
    }

    @Test
    public void testObjectMapper() throws Exception {
        OnlineComServerInfo onlineComServerInfo = new OnlineComServerInfo();
        onlineComServerInfo.name="new";
        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.registerSubtypes(OnlineComServerInfo.class);
//        objectMapper.registerSubtypes(ComServerInfo.class);
        objectMapper.getSubtypeResolver().registerSubtypes(OfflineComServerInfo.class, ComServerInfo.class);
        String response = objectMapper.writeValueAsString(onlineComServerInfo);
        System.out.println(response);
        assertThat(response).contains("comServerType");
    }

    @Test
    public void testPutComServer() throws Exception {
//        {comServers=[{id=1, name=Test, active=false, serverLogLevel=INFO, communicationLogLevel=ERROR, changesInterPollDelay={count=6, timeUnit=seconds}, schedulingInterPollDelay={count=7, timeUnit=minutes}, inboundComPorts=null, outboundComPorts=null, onlineComServerId=null, queryAPIUsername=null, queryAPIPassword=null, queryAPIPostUri=/test, usesDefaultQueryAPIPostUri=false, eventRegistrationUri=/event/registration/uri, usesDefaultEventRegistrationUri=false, storeTaskQueueSize=3, numberOfStoreTaskThreads=2, storeTaskThreadPriority=4}]}
        OnlineComServerInfo onlineComServerInfo = new OnlineComServerInfo();
        onlineComServerInfo.comServerType="Online";
        Entity<OnlineComServerInfo> objectEntity = Entity.json(onlineComServerInfo);

        final Response response = target("/comservers/3").request().put(objectEntity);
        System.out.println(response);

    }
}