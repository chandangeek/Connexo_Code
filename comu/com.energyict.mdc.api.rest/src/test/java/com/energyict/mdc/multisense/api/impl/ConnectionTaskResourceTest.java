/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.energyict.mdc.device.config.ConnectionStrategy;
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
import net.minidev.json.JSONArray;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 7/13/15.
 */
public class ConnectionTaskResourceTest extends MultisensePublicApiJerseyTest {

    @Test
    public void testGetAllConnectionTasksOfDevice() throws Exception {
        DeviceType elec1 = mockDeviceType(101, "Electricity 1", 3333L);
        DeviceConfiguration deviceConfig = mockDeviceConfiguration(34L, "default configuration", elec1, 3333L);
        Device deviceXas = mockDevice("XAS", "5544657642", deviceConfig, 233L);

        Response response = target("devices/XAS/connectiontasks").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testConnectionTaskInfoFields() throws Exception {
        Response response = target("devices/XAS/connectiontasks").request().accept(MediaType.APPLICATION_JSON).method("PROPFIND", Response.class);
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<JSONArray>get("$")).containsOnly("numberOfSimultaneousConnections", "version", "comPortPool", "comWindow",
                "connectionStrategy", "connectionType", "id", "direction", "isDefault", "link", "connectionMethod", "nextExecutionSpecs", "properties",
                "rescheduleRetryDelay", "status", "device");
    }

