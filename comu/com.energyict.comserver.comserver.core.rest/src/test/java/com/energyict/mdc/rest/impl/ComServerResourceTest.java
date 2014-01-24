package com.energyict.mdc.rest.impl;

import com.energyict.mdc.protocol.api.channels.serial.FlowControl;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.rest.impl.comserver.ComServerResource;
import com.energyict.mdc.rest.impl.comserver.InboundComPortInfo;
import com.energyict.mdc.rest.impl.comserver.ModemInboundComPortInfo;
import com.energyict.mdc.rest.impl.comserver.OnlineComServerInfo;
import org.assertj.core.data.MapEntry;
import org.codehaus.jackson.map.ObjectMapper;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * When accessing a resource, I choose not to use UriBuilder, as you should be aware that changing the URI means changing the API!
 * Hard coding URLS here will be a "gently" reminder
 * @author bvn
 */
public class ComServerResourceTest extends JerseyTest {
    private static EngineModelService engineModelService;

    @BeforeClass
    public static void setUpClass() throws Exception {
        engineModelService = mock(EngineModelService.class);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        reset(engineModelService);
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
                bind(engineModelService).to(EngineModelService.class);
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
        List<ComServer> comServers = new ArrayList<>();
        comServers.add(mock);
        when(engineModelService.findAllComServers()).thenReturn(comServers);
        when(mock.getName()).thenReturn("Test");
        when(mock.getQueryApiPostUri()).thenReturn("/test");
        when(mock.getId()).thenReturn(1L);
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
                .contains(MapEntry.entry("communicationLogLevel", "Error"))
                .contains(MapEntry.entry("serverLogLevel", "Information"));

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
        long comServer_id = 3;

        OnlineComServer serverSideComServer = mock(OnlineComServer.class);
        when(serverSideComServer.getId()).thenReturn(comServer_id);
        when(engineModelService.findComServer(comServer_id)).thenReturn(serverSideComServer);

        OnlineComServerInfo onlineComServerInfo = new OnlineComServerInfo();
        onlineComServerInfo.name="new name";
        onlineComServerInfo.eventRegistrationUri="/new/uri";
        onlineComServerInfo.schedulingInterPollDelay=new TimeDurationInfo();
        onlineComServerInfo.schedulingInterPollDelay.timeUnit="seconds";
        onlineComServerInfo.schedulingInterPollDelay.count=6;
        onlineComServerInfo.communicationLogLevel= ComServer.LogLevel.ERROR;
        onlineComServerInfo.serverLogLevel= ComServer.LogLevel.DEBUG;
        onlineComServerInfo.inboundComPorts = new ArrayList<>();
        onlineComServerInfo.outboundComPorts = new ArrayList<>();
        onlineComServerInfo.storeTaskQueueSize= 7;
        onlineComServerInfo.storeTaskThreadPriority= 8;
        onlineComServerInfo.numberOfStoreTaskThreads= 9;
        onlineComServerInfo.queryAPIUsername= "user name"; // UNUSED
        Entity<OnlineComServerInfo> json = Entity.json(onlineComServerInfo);

        final Response response = target("/comservers/3").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(serverSideComServer).save();
        ArgumentCaptor<String> onlineComServerArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(serverSideComServer).setName(onlineComServerArgumentCaptor.capture());
        assertThat(onlineComServerArgumentCaptor.getValue()).isEqualTo("new name");

        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        verify(serverSideComServer).setEventRegistrationUri(uriCaptor.capture());
        assertThat(uriCaptor.getValue()).isEqualTo("/new/uri");

        assertThat(serverSideComServer.usesDefaultEventRegistrationUri()).isEqualTo(false);

        ArgumentCaptor<TimeDuration> timeDurationCaptor = ArgumentCaptor.forClass(TimeDuration.class);
        verify(serverSideComServer).setSchedulingInterPollDelay(timeDurationCaptor.capture());
        assertThat(timeDurationCaptor.getValue()).isEqualTo(new TimeDuration(6));

        ArgumentCaptor<ComServer.LogLevel> logLevelCaptor = ArgumentCaptor.forClass(ComServer.LogLevel.class);
        verify(serverSideComServer).setCommunicationLogLevel(logLevelCaptor.capture());
        assertThat(logLevelCaptor.getValue()).isEqualTo(ComServer.LogLevel.ERROR);

        ArgumentCaptor<ComServer.LogLevel> serverLogLevelCaptor = ArgumentCaptor.forClass(ComServer.LogLevel.class);
        verify(serverSideComServer).setServerLogLevel(serverLogLevelCaptor.capture());
        assertThat(serverLogLevelCaptor.getValue()).isEqualTo(ComServer.LogLevel.DEBUG);

        ArgumentCaptor<Integer> storeTaskQueueCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(serverSideComServer).setStoreTaskQueueSize(storeTaskQueueCaptor.capture());
        assertThat(storeTaskQueueCaptor.getValue()).isEqualTo(7);

        ArgumentCaptor<Integer> storeTaskThreadCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(serverSideComServer).setStoreTaskThreadPriority(storeTaskThreadCaptor.capture());
        assertThat(storeTaskThreadCaptor.getValue()).isEqualTo(8);

        ArgumentCaptor<Integer> numberOfStoreTaskThreadCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(serverSideComServer).setNumberOfStoreTaskThreads(numberOfStoreTaskThreadCaptor.capture());
        assertThat(numberOfStoreTaskThreadCaptor.getValue()).isEqualTo(9);
    }

