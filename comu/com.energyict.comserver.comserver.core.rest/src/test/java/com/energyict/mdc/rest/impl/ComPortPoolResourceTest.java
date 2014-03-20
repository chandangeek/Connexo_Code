package com.energyict.mdc.rest.impl;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.rest.impl.comserver.ComPortPoolResource;
import com.energyict.mdc.rest.impl.comserver.InboundComPortPoolInfo;
import com.energyict.mdc.rest.impl.comserver.OutboundComPortInfo;
import com.energyict.mdc.rest.impl.comserver.OutboundComPortPoolInfo;
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
        assertThat(response).describedAs("Should contain field 'comPortPool'").containsKey("comPortPools").containsKey("total").hasSize(2);
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
        assertThat(response).describedAs("Should contain field 'comPortPool'").containsKey("comPortPools").containsKey("total").hasSize(2);
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
    public void testUpdateExistingComPortPoolAddOneDeleteOneAndKeepOne() throws Exception {
        long comPortPool_id = 16;
        long comPort1_id_to_be_kept = 166;
        long comPort2_id_to_be_added = 167;
        long comPort3_id_to_be_removed = 168;

        OutboundComPortPoolInfo outboundComPortPoolInfo = new OutboundComPortPoolInfo();
        outboundComPortPoolInfo.id=comPortPool_id;
        outboundComPortPoolInfo.active=true;
        outboundComPortPoolInfo.name="Updated";
        outboundComPortPoolInfo.description="description";
        outboundComPortPoolInfo.taskExecutionTimeout=new TimeDurationInfo(new TimeDuration(5, TimeDuration.MINUTES));
        OutboundComPortInfo tcpOutboundComPortInfo1 = new OutboundComPortInfo();
        tcpOutboundComPortInfo1.name="Port 1";
        tcpOutboundComPortInfo1.id=comPort1_id_to_be_kept;
        tcpOutboundComPortInfo1.comPortType=ComPortType.TCP;
        OutboundComPortInfo tcpOutboundComPortInfo2 = new OutboundComPortInfo();
        tcpOutboundComPortInfo2.name="Port 2";
        tcpOutboundComPortInfo2.id=comPort2_id_to_be_added;
        tcpOutboundComPortInfo2.comPortType=ComPortType.TCP;
        outboundComPortPoolInfo.outboundComPorts= new ArrayList<>(Arrays.asList(tcpOutboundComPortInfo1, tcpOutboundComPortInfo2));

        OutboundComPortPool mockOutboundComPortPool = mock(OutboundComPortPool.class);
        OutboundComPort mockTcpPort1 = mock(OutboundComPort.class);
        when(mockTcpPort1.getName()).thenReturn("Port 1");
        when(mockTcpPort1.getId()).thenReturn(comPort1_id_to_be_kept);
        OutboundComPort mockTcpPort2 = mock(OutboundComPort.class);
        when(mockTcpPort2.getName()).thenReturn("Port 2");
        when(mockTcpPort2.getId()).thenReturn(comPort2_id_to_be_added);
        OutboundComPort mockTcpPort3 = mock(OutboundComPort.class);
        when(mockTcpPort3.getName()).thenReturn("Port 3");
        when(mockTcpPort3.getId()).thenReturn(comPort3_id_to_be_removed);

        when(mockOutboundComPortPool.getComPorts()).thenReturn(Arrays.<OutboundComPort>asList(mockTcpPort1, mockTcpPort3));
        when(engineModelService.findComPortPool(comPortPool_id)).thenReturn(mockOutboundComPortPool);
        when(engineModelService.findComPort(comPort2_id_to_be_added)).thenReturn(mockTcpPort2);

        Entity<OutboundComPortPoolInfo> json = Entity.json(outboundComPortPoolInfo);

        final Response response = target("/comportpools/"+comPortPool_id).request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(mockOutboundComPortPool).save();
        verify(mockOutboundComPortPool).removeOutboundComPort(mockTcpPort3);
        ArgumentCaptor<OutboundComPort> comPortArgumentCaptor = ArgumentCaptor.forClass(OutboundComPort.class);
        verify(mockOutboundComPortPool).addOutboundComPort(comPortArgumentCaptor.capture());
        assertThat(comPortArgumentCaptor.getValue().getName()).isEqualTo("Port 2");
    }

    @Test
    public void testCreateComPortPoolWithoutComPorts() throws Exception {

        OutboundComPortPoolInfo outboundComPortPoolInfo = new OutboundComPortPoolInfo();
        outboundComPortPoolInfo.active=true;
        outboundComPortPoolInfo.name="Updated";
        outboundComPortPoolInfo.description="description";
        outboundComPortPoolInfo.taskExecutionTimeout=new TimeDurationInfo(new TimeDuration(5, TimeDuration.MINUTES));

        OutboundComPortPool outboundComPortPool = mock(OutboundComPortPool.class);
        when(engineModelService.newOutboundComPortPool()).thenReturn(outboundComPortPool);

        Entity<OutboundComPortPoolInfo> json = Entity.json(outboundComPortPoolInfo);

        final Response response = target("/comportpools/").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
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