    @Test
    public void testGetSingleConnectionTask() throws Exception {
        DeviceType elec1 = mockDeviceType(101, "Electricity 1", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1101L, "Default configuration", elec1, 3333L);
        Device deviceXas = mockDevice("XAS", "5544657642", deviceConfiguration, 3333L);
        OutboundComPortPool comPortPool = mock(OutboundComPortPool.class);
        when(comPortPool.getId()).thenReturn(65L);

        PartialScheduledConnectionTask partial = mockPartialScheduledConnectionTask(1681, "partial connection task", 3333L);
        when(partial.getConfiguration()).thenReturn(deviceConfiguration);
        ScheduledConnectionTask connectionTask = mockScheduledConnectionTask(41L, "connTask", deviceXas, comPortPool, partial, 3333L);
        when(connectionTaskService.findConnectionTask(41L)).thenReturn(Optional.of(connectionTask));
        PropertyInfo propertyInfo = new PropertyInfo("string.property", "string.property", new PropertyValueInfo<>("value", "default"), new PropertyTypeInfo(SimplePropertyType.TEXT, null, null, null), true);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Response response = target("devices/XAS/connectiontasks/41").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(41);
        assertThat(jsonModel.<Integer>get("$.connectionMethod.id")).isEqualTo(1681);
        assertThat(jsonModel.<String>get("$.connectionMethod.link.href")).isEqualTo("http://localhost:9998/devicetypes/101/deviceconfigurations/1101/partialconnectiontasks/1681");
        assertThat(jsonModel.<String>get("$.connectionMethod.link.params.rel")).isEqualTo("up");
        assertThat(jsonModel.<String>get("$.direction")).isEqualTo("Outbound");
        assertThat(jsonModel.<String>get("$.status")).isEqualTo("Active");
        assertThat(jsonModel.<Boolean>get("$.isDefault")).isEqualTo(true);
        assertThat(jsonModel.<Integer>get("$.numberOfSimultaneousConnections")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.connectionType")).isEqualTo("outbound pluggeable class");
        assertThat(jsonModel.<Integer>get("$.rescheduleRetryDelay.count")).isEqualTo(60);
        assertThat(jsonModel.<String>get("$.rescheduleRetryDelay.timeUnit")).isEqualTo("minutes");
        assertThat(jsonModel.<String>get("$.link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(jsonModel.<String>get("$.link.href")).isEqualTo("http://localhost:9998/devices/XAS/connectiontasks/41");
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
        long deviceVersion = 233L;

        ConnectionTaskInfo info = new ConnectionTaskInfo();
        info.direction = ConnectionTaskType.Inbound;
        info.device = new LinkInfo();
        info.device.version = deviceVersion;
        info.connectionMethod = new LinkInfo();
        info.connectionMethod.id = 333L;
        info.status = ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE;
        info.comPortPool = new LinkInfo();
        info.comPortPool.id = 13L;
        info.isDefault = true;

        InboundComPortPool inboundComPortPool = mock(InboundComPortPool.class);
        when(engineConfigurationService.findInboundComPortPool(info.comPortPool.id)).thenReturn(Optional.of(inboundComPortPool));

        DeviceType elec1 = mockDeviceType(101, "Electricity 1", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1101L, "Default configuration", elec1, 3333L);
        PartialInboundConnectionTask pct1 = mock(PartialInboundConnectionTask.class);
        when(pct1.getName()).thenReturn("new inbound");
        when(pct1.getId()).thenReturn(333L);
        PartialInboundConnectionTask pct2 = mock(PartialInboundConnectionTask.class);
        when(pct2.getName()).thenReturn("legacy");
        when(pct2.getId()).thenReturn(444L);
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.asList(pct1, pct2));
        Device deviceXas = mockDevice("XAS", "5544657642", deviceConfiguration, deviceVersion);

        Device.InboundConnectionTaskBuilder builder = mock(Device.InboundConnectionTaskBuilder.class);
        when(deviceXas.getInboundConnectionTaskBuilder(pct1)).thenReturn(builder);
        ArgumentCaptor<InboundComPortPool> comPortPoolArgumentCaptor = ArgumentCaptor.forClass(InboundComPortPool.class);
        when(builder.setComPortPool(comPortPoolArgumentCaptor.capture())).thenReturn(builder);
        InboundConnectionTask inboundConnectionTask = mock(InboundConnectionTask.class);
        when(inboundConnectionTask.getId()).thenReturn(12345L);
        ArgumentCaptor<ConnectionTask.ConnectionTaskLifecycleStatus> connectionTaskLifecycleStatusArgumentCaptor = ArgumentCaptor.forClass(ConnectionTask.ConnectionTaskLifecycleStatus.class);
        when(builder.setConnectionTaskLifecycleStatus(connectionTaskLifecycleStatusArgumentCaptor.capture())).thenReturn(builder);
        when(builder.add()).thenReturn(inboundConnectionTask);

        Response response = target("devices/XAS/connectiontasks").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(response.getLocation().toString()).isEqualTo("http://localhost:9998/devices/XAS/connectiontasks/12345");
        assertThat(comPortPoolArgumentCaptor.getValue()).isEqualTo(inboundComPortPool);
        assertThat(connectionTaskLifecycleStatusArgumentCaptor.getValue()).isEqualTo(info.status);
        verify(connectionTaskService).setDefaultConnectionTask(inboundConnectionTask);

    }

    @Test
    public void testUpdateInboundConnectionTask() throws Exception {
        ConnectionTaskInfo info = new ConnectionTaskInfo();
        info.direction = ConnectionTaskType.Inbound;
        info.device = new LinkInfo();
        info.device.version = 233L;
        info.connectionMethod = new LinkInfo();
        info.connectionMethod.id = 333L;
        info.status = ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE;
        info.comPortPool = new LinkInfo();
        info.comPortPool.id = 65L;
        info.isDefault = true;
        info.version = 13333L;

        InboundComPortPool inboundComPortPool = mock(InboundComPortPool.class);
        when(engineConfigurationService.findInboundComPortPool(info.comPortPool.id)).thenReturn(Optional.of(inboundComPortPool));

        DeviceType elec1 = mockDeviceType(101, "Electricity 1", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1101L, "Default configuration", elec1, 3333L);
        PartialInboundConnectionTask pct1 = mockPartialInboundConnectionTask(333L, "new inbound", deviceConfiguration, 3333L);
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Collections.singletonList(pct1));
        Device deviceXas = mockDevice("XAS", "5544657642", deviceConfiguration, 233L);

        InboundConnectionTask existing = mockInboundConnectionTask(12345L, "existing", deviceXas, inboundComPortPool, pct1, 13333L);
        when(deviceXas.getConnectionTasks()).thenReturn(Collections.singletonList(existing));

        ArgumentCaptor<InboundComPortPool> comPortPoolArgumentCaptor = ArgumentCaptor.forClass(InboundComPortPool.class);
        doNothing().when(existing).setComPortPool(comPortPoolArgumentCaptor.capture());
        PropertyInfo propertyInfo = new PropertyInfo("name", "name", new PropertyValueInfo<>("value", null), new PropertyTypeInfo(), false);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Response response = target("devices/XAS/connectiontasks/12345").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(comPortPoolArgumentCaptor.getValue()).isEqualTo(inboundComPortPool);
        verify(existing).deactivate();
        verify(connectionTaskService, never()).setDefaultConnectionTask(any());
        verify(connectionTaskService,never()).clearDefaultConnectionTask(any());
        verify(existing).save();
    }

    @Test
    public void testUpdateInboundConnectionTaskUnsetDefault() throws Exception {
        ConnectionTaskInfo info = new ConnectionTaskInfo();
        info.direction = ConnectionTaskType.Inbound;
        info.direction = ConnectionTaskType.Inbound;
        info.device = new LinkInfo();
        info.device.version = 233L;
        info.connectionMethod = new LinkInfo();
        info.connectionMethod.id = 333L;
        info.status = ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE;
        info.comPortPool = new LinkInfo();
        info.comPortPool.id = 65L;
        info.isDefault = false;
        info.version = 13333L;

        InboundComPortPool inboundComPortPool = mock(InboundComPortPool.class);
        when(engineConfigurationService.findInboundComPortPool(info.comPortPool.id)).thenReturn(Optional.of(inboundComPortPool));

        DeviceType elec1 = mockDeviceType(101, "Electricity 1", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1101L, "Default configuration", elec1, 3333L);
        PartialInboundConnectionTask pct1 = mockPartialInboundConnectionTask(333L, "new inbound", deviceConfiguration, 3333L);
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Collections.singletonList(pct1));
        Device deviceXas = mockDevice("XAS", "5544657642", deviceConfiguration, 233L);

        InboundConnectionTask existing = mockInboundConnectionTask(12345L, "existing", deviceXas, inboundComPortPool, pct1, 13333L);
        when(deviceXas.getConnectionTasks()).thenReturn(Collections.singletonList(existing));

        ArgumentCaptor<InboundComPortPool> comPortPoolArgumentCaptor = ArgumentCaptor.forClass(InboundComPortPool.class);
        doNothing().when(existing).setComPortPool(comPortPoolArgumentCaptor.capture());
        PropertyInfo propertyInfo = new PropertyInfo("name", "name", new PropertyValueInfo<>("value", null), new PropertyTypeInfo(), false);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Response response = target("devices/XAS/connectiontasks/12345").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(comPortPoolArgumentCaptor.getValue()).isEqualTo(inboundComPortPool);
        verify(existing).activate();
        verify(connectionTaskService,never()).setDefaultConnectionTask(any());
        verify(connectionTaskService,times(1)).clearDefaultConnectionTask(deviceXas);
        verify(existing).save();
    }

