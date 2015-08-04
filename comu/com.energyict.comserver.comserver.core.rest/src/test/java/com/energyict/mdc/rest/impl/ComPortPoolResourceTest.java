package com.energyict.mdc.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.rest.impl.comserver.ComPortPoolInfo;
import com.energyict.mdc.rest.impl.comserver.InboundComPortPoolInfo;
import com.energyict.mdc.rest.impl.comserver.OutboundComPortInfo;
import com.energyict.mdc.rest.impl.comserver.OutboundComPortPoolInfo;
import com.energyict.mdc.rest.impl.comserver.TcpOutboundComPortInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.assertj.core.data.MapEntry;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * When accessing a resource, I choose not to use UriBuilder, as you should be aware that changing the URI means changing the API!
 * Hard coding URLS here will be a "gently" reminder
 * @author bvn
 */
public class ComPortPoolResourceTest extends ComserverCoreApplicationJerseyTest {
    @Test
    public void testGetNonExistingComPortPool() throws Exception {
        when(this.engineConfigurationService.findComPortPool(anyLong())).thenReturn(Optional.empty());
        final Response response = target("/comportpools/8").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetComPortsForNonExistingComServer() throws Exception {
        when(this.engineConfigurationService.findComPortPool(anyLong())).thenReturn(Optional.empty());
        final Response response = target("/comportpools/8/comports").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetExistingInboundComServerJSStyle() {
        InboundComPortPool mock = mock(InboundComPortPool.class);
        InboundDeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(InboundDeviceProtocolPluggableClass.class);
        when(deviceProtocolPluggableClass.getId()).thenReturn(6L);
        List<ComPortPool> comPortPools = new ArrayList<>();
        comPortPools.add(mock);
        when(engineConfigurationService.findAllComPortPools()).thenReturn(comPortPools);
        when(mock.getName()).thenReturn("Test");
        when(mock.isActive()).thenReturn(false);
        when(mock.getDiscoveryProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        when(mock.getId()).thenReturn(1L);
        when(mock.getComPortType()).thenReturn(ComPortType.TCP);

        final Map<String, Object> response = target("/comportpools").request().get(Map.class); // Using MAP instead of *Info to resemble JS
        assertThat(response).describedAs("Should contain field 'data'").containsKey("data").containsKey("total").hasSize(2);
        List<Map<String, Object>> comPortPools1 = (List<Map<String, Object>>) response.get("data");
        assertThat(comPortPools1).describedAs("Expected only 1 comPortPool").hasSize(1);
        Map<String, Object> comPortPool1 = comPortPools1.get(0);
        assertThat(comPortPool1)
                .contains(MapEntry.entry("id", 1))
                .contains(MapEntry.entry("name", "Test"))
                .contains(MapEntry.entry("direction", "Inbound"))
                .contains(MapEntry.entry("active", false))
                .contains(MapEntry.entry("obsoleteFlag", false))
                .contains(MapEntry.entry("comPortType", "TCP"))
                .contains(MapEntry.entry("discoveryProtocolPluggableClassId", 6));
    }

    @Test
    public void testGetExistingOutboundComServerJSStyle() {
        OutboundComPortPool mock = mock(OutboundComPortPool.class);
        List<ComPortPool> comPortPools = new ArrayList<>();
        comPortPools.add(mock);
        when(engineConfigurationService.findAllComPortPools()).thenReturn(comPortPools);
        when(mock.getName()).thenReturn("Test");
        when(mock.isActive()).thenReturn(false);
        when(mock.getId()).thenReturn(1L);
        when(mock.getComPortType()).thenReturn(ComPortType.TCP);
        when(mock.getTaskExecutionTimeout()).thenReturn(new TimeDuration(5, TimeDuration.TimeUnit.MINUTES));

        final Map<String, Object> response = target("/comportpools").request().get(Map.class); // Using MAP instead of *Info to resemble JS
        assertThat(response).describedAs("Should contain field 'data'").containsKey("data").containsKey("total").hasSize(2);
        List<Map<String, Object>> comPortPools1 = (List<Map<String, Object>>) response.get("data");
        assertThat(comPortPools1).describedAs("Expected only 1 comPortPool").hasSize(1);
        Map<String, Object> comPortPool1 = comPortPools1.get(0);
        assertThat(comPortPool1)
                .contains(MapEntry.entry("id", 1))
                .contains(MapEntry.entry("name", "Test"))
                .contains(MapEntry.entry("direction", "Outbound"))
                .contains(MapEntry.entry("active", false))
                .contains(MapEntry.entry("obsoleteFlag", false))
                .contains(MapEntry.entry("comPortType", "TCP"));

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
        assertThat(response).contains("\"direction\"", "\"Inbound\"");
    }

    @Test
    public void testObjectMapperSerializesOutboundTypeInformation() throws Exception {
        OutboundComPortPoolInfo outboundComPortPoolInfo = new OutboundComPortPoolInfo();
        outboundComPortPoolInfo.name="new";
        ObjectMapper objectMapper = new ObjectMapper();
        String response = objectMapper.writeValueAsString(outboundComPortPoolInfo);
        assertThat(response).contains("\"direction\"", "\"Outbound\"");
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
        outboundComPortPoolInfo.taskExecutionTimeout=new TimeDurationInfo(new TimeDuration(5, TimeDuration.TimeUnit.MINUTES));
        OutboundComPortInfo tcpOutboundComPortInfo1 = new TcpOutboundComPortInfo();
        tcpOutboundComPortInfo1.name="Port 1";
        tcpOutboundComPortInfo1.id=comPort1_id_to_be_kept;
        tcpOutboundComPortInfo1.comPortType=ComPortType.TCP;
        OutboundComPortInfo tcpOutboundComPortInfo2 = new TcpOutboundComPortInfo();
        tcpOutboundComPortInfo2.name="Port 2";
        tcpOutboundComPortInfo2.id=comPort2_id_to_be_added;
        tcpOutboundComPortInfo2.comPortType=ComPortType.TCP;
        outboundComPortPoolInfo.outboundComPorts= new ArrayList<>(Arrays.asList(tcpOutboundComPortInfo1, tcpOutboundComPortInfo2));

        OutboundComPortPool mockOutboundComPortPool = mock(OutboundComPortPool.class);
        OutboundComPort mockTcpPort1 = mock(OutboundComPort.class);
        when(mockTcpPort1.getName()).thenReturn("Port 1");
        when(mockTcpPort1.getId()).thenReturn(comPort1_id_to_be_kept);
        when(mockTcpPort1.getComPortType()).thenReturn(ComPortType.TCP);
        OutboundComPort mockTcpPort2 = mock(OutboundComPort.class);
        when(mockTcpPort2.getName()).thenReturn("Port 2");
        when(mockTcpPort2.getId()).thenReturn(comPort2_id_to_be_added);
        when(mockTcpPort2.getComPortType()).thenReturn(ComPortType.TCP);
        OutboundComPort mockTcpPort3 = mock(OutboundComPort.class);
        when(mockTcpPort3.getName()).thenReturn("Port 3");
        when(mockTcpPort3.getId()).thenReturn(comPort3_id_to_be_removed);
        when(mockTcpPort3.getComPortType()).thenReturn(ComPortType.TCP);

        when(mockOutboundComPortPool.getComPorts()).thenReturn(Arrays.<OutboundComPort>asList(mockTcpPort1, mockTcpPort3));
        doReturn(Optional.of(mockOutboundComPortPool)).when(engineConfigurationService).findComPortPool(comPortPool_id);
        doReturn(Optional.of(mockTcpPort2)).when(engineConfigurationService).findComPort(comPort2_id_to_be_added);

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
        outboundComPortPoolInfo.taskExecutionTimeout=new TimeDurationInfo(new TimeDuration(5, TimeDuration.TimeUnit.MINUTES));

        OutboundComPortPool outboundComPortPool = mock(OutboundComPortPool.class);
        when(engineConfigurationService.newOutboundComPortPool(anyString(), any(ComPortType.class), any(TimeDuration.class))).thenReturn(outboundComPortPool);

        Entity<OutboundComPortPoolInfo> json = Entity.json(outboundComPortPoolInfo);

        final Response response = target("/comportpools/").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testCreateOutboundComPortPoolWithoutNextExecutionSpecApliesDefaultOf6Hours() throws Exception {

        OutboundComPortPoolInfo outboundComPortPoolInfo = new OutboundComPortPoolInfo();
        outboundComPortPoolInfo.active=true;
        outboundComPortPoolInfo.name="Updated";
        outboundComPortPoolInfo.description="description";
        outboundComPortPoolInfo.taskExecutionTimeout=new TimeDurationInfo();

        OutboundComPortPool outboundComPortPool = mock(OutboundComPortPool.class);
        when(engineConfigurationService.newOutboundComPortPool(anyString(), any(ComPortType.class), any(TimeDuration.class))).thenReturn(outboundComPortPool);

        Entity<OutboundComPortPoolInfo> json = Entity.json(outboundComPortPoolInfo);

        final Response response = target("/comportpools/").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        ArgumentCaptor<TimeDuration> timeDurationCaptor = ArgumentCaptor.forClass(TimeDuration.class);
        verify(engineConfigurationService).newOutboundComPortPool(anyString(), any(ComPortType.class), timeDurationCaptor.capture());
        assertThat(timeDurationCaptor.getValue().getSeconds()).isEqualTo(3600*6);
    }

    @Test
    public void testCreateInboundComPortPoolWithoutProtocol() throws Exception {

        InboundComPortPoolInfo inboundComPortPoolInfo = new InboundComPortPoolInfo();
        inboundComPortPoolInfo.active=true;
        inboundComPortPoolInfo.name="Updated";
        inboundComPortPoolInfo.description="description";
        inboundComPortPoolInfo.taskExecutionTimeout=new TimeDurationInfo(new TimeDuration(5, TimeDuration.TimeUnit.MINUTES));
        MessageSeed messageSeed = mock(MessageSeed.class);
        when(messageSeed.getKey()).thenReturn("someKey");
        when(messageSeed.getDefaultFormat()).thenReturn("required value");
        when(protocolPluggableService.findInboundDeviceProtocolPluggableClass(0L)).thenReturn(Optional.empty());
        when(engineConfigurationService.newInboundComPortPool(anyString(), any(ComPortType.class), any(InboundDeviceProtocolPluggableClass.class))).thenThrow(new LocalizedFieldValidationException(messageSeed, "discoveryProtocolPluggableClassId"));

        final Response response = target("/comportpools/").request().post(Entity.json(inboundComPortPoolInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testCreateComPortPoolWithoutType() throws Exception {
        ComPortPoolInfo outboundComPortPoolInfo = new ComPortPoolInfo() { // Unknown type
            @Override
            protected ComPortPool createNew(EngineConfigurationService engineConfigurationService, ProtocolPluggableService protocolPluggableService) {
                return null;
            }
        };
        outboundComPortPoolInfo.active=true;
        outboundComPortPoolInfo.name="Created";
        outboundComPortPoolInfo.description="description";
        outboundComPortPoolInfo.taskExecutionTimeout=new TimeDurationInfo(new TimeDuration(5, TimeDuration.TimeUnit.MINUTES));

        Entity<ComPortPoolInfo> json = Entity.json(outboundComPortPoolInfo);

        final Response response = target("/comportpools/").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testDeleteComPortPool() throws Exception {
        int comPortPool_id = 5;

        InboundComPortPool mock = mock(InboundComPortPool.class);
        doReturn(Optional.of(mock)).when(engineConfigurationService).findComPortPool(comPortPool_id);

        final Response response = target("/comportpools/5").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());

        verify(mock).makeObsolete();
    }

    @Test
    public void testDeleteNonExistingComPortPoolThrows404() throws Exception {
        when(this.engineConfigurationService.findComPortPool(anyLong())).thenReturn(Optional.empty());
        final Response response = target("/comportpools/5").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

}