package com.energyict.mdc.rest.impl;

import com.energyict.cbo.TimeDuration;
import com.energyict.mdc.servers.ComServer;
import com.energyict.mdc.servers.OnlineComServer;
import com.energyict.mdc.services.ComServerService;
import com.energyict.mdc.shadow.servers.ComServerShadow;
import com.energyict.mdc.shadow.servers.OnlineComServerShadow;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import org.assertj.core.data.MapEntry;
import org.codehaus.jackson.map.ObjectMapper;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * When accessing a resource, I choose not to use UriBuilder, as you should be aware that changing the URI means changing the API!
 * Hardcoding URLS here will be a "gently" reminder
 * @author bvn
 */
public class ComServerResourceTest extends JerseyTest {

    private static ComServerService comServerService;

    @BeforeClass
    static public void setUpClass() throws Exception {
        comServerService = mock(ComServerService.class);
    }

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resourceConfig = new ResourceConfig(ComServerResource.class);
        resourceConfig.register(JacksonFeature.class); // Server side JSON processing
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(comServerService).to(ComServerService.class);
            }
        });
        return resourceConfig;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(JacksonFeature.class); // client side JSON processing

        super.configureClient(config);
    }

    @Test
    public void testGetNonExistingComServer() throws Exception {
        final Response response = target("/comservers/8").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetComPortsForNonExistingComServer() throws Exception {
        final Response response = target("/comservers/8/comports").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetExistingComServerJSStyle() {
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

        final Map<String, Object> response = target("/comservers").request().get(Map.class); // Using MAP instead of *Info to resemble JS
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
                .contains(MapEntry.entry("serverLogLevel", "INFO"));

        Map<String, Object> changesInterPollDelay = (Map<String, Object>) comServer1.get("changesInterPollDelay");
        assertThat(changesInterPollDelay).hasSize(2)
                .contains(MapEntry.entry("count", 6))
                .contains(MapEntry.entry("timeUnit", "seconds"));
        Map<String, Object> schedulingInterPollDelay = (Map<String, Object>) comServer1.get("schedulingInterPollDelay");
        assertThat(schedulingInterPollDelay).hasSize(2)
                .contains(MapEntry.entry("count", 7))
                .contains(MapEntry.entry("timeUnit", "minutes"));
    }

    @Test
    public void testObjectMapperSerializesTypeInformation() throws Exception {
        OnlineComServerInfo onlineComServerInfo = new OnlineComServerInfo();
        onlineComServerInfo.name="new";
        ObjectMapper objectMapper = new ObjectMapper();
        String response = objectMapper.writeValueAsString(onlineComServerInfo);
        assertThat(response).contains("\"comServerType\":\"Online\"");
    }

    @Test
    public void testUpdateExistingComServer() throws Exception {
        int comServer_id = 3;

        ComServer serverSideComServer = mock(OnlineComServer.class);
        OnlineComServerShadow onlineComServerShadow = new OnlineComServerShadow();
        onlineComServerShadow.setId(comServer_id);
        when(serverSideComServer.getShadow()).thenReturn(onlineComServerShadow);
        when(comServerService.find(comServer_id)).thenReturn(serverSideComServer);

        OnlineComServerInfo onlineComServerInfo = new OnlineComServerInfo();
        onlineComServerInfo.name="new name";
        onlineComServerInfo.eventRegistrationUri="/new/uri";
        onlineComServerInfo.schedulingInterPollDelay=new TimeDurationInfo();
        onlineComServerInfo.schedulingInterPollDelay.timeUnit="seconds";
        onlineComServerInfo.schedulingInterPollDelay.count=6;
        onlineComServerInfo.communicationLogLevel= ComServer.LogLevel.ERROR;
        onlineComServerInfo.serverLogLevel= ComServer.LogLevel.DEBUG;
        onlineComServerInfo.inboundComPorts= new ArrayList<>();
        onlineComServerInfo.outboundComPorts= new ArrayList<>();
        onlineComServerInfo.storeTaskQueueSize= 7;
        onlineComServerInfo.storeTaskThreadPriority= 8;
        onlineComServerInfo.numberOfStoreTaskThreads= 9;
        onlineComServerInfo.queryAPIUsername= "user name"; // UNUSED
        Entity<OnlineComServerInfo> json = Entity.json(onlineComServerInfo);

        final Response response = target("/comservers/3").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        ArgumentCaptor<OnlineComServerShadow> onlineComServerShadowArgumentCaptor = ArgumentCaptor.forClass(OnlineComServerShadow.class);
        verify(serverSideComServer).update(onlineComServerShadowArgumentCaptor.capture());
        OnlineComServerShadow updatingValue = onlineComServerShadowArgumentCaptor.getValue();
        assertThat(updatingValue.getName()).isEqualTo("new name");
        assertThat(updatingValue.getEventRegistrationUri()).isEqualTo("/new/uri");
        assertThat(updatingValue.isUsesDefaultEventRegistrationUri()).isEqualTo(false);
        assertThat(updatingValue.getSchedulingInterPollDelay()).isEqualTo(new TimeDuration(6));
        assertThat(updatingValue.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.ERROR);
        assertThat(updatingValue.getServerLogLevel()).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(updatingValue.getStoreTaskQueueSize()).isEqualTo(7);
        assertThat(updatingValue.getStoreTaskThreadPriority()).isEqualTo(8);
        assertThat(updatingValue.getNumberOfStoreTaskThreads()).isEqualTo(9);
    }

    @Test
    public void testCanNotUpdateComServerWithoutInboundComPorts() throws Exception {
        OnlineComServerInfo onlineComServerInfo = new OnlineComServerInfo();
        onlineComServerInfo.name="new name";
        onlineComServerInfo.inboundComPorts= null;
        onlineComServerInfo.outboundComPorts= new ArrayList<>();
        Entity<OnlineComServerInfo> json = Entity.json(onlineComServerInfo);
        final Response response = target("/comservers/4").request().put(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testCanNotUpdateComServerWithoutOutboundComPorts() throws Exception {
        OnlineComServerInfo onlineComServerInfo = new OnlineComServerInfo();
        onlineComServerInfo.name="new name";
        onlineComServerInfo.inboundComPorts= new ArrayList<>();
        onlineComServerInfo.outboundComPorts= null;
        Entity<OnlineComServerInfo> json = Entity.json(onlineComServerInfo);
        final Response response = target("/comservers/5").request().put(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testCanNotUpdateNonExistingComServer() throws Exception {
        int comServer_id = 5;

        ComServer serverSideComServer = mock(OnlineComServer.class);
        OnlineComServerShadow onlineComServerShadow = new OnlineComServerShadow();
        onlineComServerShadow.setId(comServer_id);
        when(serverSideComServer.getShadow()).thenReturn(onlineComServerShadow);
        when(comServerService.find(comServer_id)).thenReturn(serverSideComServer);

        OnlineComServerInfo onlineComServerInfo = new OnlineComServerInfo();
        onlineComServerInfo.name="new name";
        onlineComServerInfo.inboundComPorts= new ArrayList<>();
        onlineComServerInfo.outboundComPorts= new ArrayList<>();
        Entity<OnlineComServerInfo> json = Entity.json(onlineComServerInfo);
        final Response response = target("/comservers/3").request().put(json); //5 was mocked, there is no ComServer 3

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }
}