    @Test
    public void testUpdateInboundConnectionTaskVersionMissing() throws Exception {
        ConnectionTaskInfo info = new ConnectionTaskInfo();
        info.direction = ConnectionTaskType.Inbound;
        info.direction = ConnectionTaskType.Inbound;
        info.device = new LinkInfo();
        info.device.version = 233L;
        info.connectionMethod = new LinkInfo();
        info.connectionMethod.id = 333L;
        info.status = ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE;
        info.comPortPool = new LinkInfo();
        info.comPortPool.id = 65L;
        info.isDefault = false;
        info.version = null; // <<<<<<<<<<<<<<<<<<<<<<
        InboundComPortPool inboundComPortPool = mock(InboundComPortPool.class);
        when(engineConfigurationService.findInboundComPortPool(info.comPortPool.id)).thenReturn(Optional.of(inboundComPortPool));

        DeviceType elec1 = mockDeviceType(101, "Electricity 1", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1101L, "Default configuration", elec1, 3333L);
        PartialInboundConnectionTask pct1 = mockPartialInboundConnectionTask(333L, "new inbound", deviceConfiguration, 3333L);
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Collections.singletonList(pct1));
        Device deviceXas = mockDevice("XAS", "5544657642", deviceConfiguration, 233L);

        InboundConnectionTask existing = mockInboundConnectionTask(12345L, "existing", deviceXas, inboundComPortPool, pct1, 3333L);
        when(deviceXas.getConnectionTasks()).thenReturn(Collections.singletonList(existing));

