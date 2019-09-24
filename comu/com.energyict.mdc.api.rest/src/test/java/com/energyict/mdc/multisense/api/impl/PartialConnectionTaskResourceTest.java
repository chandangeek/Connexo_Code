/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.PartialConnectionTask;

import com.jayway.jsonpath.JsonModel;
import net.minidev.json.JSONArray;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PartialConnectionTaskResourceTest extends MultisensePublicApiJerseyTest {

    @Before
    public void setup() {
        DeviceType deviceType = mockDeviceType(112L, "device type", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(113L, "Default configuration", deviceType, 3333L);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        ProtocolDialectConfigurationProperties properties = mock(ProtocolDialectConfigurationProperties.class);
        when(properties.getId()).thenReturn(25L);
        when(properties.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(properties.getDeviceProtocolDialectName()).thenReturn("Protocol Dialect Name");

        PartialConnectionTask partialConnectionTask1 = mockPartialInboundConnectionTask(114L, "partial conn task 114", deviceConfiguration, 3333L, properties);
        PartialConnectionTask partialConnectionTask2 = mockPartialInboundConnectionTask(124L, "partial conn task 124", deviceConfiguration, 3333L, properties);
        PartialConnectionTask partialConnectionTask3 = mockPartialOutboundConnectionTask(134L, "partial conn task 134", deviceConfiguration, 3333L, properties);
        PartialConnectionTask partialConnectionTask4 = mockPartialOutboundConnectionTask(135L, "partial conn task 135", deviceConfiguration, 3333L, properties, true, null);
        PartialConnectionTask partialConnectionTask5 = mockPartialOutboundConnectionTask(136L, "partial conn task 136", deviceConfiguration, 3333L, properties, false, mockUPLConnectionFunction(1, "CF_1"));
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.asList(partialConnectionTask1, partialConnectionTask2, partialConnectionTask3, partialConnectionTask4, partialConnectionTask5));
    }

    @Test
    public void testPartialConnectionTaskFields() throws Exception {
        Response response = target("/devicetypes/112/deviceconfigurations/113/partialconnectiontasks").request().accept(MediaType.APPLICATION_JSON).method("PROPFIND", Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<JSONArray>get("$")).containsOnly("id", "version", "name", "link", "direction", "comWindow", "rescheduleRetryDelay", "nextExecutionSpecs",
                "connectionType", "comPortPool", "isDefault", "connectionFunction", "connectionStrategy", "numberOfSimultaneousConnections", "properties", "protocolDialectConfigurationProperties");
    }

    @Test
    public void testGetPartialConnectionTasks() throws Exception {
        PropertyInfo propertyInfo = new PropertyInfo("string.property", "string.property", new PropertyValueInfo<>("Hello world 1", "default"), new PropertyTypeInfo(SimplePropertyType.TEXT, null, null, null), true);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Response response = target("/devicetypes/112/deviceconfigurations/113/partialconnectiontasks").queryParam("start", 0).queryParam("limit", 5).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<List>get("$.link")).hasSize(1);
        assertThat(jsonModel.<String>get("$.link[0].params.rel")).isEqualTo("current");
        assertThat(jsonModel.<String>get("$.link[0].href")).isEqualTo("http://localhost:9998/devicetypes/112/deviceconfigurations/113/partialconnectiontasks?start=0&limit=5");
        assertThat(jsonModel.<List>get("$.data")).hasSize(5);
        assertThat(jsonModel.<Integer>get("$.data[0].id")).isEqualTo(114);
        assertThat(jsonModel.<String>get("$.data[0].name")).isEqualTo("partial conn task 114");
        assertThat(jsonModel.<String>get("$.data[0].link.params.rel")).isEqualTo("self");
        assertThat(jsonModel.<String>get("$.data[0].link.href")).isEqualTo("http://localhost:9998/devicetypes/112/deviceconfigurations/113/partialconnectiontasks/114");
        assertThat(jsonModel.<Integer>get("$.data[2].id")).isEqualTo(134);
        assertThat(jsonModel.<String>get("$.data[2].name")).isEqualTo("partial conn task 134");
        assertThat(jsonModel.<String>get("$.data[2].link.params.rel")).isEqualTo("self");
        assertThat(jsonModel.<String>get("$.data[2].link.href")).isEqualTo("http://localhost:9998/devicetypes/112/deviceconfigurations/113/partialconnectiontasks/134");
        assertThat(jsonModel.<Integer>get("$.data[4].id")).isEqualTo(136);
        assertThat(jsonModel.<String>get("$.data[4].name")).isEqualTo("partial conn task 136");
        assertThat(jsonModel.<String>get("$.data[4].link.params.rel")).isEqualTo("self");
        assertThat(jsonModel.<String>get("$.data[4].link.href")).isEqualTo("http://localhost:9998/devicetypes/112/deviceconfigurations/113/partialconnectiontasks/136");
    }

    @Test
    public void testGetPartialInboundConnectionTask() throws Exception {
        PropertyInfo propertyInfo = new PropertyInfo("string.property", "string.property", new PropertyValueInfo<>("Hello world 1", "default"), new PropertyTypeInfo(SimplePropertyType.TEXT, null, null, null), true);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Response response = target("/devicetypes/112/deviceconfigurations/113/partialconnectiontasks/124").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(124);
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("partial conn task 124");
        assertThat(jsonModel.<String>get("$.link.params.rel")).isEqualTo("self");
        assertThat(jsonModel.<String>get("$.link.href")).isEqualTo("http://localhost:9998/devicetypes/112/deviceconfigurations/113/partialconnectiontasks/124");
        assertThat(jsonModel.<String>get("$.direction")).isEqualTo("Inbound");
        assertThat(jsonModel.<String>get("$.connectionType")).isEqualTo("inbound pluggeable class");
        assertThat(jsonModel.<Integer>get("$.comPortPool.id")).isEqualTo(65);
        assertThat(jsonModel.<String>get("$.comPortPool.link.href")).isEqualTo("http://localhost:9998/comportpools/65");
        assertThat(jsonModel.<String>get("$.comPortPool.link.params.rel")).isEqualTo("related");
        assertThat(jsonModel.<Boolean>get("$.isDefault")).isEqualTo(false);
        assertThat(jsonModel.<List>get("$.properties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.properties[0].key")).isEqualTo("string.property");
        assertThat(jsonModel.<String>get("$.properties[0].propertyValueInfo.defaultValue")).isEqualTo("default");
        assertThat(jsonModel.<String>get("$.properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
        assertThat(jsonModel.<Boolean>get("$.properties[0].required")).isEqualTo(true);

    }

    @Test
    public void testGetPartialOutboundConnectionTask() throws Exception {
        PropertyInfo propertyInfo = new PropertyInfo("string.property", "string.property", new PropertyValueInfo<>("Hello world 1", "default"), new PropertyTypeInfo(SimplePropertyType.TEXT, null, null, null), true);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Response response = target("/devicetypes/112/deviceconfigurations/113/partialconnectiontasks/134").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(134);
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("partial conn task 134");
        assertThat(jsonModel.<String>get("$.link.params.rel")).isEqualTo("self");
        assertThat(jsonModel.<String>get("$.link.href")).isEqualTo("http://localhost:9998/devicetypes/112/deviceconfigurations/113/partialconnectiontasks/134");
        assertThat(jsonModel.<String>get("$.direction")).isEqualTo("Outbound");
        assertThat(jsonModel.<String>get("$.connectionType")).isEqualTo("outbound pluggeable class");
        assertThat(jsonModel.<Integer>get("$.comPortPool.id")).isEqualTo(165);
        assertThat(jsonModel.<String>get("$.comPortPool.link.href")).isEqualTo("http://localhost:9998/comportpools/165");
        assertThat(jsonModel.<String>get("$.comPortPool.link.params.rel")).isEqualTo("related");
        assertThat(jsonModel.<Boolean>get("$.isDefault")).isEqualTo(false);
        assertThat(jsonModel.<String>get("$.connectionFunction")).isEqualTo(null);
        assertThat(jsonModel.<List>get("$.properties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.properties[0].key")).isEqualTo("string.property");
        assertThat(jsonModel.<String>get("$.properties[0].propertyValueInfo.defaultValue")).isEqualTo("default");
        assertThat(jsonModel.<String>get("$.properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
        assertThat(jsonModel.<Boolean>get("$.properties[0].required")).isEqualTo(true);
        assertThat(jsonModel.<Integer>get("$.comWindow.start")).isEqualTo(7200000);
        assertThat(jsonModel.<Integer>get("$.comWindow.end")).isEqualTo(14400000);
        assertThat(jsonModel.<Integer>get("$.numberOfSimultaneousConnections")).isEqualTo(2);
        assertThat(jsonModel.<Integer>get("$.rescheduleRetryDelay.count")).isEqualTo(60);
        assertThat(jsonModel.<String>get("$.rescheduleRetryDelay.timeUnit")).isEqualTo("minutes");
    }

    @Test
    public void testGetPartialOutboundConnectionTaskSetAsDefault() throws Exception {
        PropertyInfo propertyInfo = new PropertyInfo("string.property", "string.property", new PropertyValueInfo<>("Hello world 1", "default"), new PropertyTypeInfo(SimplePropertyType.TEXT, null, null, null), true);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Response response = target("/devicetypes/112/deviceconfigurations/113/partialconnectiontasks/135").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(135);
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("partial conn task 135");
        assertThat(jsonModel.<String>get("$.link.params.rel")).isEqualTo("self");
        assertThat(jsonModel.<String>get("$.link.href")).isEqualTo("http://localhost:9998/devicetypes/112/deviceconfigurations/113/partialconnectiontasks/135");
        assertThat(jsonModel.<String>get("$.direction")).isEqualTo("Outbound");
        assertThat(jsonModel.<String>get("$.connectionType")).isEqualTo("outbound pluggeable class");
        assertThat(jsonModel.<Integer>get("$.comPortPool.id")).isEqualTo(165);
        assertThat(jsonModel.<String>get("$.comPortPool.link.href")).isEqualTo("http://localhost:9998/comportpools/165");
        assertThat(jsonModel.<String>get("$.comPortPool.link.params.rel")).isEqualTo("related");
        assertThat(jsonModel.<Boolean>get("$.isDefault")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.connectionFunction")).isEqualTo(null);
        assertThat(jsonModel.<List>get("$.properties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.properties[0].key")).isEqualTo("string.property");
        assertThat(jsonModel.<String>get("$.properties[0].propertyValueInfo.defaultValue")).isEqualTo("default");
        assertThat(jsonModel.<String>get("$.properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
        assertThat(jsonModel.<Boolean>get("$.properties[0].required")).isEqualTo(true);
        assertThat(jsonModel.<Integer>get("$.comWindow.start")).isEqualTo(7200000);
        assertThat(jsonModel.<Integer>get("$.comWindow.end")).isEqualTo(14400000);
        assertThat(jsonModel.<Integer>get("$.numberOfSimultaneousConnections")).isEqualTo(2);
        assertThat(jsonModel.<Integer>get("$.rescheduleRetryDelay.count")).isEqualTo(60);
        assertThat(jsonModel.<String>get("$.rescheduleRetryDelay.timeUnit")).isEqualTo("minutes");
    }

    @Test
    public void testGetPartialOutboundConnectionTaskHavingConnectionFunctionSet() throws Exception {
        PropertyInfo propertyInfo = new PropertyInfo("string.property", "string.property", new PropertyValueInfo<>("Hello world 1", "default"), new PropertyTypeInfo(SimplePropertyType.TEXT, null, null, null), true);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Response response = target("/devicetypes/112/deviceconfigurations/113/partialconnectiontasks/136").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(136);
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("partial conn task 136");
        assertThat(jsonModel.<String>get("$.link.params.rel")).isEqualTo("self");
        assertThat(jsonModel.<String>get("$.link.href")).isEqualTo("http://localhost:9998/devicetypes/112/deviceconfigurations/113/partialconnectiontasks/136");
        assertThat(jsonModel.<String>get("$.direction")).isEqualTo("Outbound");
        assertThat(jsonModel.<String>get("$.connectionType")).isEqualTo("outbound pluggeable class");
        assertThat(jsonModel.<Integer>get("$.comPortPool.id")).isEqualTo(165);
        assertThat(jsonModel.<String>get("$.comPortPool.link.href")).isEqualTo("http://localhost:9998/comportpools/165");
        assertThat(jsonModel.<String>get("$.comPortPool.link.params.rel")).isEqualTo("related");
        assertThat(jsonModel.<Boolean>get("$.isDefault")).isEqualTo(false);
        assertThat(jsonModel.<Integer>get("$.connectionFunction.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.connectionFunction.link.href")).isEqualTo("http://localhost:9998/pluggableclasses/12544/connectionfunctions/1");
        assertThat(jsonModel.<String>get("$.connectionFunction.link.params.rel")).isEqualTo("related");
        assertThat(jsonModel.<List>get("$.properties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.properties[0].key")).isEqualTo("string.property");
        assertThat(jsonModel.<String>get("$.properties[0].propertyValueInfo.defaultValue")).isEqualTo("default");
        assertThat(jsonModel.<String>get("$.properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
        assertThat(jsonModel.<Boolean>get("$.properties[0].required")).isEqualTo(true);
        assertThat(jsonModel.<Integer>get("$.comWindow.start")).isEqualTo(7200000);
        assertThat(jsonModel.<Integer>get("$.comWindow.end")).isEqualTo(14400000);
        assertThat(jsonModel.<Integer>get("$.numberOfSimultaneousConnections")).isEqualTo(2);
        assertThat(jsonModel.<Integer>get("$.rescheduleRetryDelay.count")).isEqualTo(60);
        assertThat(jsonModel.<String>get("$.rescheduleRetryDelay.timeUnit")).isEqualTo("minutes");
    }

    @Test
    public void testGetPartialConnectionTaskWithFieldSelection() throws Exception {
        Response response = target("/devicetypes/112/deviceconfigurations/113/partialconnectiontasks/124").queryParam("fields", "name").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.id")).isNull();
        assertThat(jsonModel.<Integer>get("$.link")).isNull();
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("partial conn task 124");
        assertThat(jsonModel.<String>get("$.direction")).isNull();
        assertThat(jsonModel.<String>get("$.protocolDialectConfigurationProperties")).isNull();
    }

}
