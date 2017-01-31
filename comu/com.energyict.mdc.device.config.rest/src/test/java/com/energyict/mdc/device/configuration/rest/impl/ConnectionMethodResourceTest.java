/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.time.impl.MessageSeeds;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.pluggable.rest.impl.properties.SimplePropertyType;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConnectionMethodResourceTest extends DeviceConfigurationApplicationJerseyTest {

    public static final long OK_VERSION = 24L;
    public static final long BAD_VERSION = 17L;

    private PartialInboundConnectionTask partialInboundConnectionTask;
    private PartialScheduledConnectionTask partialScheduledConnectionTask;

    @Test
    public void testGetConnectionMethod() throws Exception {
        String response = target("/devicetypes/11/deviceconfigurations/12/connectionmethods").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
    }

    @Test
    public void testUpdateConnectionMethodSetComPortPoolToNull() throws Exception {
        InboundConnectionMethodInfo info = new InboundConnectionMethodInfo();
        info.name = "name";
        info.comPortPool = null; // should be set to null
        info.comWindowStart = 6;
        info.comWindowEnd = 12;
        info.connectionTypePluggableClass = "pluggableClass";
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(12L, OK_VERSION);
        Response response = target("/devicetypes/11/deviceconfigurations/12/connectionmethods/13").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ArgumentCaptor<InboundComPortPool> comPortPoolArgumentCaptor = ArgumentCaptor.forClass(InboundComPortPool.class);
        verify(partialInboundConnectionTask).setComportPool(comPortPoolArgumentCaptor.capture());
        assertThat(comPortPoolArgumentCaptor.getValue()).isNull();
    }


    @Test
    public void testUpdateConnectionMethodBadVersion() throws Exception {
        InboundConnectionMethodInfo info = new InboundConnectionMethodInfo();
        info.name = "name";
        info.version = BAD_VERSION;
        info.parent = new VersionInfo<>(12L, OK_VERSION);
        Response response = target("/devicetypes/11/deviceconfigurations/12/connectionmethods/13").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testUpdateConnectionMethodSetInvalidTimeout() throws Exception {
        InboundConnectionMethodInfo info = new InboundConnectionMethodInfo();
        info.name = "name";
        info.comPortPool = null; // should be set to null
        info.comWindowStart = 6;
        info.comWindowEnd = 12;
        info.connectionTypePluggableClass = "pluggableClass";
        info.properties = new ArrayList<>();
        PropertyInfo propertyInfo = new PropertyInfo();
        propertyInfo.key = "connectionTimeOut";
        propertyInfo.name = "connectionTimeOut";
        propertyInfo.required = false;
        TimeDurationInfo timeDurationInfo = new TimeDurationInfo();
        timeDurationInfo.timeUnit="1"; // INVALID
        propertyInfo.propertyValueInfo = new PropertyValueInfo<TimeDurationInfo>(timeDurationInfo,null,null,true);
        propertyInfo.propertyTypeInfo = new PropertyTypeInfo();
        propertyInfo.propertyTypeInfo.simplePropertyType = SimplePropertyType.TIMEDURATION;
        info.properties.add(propertyInfo);
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(12L, OK_VERSION);
        doThrow(new LocalizedFieldValidationException(MessageSeeds.UNKNOWN_TIME_UNIT, "properties.id", timeDurationInfo.timeUnit)).when(propertyValueInfoService).findPropertyValue(any(), any());
        Response response = target("/devicetypes/11/deviceconfigurations/12/connectionmethods/13").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream)response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(jsonModel.<String>get("$.errors[0].id")).startsWith("properties.");
        assertThat(jsonModel.<String>get("$.errors[0].msg")).isEqualTo("Unknown time unit '" + timeDurationInfo.timeUnit + "'");
    }

    @Test
    public void testUpdateScheduledConnectionMethodSetComPortPoolToNull() throws Exception {
        ScheduledConnectionMethodInfo info = new ScheduledConnectionMethodInfo();
        info.name = "name";
        info.comPortPool = null; // should be set to null
        info.comWindowStart = 6;
        info.comWindowEnd = 12;
        info.connectionTypePluggableClass = "pluggableClass";
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(12L, OK_VERSION);
        Response response = target("/devicetypes/11/deviceconfigurations/12/connectionmethods/14").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ArgumentCaptor<OutboundComPortPool> comPortPoolArgumentCaptor = ArgumentCaptor.forClass(OutboundComPortPool.class);
        verify(partialScheduledConnectionTask).setComportPool(comPortPoolArgumentCaptor.capture());
        assertThat(comPortPoolArgumentCaptor.getValue()).isNull();
    }

    @Before
    public void setup() {
        super.setup();
        DeviceType deviceType = mockDeviceType("device", 11);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration("config", 12);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        partialInboundConnectionTask = mockPartialInboundConnectionTask(13L, "partial inbound");
        when(partialInboundConnectionTask.getConfiguration()).thenReturn(deviceConfiguration);
        partialScheduledConnectionTask = mockPartialScheduledConnectionTask(14L, "partial scheduled");
        when(partialScheduledConnectionTask.getConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.asList(partialInboundConnectionTask, partialScheduledConnectionTask));

    }

    protected DeviceConfiguration mockDeviceConfiguration(String name, long id){
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getName()).thenReturn(name);
        when(deviceConfiguration.getId()).thenReturn(id);
        RegisterSpec registerSpec = mock(RegisterSpec.class);
        RegisterType registerType = mock(RegisterType.class);
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        when(registerType.getId()).thenReturn(101L);
        when(deviceConfiguration.getRegisterSpecs()).thenReturn(Collections.singletonList(registerSpec));
        when(deviceConfigurationService.findDeviceConfiguration(id)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(id, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(id, BAD_VERSION)).thenReturn(Optional.empty());
        return deviceConfiguration;
    }

    private PartialInboundConnectionTask mockPartialInboundConnectionTask(long id, String name) {
        PartialInboundConnectionTask partialConnectionTask = mock(PartialInboundConnectionTask.class);
        when(partialConnectionTask.getId()).thenReturn(id);
        when(partialConnectionTask.getName()).thenReturn(name);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionType.getPropertySpecs()).thenReturn(Collections.emptyList());
        when(partialConnectionTask.getTypedProperties()).thenReturn(TypedProperties.empty());
        when(partialConnectionTask.getConnectionType()).thenReturn(connectionType);
        ConnectionTypePluggableClass pluggableClass = mock(ConnectionTypePluggableClass.class);
        when(pluggableClass.getName()).thenReturn("pluggableClass");

        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn("connectionTimeOut");
        when(propertySpec.isRequired()).thenReturn(false);
        when(propertySpec.getValueFactory()).thenReturn(new TimeDurationValueFactory());
        when(pluggableClass.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(protocolPluggableService.findConnectionTypePluggableClassByName("pluggableClass")).thenReturn(Optional.of(pluggableClass));
        when(deviceConfigurationService.findPartialConnectionTask(id)).thenReturn(Optional.of(partialConnectionTask));
        when(deviceConfigurationService.findAndLockPartialConnectionTaskByIdAndVersion(id, OK_VERSION)).thenReturn(Optional.of(partialConnectionTask));
        when(deviceConfigurationService.findAndLockPartialConnectionTaskByIdAndVersion(id, BAD_VERSION)).thenReturn(Optional.empty());
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(comPortPool.getName()).thenReturn("com port pool");
        when(partialConnectionTask.getComPortPool()).thenReturn(comPortPool);
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggableClass);
        return partialConnectionTask;
    }

    private PartialScheduledConnectionTask mockPartialScheduledConnectionTask(long id, String name) {
        PartialScheduledConnectionTask partialConnectionTask = mock(PartialScheduledConnectionTask.class);
        when(partialConnectionTask.getId()).thenReturn(id);
        when(partialConnectionTask.getName()).thenReturn(name);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionType.getPropertySpecs()).thenReturn(Collections.emptyList());
        when(partialConnectionTask.getTypedProperties()).thenReturn(TypedProperties.empty());
        when(partialConnectionTask.getConnectionType()).thenReturn(connectionType);
        ConnectionTypePluggableClass pluggableClass = mock(ConnectionTypePluggableClass.class);
        when(pluggableClass.getName()).thenReturn("pluggableClass");
        when(protocolPluggableService.findConnectionTypePluggableClassByName("pluggableClass")).thenReturn(Optional.of(pluggableClass));
        when(deviceConfigurationService.findPartialConnectionTask(id)).thenReturn(Optional.of(partialConnectionTask));
        when(deviceConfigurationService.findAndLockPartialConnectionTaskByIdAndVersion(id, OK_VERSION)).thenReturn(Optional.of(partialConnectionTask));
        when(deviceConfigurationService.findAndLockPartialConnectionTaskByIdAndVersion(id, BAD_VERSION)).thenReturn(Optional.empty());
        OutboundComPortPool comPortPool = mock(OutboundComPortPool.class);
        when(comPortPool.getName()).thenReturn("com port pool");
        when(partialConnectionTask.getComPortPool()).thenReturn(comPortPool);
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggableClass);
        return partialConnectionTask;
    }


    protected DeviceType mockDeviceType(String name, long id) {
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getName()).thenReturn(name);
        when(deviceType.getId()).thenReturn(id);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceConfigurationService.findDeviceType(id)).thenReturn(Optional.of(deviceType));
        when(deviceConfigurationService.findAndLockDeviceType(id, OK_VERSION)).thenReturn(Optional.of(deviceType));
        when(deviceConfigurationService.findAndLockDeviceType(id, BAD_VERSION)).thenReturn(Optional.empty());
        return deviceType;
    }

}
