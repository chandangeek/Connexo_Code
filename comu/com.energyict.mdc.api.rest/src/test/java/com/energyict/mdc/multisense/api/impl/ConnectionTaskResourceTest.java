package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.jayway.jsonpath.JsonModel;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import net.minidev.json.JSONArray;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 7/13/15.
 */
public class ConnectionTaskResourceTest extends MultisensePublicApiJerseyTest {

    @Test
    public void testGetAllConnectionTasksOfDevice() throws Exception {
        DeviceType elec1 = mockDeviceType(101, "Electricity 1");
        DeviceConfiguration deviceConfig = mockDeviceConfiguration(34L, "default configuration", elec1);
        Device deviceXas = mockDevice("XAS", "5544657642", deviceConfig);

        Response response = target("devices/XAS/connectionmethods").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testConnectionTaskInfoFields() throws Exception {
        Response response = target("devices/XAS/connectionmethods").request().accept(MediaType.APPLICATION_JSON).method("PROPFIND", Response.class);
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<JSONArray>get("$")).containsOnly("allowSimultaneousConnections", "comPortPool", "comWindow",
                "connectionStrategy", "connectionType", "id", "direction", "isDefault", "link", "connectionMethod", "nextExecutionSpecs", "properties",
                "rescheduleRetryDelay", "status");
    }

    @Test
    public void testGetSingleConnectionTask() throws Exception {
        DeviceType elec1 = mockDeviceType(101, "Electricity 1");
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1101L, "Default configuration", elec1);
        Device deviceXas = mockDevice("XAS", "5544657642", deviceConfiguration);
        OutboundComPortPool comPortPool = mock(OutboundComPortPool.class);
        when(comPortPool.getId()).thenReturn(65L);

        PartialScheduledConnectionTask partial = mockPartialScheduledConnectionTask(1681, "partial connection task");
        ScheduledConnectionTask connectionTask = mockScheduledConnectionTask(41L, "connTask", deviceXas, comPortPool, partial);
        when(connectionTaskService.findConnectionTask(41L)).thenReturn(Optional.of(connectionTask));

        Response response = target("devices/XAS/connectionmethods/41").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(41);
        assertThat(jsonModel.<Integer>get("$.connectionMethod.id")).isEqualTo(1681);
        assertThat(jsonModel.<String>get("$.connectionMethod.link.href")).isEqualTo("http://localhost:9998/devicetypes/101/deviceconfigurations/1101/connectionmethods/1681");
        assertThat(jsonModel.<String>get("$.connectionMethod.link.params.rel")).isEqualTo("up");
        assertThat(jsonModel.<String>get("$.direction")).isEqualTo("Outbound");
        assertThat(jsonModel.<String>get("$.status")).isEqualTo("Active");
        assertThat(jsonModel.<Boolean>get("$.isDefault")).isEqualTo(true);
        assertThat(jsonModel.<Boolean>get("$.allowSimultaneousConnections")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.connectionType")).isEqualTo("outbound pluggeable class");
        assertThat(jsonModel.<Integer>get("$.rescheduleRetryDelay.count")).isEqualTo(60);
        assertThat(jsonModel.<String>get("$.rescheduleRetryDelay.timeUnit")).isEqualTo("minutes");
        assertThat(jsonModel.<String>get("$.link.params.rel")).isEqualTo(LinkInfo.REF_SELF);
        assertThat(jsonModel.<String>get("$.link.href")).isEqualTo("http://localhost:9998/devices/XAS/connectionmethods/41");
        assertThat(jsonModel.<Integer>get("$.comWindow.start")).isEqualTo(7200000);
        assertThat(jsonModel.<Integer>get("$.comWindow.end")).isEqualTo(14400000);
        assertThat(jsonModel.<String>get("$.comPortPool.link.href")).isEqualTo("http://localhost:9998/comportpools/65");
        assertThat(jsonModel.<String>get("$.comPortPool.link.params.rel")).isEqualTo("related");
        assertThat(jsonModel.<List>get("$.properties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.properties[0].key")).isEqualTo("string.property");
        assertThat(jsonModel.<String>get("$.properties[0].propertyValueInfo.defaultValue")).isEqualTo("default");
        assertThat(jsonModel.<String>get("$.properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
        assertThat(jsonModel.<Boolean>get("$.properties[0].required")).isEqualTo(true);
    }

    @Test
    public void testCreateInboundConnectionTask() throws Exception {
        ConnectionTaskInfo info = new ConnectionTaskInfo();
        info.direction = ConnectionTaskType.Inbound;
        info.connectionMethod = new LinkInfo();
        info.connectionMethod.id = 333L;
        info.status = ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE;
        info.comPortPool = new LinkInfo();
        info.comPortPool.id = 13L;
        info.isDefault = true;

        InboundComPortPool inboundComPortPool = mock(InboundComPortPool.class);
        when(engineConfigurationService.findInboundComPortPool(info.comPortPool.id)).thenReturn(Optional.of(inboundComPortPool));

        DeviceType elec1 = mockDeviceType(101, "Electricity 1");
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1101L, "Default configuration", elec1);
        PartialInboundConnectionTask pct1 = mock(PartialInboundConnectionTask.class);
        when(pct1.getName()).thenReturn("new inbound");
        when(pct1.getId()).thenReturn(333L);
        PartialInboundConnectionTask pct2 = mock(PartialInboundConnectionTask.class);
        when(pct2.getName()).thenReturn("legacy");
        when(pct2.getId()).thenReturn(444L);
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.asList(pct1, pct2));
        Device deviceXas = mockDevice("XAS", "5544657642", deviceConfiguration);

        Device.InboundConnectionTaskBuilder builder = mock(Device.InboundConnectionTaskBuilder.class);
        when(deviceXas.getInboundConnectionTaskBuilder(pct1)).thenReturn(builder);
        ArgumentCaptor<InboundComPortPool> comPortPoolArgumentCaptor = ArgumentCaptor.forClass(InboundComPortPool.class);
        when(builder.setComPortPool(comPortPoolArgumentCaptor.capture())).thenReturn(builder);
        InboundConnectionTask inboundConnectionTask = mock(InboundConnectionTask.class);
        when(inboundConnectionTask.getId()).thenReturn(12345L);
        ArgumentCaptor<ConnectionTask.ConnectionTaskLifecycleStatus> connectionTaskLifecycleStatusArgumentCaptor = ArgumentCaptor.forClass(ConnectionTask.ConnectionTaskLifecycleStatus.class);
        when(builder.setConnectionTaskLifecycleStatus(connectionTaskLifecycleStatusArgumentCaptor.capture())).thenReturn(builder);
        when(builder.add()).thenReturn(inboundConnectionTask);

        Response response = target("devices/XAS/connectionmethods").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(response.getLocation().toString()).isEqualTo("http://localhost:9998/devices/XAS/connectionmethods/12345");
        assertThat(comPortPoolArgumentCaptor.getValue()).isEqualTo(inboundComPortPool);
        assertThat(connectionTaskLifecycleStatusArgumentCaptor.getValue()).isEqualTo(info.status);
        verify(connectionTaskService).setDefaultConnectionTask(inboundConnectionTask);

    }