    @Test
    public void testUpdateExistingComServerAddSerialInboundComPort() throws Exception {
        long comServer_id = 3;
        long comPortPool_id = 16;

        ComServer serverSideComServer = mock(OnlineComServer.class);
        when(serverSideComServer.getId()).thenReturn(comServer_id);
        when(engineModelService.findComServer(comServer_id)).thenReturn(serverSideComServer);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(comPortPool.getId()).thenReturn(comPortPool_id);
        when(engineModelService.findInboundComPortPool(comPortPool_id)).thenReturn(comPortPool);


        OnlineComServerInfo onlineComServerInfo = new OnlineComServerInfo();
        onlineComServerInfo.name="new name";
        ModemInboundComPortInfo modemInboundComPortInfo = new ModemInboundComPortInfo();
        modemInboundComPortInfo.ringCount = 100;
        modemInboundComPortInfo.maximumNumberOfDialErrors = 101;
        TimeDurationInfo timeDurationInfo = new TimeDurationInfo();
        timeDurationInfo.count=2;
        timeDurationInfo.timeUnit="seconds";
        modemInboundComPortInfo.atCommandTimeout = timeDurationInfo;
        modemInboundComPortInfo.comServer_id = comServer_id;
        modemInboundComPortInfo.comPortPool_id = comPortPool_id;
        modemInboundComPortInfo.flowControl = FlowControl.XONXOFF;
        List<InboundComPortInfo<? extends InboundComPort>> inboundPorts = new ArrayList<>();
        inboundPorts.add(modemInboundComPortInfo);
        onlineComServerInfo.inboundComPorts =inboundPorts;
        onlineComServerInfo.outboundComPorts =new ArrayList<>();

        MockModemBasedInboundComPort mockModemBasedInboundComPort = new MockModemBasedInboundComPort();
        when(engineModelService.newModemBasedInbound(serverSideComServer)).thenReturn(mockModemBasedInboundComPort);

        Entity<OnlineComServerInfo> json = Entity.json(onlineComServerInfo);

        final Response response = target("/comservers/3").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(serverSideComServer).save();

        assertThat(mockModemBasedInboundComPort.getComPortPool().getId()).isEqualTo(comPortPool_id);
        assertThat(mockModemBasedInboundComPort.getRingCount()).isEqualTo(100);
        assertThat(mockModemBasedInboundComPort.getMaximumDialErrors()).isEqualTo(101);
        assertThat(mockModemBasedInboundComPort.getAtCommandTimeout().getSeconds()).isEqualTo(2);
    }

    @Test
    public void testCanNotUpdateComServerWithoutInboundComPorts() throws Exception {
        OnlineComServerInfo onlineComServerInfo = new OnlineComServerInfo();
        onlineComServerInfo.name="new name";
        onlineComServerInfo.inboundComPorts = null;
        onlineComServerInfo.outboundComPorts = new ArrayList<>();
        Entity<OnlineComServerInfo> json = Entity.json(onlineComServerInfo);
        final Response response = target("/comservers/4").request().put(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testCanNotUpdateComServerWithoutOutboundComPorts() throws Exception {
        OnlineComServerInfo onlineComServerInfo = new OnlineComServerInfo();
        onlineComServerInfo.name="new name";
        onlineComServerInfo.inboundComPorts = new ArrayList<>();
        onlineComServerInfo.outboundComPorts = null;
        Entity<OnlineComServerInfo> json = Entity.json(onlineComServerInfo);
        final Response response = target("/comservers/5").request().put(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testCanNotUpdateNonExistingComServer() throws Exception {
        int comServer_id = 5;

        ComServer serverSideComServer = mock(OnlineComServer.class);
        when(engineModelService.findComServer(comServer_id)).thenReturn(serverSideComServer);

        OnlineComServerInfo onlineComServerInfo = new OnlineComServerInfo();
        onlineComServerInfo.name="new name";
        onlineComServerInfo.inboundComPorts = new ArrayList<>();
        onlineComServerInfo.outboundComPorts = new ArrayList<>();
        Entity<OnlineComServerInfo> json = Entity.json(onlineComServerInfo);
        final Response response = target("/comservers/3").request().put(json); //5 was mocked, there is no ComServer 3

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testDeleteComServer() throws Exception {
        int comServer_id = 5;

        ComServer serverSideComServer = mock(OnlineComServer.class);
        when(engineModelService.findComServer(comServer_id)).thenReturn(serverSideComServer);

        final Response response = target("/comservers/5").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(serverSideComServer).delete();
    }

    @Test
    public void testDeleteNonExistingComServerThrows404() throws Exception {
        final Response response = target("/comservers/5").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }
}