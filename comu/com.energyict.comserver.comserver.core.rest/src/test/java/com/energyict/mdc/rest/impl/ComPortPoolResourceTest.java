package com.energyict.mdc.rest.impl;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.engine.model.TCPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.channels.serial.FlowControl;
import com.energyict.mdc.rest.impl.comserver.ComPortPoolResource;
import com.energyict.mdc.rest.impl.comserver.InboundComPortInfo;
import com.energyict.mdc.rest.impl.comserver.InboundComPortPoolInfo;
import com.energyict.mdc.rest.impl.comserver.ModemInboundComPortInfo;
import com.energyict.mdc.rest.impl.comserver.OnlineComServerInfo;
import com.energyict.mdc.rest.impl.comserver.OutboundComPortPoolInfo;
import com.energyict.mdc.rest.impl.comserver.TcpInboundComPortInfo;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

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
public class ComPortPoolResourceTest extends JerseyTest {
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
        ResourceConfig resourceConfig = new ResourceConfig(ComPortPoolResource.class);
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
    public void testGetNonExistingComPortPool() throws Exception {
        final Response response = target("/comportpools/8").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetComPortsForNonExistingComServer() throws Exception {
        final Response response = target("/comportpools/8/comports").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetExistingInboundComServerJSStyle() {
        InboundComPortPool mock = mock(InboundComPortPool.class);
        List<ComPortPool> comPortPools = new ArrayList<>();
        comPortPools.add(mock);
        when(engineModelService.findAllComPortPools()).thenReturn(comPortPools);
        when(mock.getName()).thenReturn("Test");
        when(mock.isActive()).thenReturn(false);
        when(mock.getDiscoveryProtocolPluggableClassId()).thenReturn(6L);
        when(mock.getId()).thenReturn(1L);
        when(mock.getComPortType()).thenReturn(ComPortType.TCP);

        final Map<String, Object> response = target("/comportpools").request().get(Map.class); // Using MAP instead of *Info to resemble JS
        assertThat(response).describedAs("Should contain field 'comPortPool'").containsKey("comPortPools").hasSize(1);
        List<Map<String, Object>> comPortPools1 = (List<Map<String, Object>>) response.get("comPortPools");
        assertThat(comPortPools1).describedAs("Expected only 1 comPortPool").hasSize(1);
        Map<String, Object> comPortPool1 = comPortPools1.get(0);
        assertThat(comPortPool1)
                .contains(MapEntry.entry("id", 1))
                .contains(MapEntry.entry("name", "Test"))
                .contains(MapEntry.entry("direction", "inbound"))
                .contains(MapEntry.entry("active", false))
                .contains(MapEntry.entry("obsoleteFlag", false))
                .contains(MapEntry.entry("type", "TCP"))
                .contains(MapEntry.entry("discoveryProtocolPluggableClassId", 6));
    }

    @Test
    public void testGetExistingOutboundComServerJSStyle() {
        OutboundComPortPool mock = mock(OutboundComPortPool.class);
        List<ComPortPool> comPortPools = new ArrayList<>();
        comPortPools.add(mock);
        when(engineModelService.findAllComPortPools()).thenReturn(comPortPools);
        when(mock.getName()).thenReturn("Test");
        when(mock.isActive()).thenReturn(false);
        when(mock.getId()).thenReturn(1L);
        when(mock.getComPortType()).thenReturn(ComPortType.TCP);
        when(mock.getTaskExecutionTimeout()).thenReturn(new TimeDuration(5, TimeDuration.MINUTES));

        final Map<String, Object> response = target("/comportpools").request().get(Map.class); // Using MAP instead of *Info to resemble JS
        assertThat(response).describedAs("Should contain field 'comPortPool'").containsKey("comPortPools").hasSize(1);
        List<Map<String, Object>> comPortPools1 = (List<Map<String, Object>>) response.get("comPortPools");
        assertThat(comPortPools1).describedAs("Expected only 1 comPortPool").hasSize(1);
        Map<String, Object> comPortPool1 = comPortPools1.get(0);
        assertThat(comPortPool1)
                .contains(MapEntry.entry("id", 1))
                .contains(MapEntry.entry("name", "Test"))
                .contains(MapEntry.entry("direction", "outbound"))
                .contains(MapEntry.entry("active", false))
                .contains(MapEntry.entry("obsoleteFlag", false))
                .contains(MapEntry.entry("type", "TCP"));

        Map<String, Object> taskExecutionTimeout = (Map<String, Object>) comPortPool1.get("taskExecutionTimeout");
        assertThat(taskExecutionTimeout).hasSize(2)
                .contains(MapEntry.entry("count", 5))
                .contains(MapEntry.entry("timeUnit", "minutes"));
    }

    @Test
    public void testObjectMapperSerializesTypeInformation() throws Exception {
        InboundComPortPoolInfo inboundComPortPoolInfo = new InboundComPortPoolInfo();
        inboundComPortPoolInfo.name="new";
        ObjectMapper objectMapper = new ObjectMapper();
        String response = objectMapper.writeValueAsString(inboundComPortPoolInfo);
        assertThat(response).contains("\"direction\"", "\"inbound\"");
    }

    @Test
    public void testObjectMapperSerializesOutboundTypeInformation() throws Exception {
        OutboundComPortPoolInfo outboundComPortPoolInfo = new OutboundComPortPoolInfo();
        outboundComPortPoolInfo.name="new";
        ObjectMapper objectMapper = new ObjectMapper();
        String response = objectMapper.writeValueAsString(outboundComPortPoolInfo);
        assertThat(response).contains("\"direction\"", "\"outbound\"");
    }

    @Test
    public void testUpdateExistingComPortPoolAddInboundComPort() throws Exception {
        long comPortPool_id = 16;
        long comPort1_id = 166;
        long comPort2_id = 167;
        long comPort3_id = 168;

        InboundComPortPoolInfo inboundComPortPoolInfo = new InboundComPortPoolInfo();
        inboundComPortPoolInfo.id=comPortPool_id;
        inboundComPortPoolInfo.active=true;
        inboundComPortPoolInfo.name="Updated";
        inboundComPortPoolInfo.description="description";
        inboundComPortPoolInfo.discoveryProtocolPluggableClassId=13;
        TcpInboundComPortInfo tcpInboundComPortInfo1 = new TcpInboundComPortInfo();
        tcpInboundComPortInfo1.name="Port 1";
        tcpInboundComPortInfo1.id=comPort1_id;
        TcpInboundComPortInfo tcpInboundComPortInfo2 = new TcpInboundComPortInfo();
        tcpInboundComPortInfo2.name="Port 2";
        tcpInboundComPortInfo2.id=comPort2_id;
        inboundComPortPoolInfo.inboundComPorts= new ArrayList<InboundComPortInfo>(Arrays.asList(tcpInboundComPortInfo1, tcpInboundComPortInfo2));

        InboundComPortPool mockInboundComPortPool = mock(InboundComPortPool.class);
        TCPBasedInboundComPort mockTcpPort1 = mock(TCPBasedInboundComPort.class);
        when(mockTcpPort1.getName()).thenReturn("Port 1");
        when(mockTcpPort1.getId()).thenReturn(comPort1_id);
        TCPBasedInboundComPort mockTcpPort2 = mock(TCPBasedInboundComPort.class);
        when(mockTcpPort2.getName()).thenReturn("Port 3");
        when(mockTcpPort2.getId()).thenReturn(comPort3_id);

        when(mockInboundComPortPool.getComPorts()).thenReturn(Arrays.<InboundComPort>asList(mockTcpPort1, mockTcpPort2));
        when(engineModelService.findComPortPool(comPortPool_id)).thenReturn(mockInboundComPortPool);

        Entity<InboundComPortPoolInfo> json = Entity.json(inboundComPortPoolInfo);

        final Response response = target("/comportpools/"+comPortPool_id).request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(mockInboundComPortPool).save();
        ArgumentCaptor<String> nameArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockInboundComPortPool).setName(nameArgumentCaptor.capture());
        assertThat(nameArgumentCaptor.getValue()).isEqualTo("Updated");

        ArgumentCaptor<String> descriptionArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockInboundComPortPool).setDescription(descriptionArgumentCaptor.capture());
        assertThat(descriptionArgumentCaptor.getValue()).isEqualTo("description");

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(mockInboundComPortPool).setDiscoveryProtocolPluggableClassId(longArgumentCaptor.capture());
        assertThat(longArgumentCaptor.getValue()).isEqualTo(13L);
    }

    @Test
    public void testDeleteComPortPool() throws Exception {
        int comPortPool_id = 5;

        InboundComPortPool mock = mock(InboundComPortPool.class);
        when(engineModelService.findComPortPool(comPortPool_id)).thenReturn(mock);

        final Response response = target("/comportpools/5").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(mock).delete();
    }

    @Test
    public void testDeleteNonExistingComPortPoolThrows404() throws Exception {
        final Response response = target("/comportpools/5").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }


}