        ArgumentCaptor<InboundComPortPool> comPortPoolArgumentCaptor = ArgumentCaptor.forClass(InboundComPortPool.class);
        doNothing().when(existing).setComPortPool(comPortPoolArgumentCaptor.capture());

        Response response = target("devices/XAS/connectiontasks/12345").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testCreateScheduledConnectionTask() throws Exception {
        ConnectionTaskInfo info = new ConnectionTaskInfo();
        info.direction = ConnectionTaskType.Outbound;
        info.device = new LinkInfo();
        info.device.version = 233L;
        info.connectionMethod = new LinkInfo();
        info.connectionMethod.id = 333L;
        info.status = ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE;
        info.comPortPool = new LinkInfo();
        info.comPortPool.id = 13L;
        info.isDefault = true;
        info.numberOfSimultaneousConnections = 2;
        info.properties = new ArrayList<>();
        PropertyInfo property = new PropertyInfo();
        info.properties.add(property);
        property.propertyValueInfo = new PropertyValueInfo<>(8080, "");
        property.key = "decimal.property";

        OutboundComPortPool outboundComPortPool = mock(OutboundComPortPool.class);
        when(engineConfigurationService.findOutboundComPortPool(info.comPortPool.id)).thenReturn(Optional.of(outboundComPortPool));

        DeviceType elec1 = mockDeviceType(101, "Electricity 1", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1101L, "Default configuration", elec1, 3333L);
        PropertySpec propertySpec = mockBigDecimalPropertySpec();
        PartialScheduledConnectionTask pct1 = mockPartialScheduledConnectionTask(333L, "new outbound", 3333L, propertySpec);
        PartialScheduledConnectionTask pct2 = mockPartialScheduledConnectionTask(444L, "legacy", 3333L);
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.asList(pct1, pct2));
        Device deviceXas = mockDevice("XAS", "5544657642", deviceConfiguration, 233L);

        Device.ScheduledConnectionTaskBuilder builder = mock(Device.ScheduledConnectionTaskBuilder.class);
        when(deviceXas.getScheduledConnectionTaskBuilder(pct1)).thenReturn(builder);
        ArgumentCaptor<OutboundComPortPool> comPortPoolArgumentCaptor = ArgumentCaptor.forClass(OutboundComPortPool.class);
        when(builder.setComPortPool(comPortPoolArgumentCaptor.capture())).thenReturn(builder);
        ScheduledConnectionTask scheduledConnectionTask = mock(ScheduledConnectionTask.class);
        when(scheduledConnectionTask.getId()).thenReturn(6789L);
        ArgumentCaptor<ConnectionTask.ConnectionTaskLifecycleStatus> connectionTaskLifecycleStatusArgumentCaptor = ArgumentCaptor.forClass(ConnectionTask.ConnectionTaskLifecycleStatus.class);
        when(builder.setConnectionTaskLifecycleStatus(connectionTaskLifecycleStatusArgumentCaptor.capture())).thenReturn(builder);
        when(builder.setNumberOfSimultaneousConnections(1)).thenReturn(builder);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> objectArgumentCaptor = ArgumentCaptor.forClass(Object.class);
        when(builder.setProperty(stringArgumentCaptor.capture(), objectArgumentCaptor.capture())).thenReturn(builder);
        when(builder.add()).thenReturn(scheduledConnectionTask);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(property);
        when(propertyValueInfoService.findPropertyValue(any(), any())).thenReturn(property.getPropertyValueInfo().getValue());
        // ACTUAL CALL
        Response response = target("devices/XAS/connectiontasks").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(response.getLocation().toString()).isEqualTo("http://localhost:9998/devices/XAS/connectiontasks/6789");
        assertThat(comPortPoolArgumentCaptor.getValue()).isEqualTo(outboundComPortPool);
        assertThat(connectionTaskLifecycleStatusArgumentCaptor.getValue()).isEqualTo(info.status);
        assertThat(stringArgumentCaptor.getValue()).isEqualTo("decimal.property");
        assertThat(objectArgumentCaptor.getValue()).isEqualTo(8080);