    @Test
    public void testCreateScheduledConnectionTask() throws Exception {
        ConnectionTaskInfo info = new ConnectionTaskInfo();
        info.direction = ConnectionTaskType.Outbound;
        info.connectionMethod = new LinkInfo();
        info.connectionMethod.id = 333L;
        info.status = ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE;
        info.comPortPool = new LinkInfo();
        info.comPortPool.id = 13L;
        info.isDefault = true;
        info.allowSimultaneousConnections = true;
        info.properties = new ArrayList<>();
        PropertyInfo property = new PropertyInfo();
        info.properties.add(property);
        property.propertyValueInfo = new PropertyValueInfo<>(8080, "");
        property.key = "decimal.property";

        OutboundComPortPool outboundComPortPool = mock(OutboundComPortPool.class);
        when(engineConfigurationService.findOutboundComPortPool(info.comPortPool.id)).thenReturn(Optional.of(outboundComPortPool));

        DeviceType elec1 = mockDeviceType(101, "Electricity 1");
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1101L, "Default configuration", elec1);
        PropertySpec propertySpec = mockBigDecimalPropertySpec();
        PartialScheduledConnectionTask pct1 = mockPartialScheduledConnectionTask(333L, "new outbound", propertySpec);
        PartialScheduledConnectionTask pct2 = mockPartialScheduledConnectionTask(444L, "legacy");
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.asList(pct1, pct2));
        Device deviceXas = mockDevice("XAS", "5544657642", deviceConfiguration);

        Device.ScheduledConnectionTaskBuilder builder = mock(Device.ScheduledConnectionTaskBuilder.class);
        when(deviceXas.getScheduledConnectionTaskBuilder(pct1)).thenReturn(builder);
        ArgumentCaptor<OutboundComPortPool> comPortPoolArgumentCaptor = ArgumentCaptor.forClass(OutboundComPortPool.class);
        when(builder.setComPortPool(comPortPoolArgumentCaptor.capture())).thenReturn(builder);
        ScheduledConnectionTask scheduledConnectionTask = mock(ScheduledConnectionTask.class);
        when(scheduledConnectionTask.getId()).thenReturn(6789L);
        ArgumentCaptor<ConnectionTask.ConnectionTaskLifecycleStatus> connectionTaskLifecycleStatusArgumentCaptor = ArgumentCaptor.forClass(ConnectionTask.ConnectionTaskLifecycleStatus.class);
        when(builder.setConnectionTaskLifecycleStatus(connectionTaskLifecycleStatusArgumentCaptor.capture())).thenReturn(builder);
        when(builder.setSimultaneousConnectionsAllowed(true)).thenReturn(builder);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> objectArgumentCaptor = ArgumentCaptor.forClass(Object.class);
        when(builder.setProperty(stringArgumentCaptor.capture(), objectArgumentCaptor.capture())).thenReturn(builder);
        when(builder.add()).thenReturn(scheduledConnectionTask);

        // ACTUAL CALL
        Response response = target("devices/XAS/connectionmethods").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(response.getLocation().toString()).isEqualTo("http://localhost:9998/devices/XAS/connectionmethods/6789");
        assertThat(comPortPoolArgumentCaptor.getValue()).isEqualTo(outboundComPortPool);
        assertThat(connectionTaskLifecycleStatusArgumentCaptor.getValue()).isEqualTo(info.status);
        assertThat(stringArgumentCaptor.getValue()).isEqualTo("decimal.property");
        assertThat(objectArgumentCaptor.getValue()).isEqualTo(BigDecimal.valueOf(8080));

        verify(connectionTaskService).setDefaultConnectionTask(scheduledConnectionTask);

    }
}