        verify(connectionTaskService).setDefaultConnectionTask(scheduledConnectionTask);
    }

    @Test
    public void testCreateScheduledConnectionTaskWithoutDeviceVersion() throws Exception {
        ConnectionTaskInfo info = new ConnectionTaskInfo();
//        info.direction = ConnectionTaskType.Outbound;
        info.device = new LinkInfo();
        info.device.version = null;

        // ACTUAL CALL
        Response response = target("devices/XAS/connectiontasks").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testUpdateScheduledConnectionTask() throws Exception {
        ConnectionTaskInfo info = new ConnectionTaskInfo();
        info.direction = ConnectionTaskType.Outbound;
        info.device = new LinkInfo();
        info.device.version = 233L;
        info.connectionMethod = new LinkInfo();
        info.connectionMethod.id = 333L;
        info.status = ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE;
        info.connectionStrategy = ConnectionStrategy.MINIMIZE_CONNECTIONS;
        info.comPortPool = new LinkInfo();
        info.comPortPool.id = 13L;
        info.isDefault = true;
        info.numberOfSimultaneousConnections = 2;
        info.version = 13333L;
        info.properties = new ArrayList<>();
        PropertyInfo property = new PropertyInfo();
        info.properties.add(property);
        property.propertyValueInfo = new PropertyValueInfo<>(8080, "");
        property.key = "decimal.property";

        OutboundComPortPool outboundComPortPool = mock(OutboundComPortPool.class);
        when(engineConfigurationService.findOutboundComPortPool(info.comPortPool.id)).thenReturn(Optional.of(outboundComPortPool));

        DeviceType elec1 = mockDeviceType(101, "Electricity 1", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1101L, "Default configuration", elec1, 3333L);
        PropertySpec propertySpec = mockBigDecimalPropertySpec();
        PartialScheduledConnectionTask pct1 = mockPartialScheduledConnectionTask(333L, "new outbound", 3333L, propertySpec);
        PartialScheduledConnectionTask pct2 = mockPartialScheduledConnectionTask(444L, "legacy", 3333L);
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.asList(pct1, pct2));
        Device deviceXas = mockDevice("XAS", "5544657642", deviceConfiguration, 233L);
        ScheduledConnectionTask existing = mockScheduledConnectionTask(123456789, "existing", deviceXas, outboundComPortPool, pct1, 13333L);
        when(existing.isDefault()).thenReturn(false); // override
        when(deviceXas.getConnectionTasks()).thenReturn(Collections.singletonList(existing));
        when(connectionTaskService.findConnectionTask(123456789)).thenReturn(Optional.of(existing));
        when(pct1.getConfiguration()).thenReturn(deviceConfiguration);
        when(pct2.getConfiguration()).thenReturn(deviceConfiguration);
        // ACTUAL CALL
        PropertyInfo propertyInfo = new PropertyInfo("decimal.property", "decimal.property", new PropertyValueInfo<>(BigDecimal.valueOf(8080), 8080), new PropertyTypeInfo(SimplePropertyType.TEXT, null, null, null), true);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        when(propertyValueInfoService.findPropertyValue(any(), any())).thenReturn(propertyInfo.getPropertyValueInfo().getValue());
        Response response = target("devices/XAS/connectiontasks/123456789").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(existing).setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        verify(existing).setNumberOfSimultaneousConnections(2);
        verify(existing).setProperty("decimal.property", BigDecimal.valueOf(8080));
        verify(connectionTaskService).setDefaultConnectionTask(existing);
        verify(connectionTaskService, never()).clearDefaultConnectionTask(any());
        verify(existing).save();
    }

}
