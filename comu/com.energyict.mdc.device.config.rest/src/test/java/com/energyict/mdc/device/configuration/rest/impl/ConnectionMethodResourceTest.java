/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.TemporalAmountValueFactory;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.ConnectionFunction;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.upl.TypedProperties;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class ConnectionMethodResourceTest extends DeviceConfigurationApplicationJerseyTest {

    public static final long OK_VERSION = 24L;
    public static final long BAD_VERSION = 17L;
    private static final String COM_PORT_POOL_NAME = "com port pool";

    private ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties;
    private PartialInboundConnectionTask partialInboundConnectionTask;
    private PartialScheduledConnectionTask partialScheduledConnectionTask;
    DeviceType deviceType;
    DeviceConfiguration deviceConfiguration;
    ConnectionFunction connectionFunction_1, connectionFunction_2, connectionFunction_3;

    @Override
    public void setupMocks() {
        super.setupMocks();
        protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(protocolDialectConfigurationProperties.getId()).thenReturn(1234L);
        when(protocolDialectConfigurationProperties.getDeviceProtocolDialectName()).thenReturn("Dialectje");

        when(deviceConfigurationService.getProtocolDialectConfigurationProperties(1234L)).thenReturn(Optional.of(protocolDialectConfigurationProperties));
        connectionFunction_1 = mockConnectionFunction(1, "CF_1", "CF 1");
        connectionFunction_2 = mockConnectionFunction(2, "CF_2", "CF 2");
        connectionFunction_3 = mockConnectionFunction(3, "CF_3", "CF 3");
    }

    @Test
    public void testGetConnectionMethods() throws Exception {
        String response = target("/devicetypes/11/deviceconfigurations/12/connectionmethods").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
    }

    @Test
    public void testGetConnectionMethod() throws Exception {
        when(partialScheduledConnectionTask.getConnectionFunction()).thenReturn(Optional.of(connectionFunction_2));

        String response = target("/devicetypes/11/deviceconfigurations/12/connectionmethods/14").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(14);
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("partial scheduled");
        assertThat(jsonModel.<Integer>get("$.connectionTypePluggableClass.id")).isEqualTo(0);
        assertThat(jsonModel.<String>get("$.connectionTypePluggableClass.name")).isEqualTo("pluggableClass");
        assertThat(jsonModel.<Boolean>get("$.isDefault")).isFalse();
        assertThat(jsonModel.<String>get("$.direction")).isEqualTo("Outbound");
        assertThat(jsonModel.<String>get("$.comPortPool")).isEqualTo("com port pool");
        assertThat(jsonModel.<Integer>get("$.numberOfSimultaneousConnections")).isEqualTo(0);
        assertThat(jsonModel.<Integer>get("$.comWindowStart")).isEqualTo(0);
        assertThat(jsonModel.<Integer>get("$.comWindowEnd")).isEqualTo(0);
        assertThat(jsonModel.<Integer>get("$.protocolDialectConfigurationProperties.id")).isEqualTo(11111111);
        assertThat(jsonModel.<String>get("$.protocolDialectConfigurationProperties.name")).isEqualTo("Mocked Protocol Dialect");
        assertThat(jsonModel.<String>get("$.protocolDialectConfigurationProperties.displayName")).isEqualTo("DisplayName of the mocked Protocol Dialect");
        assertThat(jsonModel.<Integer>get("$.connectionFunctionInfo.id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.connectionFunctionInfo.localizedValue")).isEqualTo("CF 2");
        assertThat(jsonModel.<Integer>get("$.version")).isEqualTo(0);
        assertThat(jsonModel.<Integer>get("$.parent.id")).isEqualTo(12);
    }

    @Test
    public void testGetConnectionMethodWhenNoConnectionFunctionIsSet() throws Exception {
        //A. No connection function set, but the deviceProtocolPluggableClass has support for connection functions
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = deviceType.getDeviceProtocolPluggableClass().get();
        when(deviceProtocolPluggableClass.getProvidedConnectionFunctions()).thenReturn(Arrays.asList(connectionFunction_1, connectionFunction_2));
        when(partialScheduledConnectionTask.getConnectionFunction()).thenReturn(Optional.empty());

        String response = target("/devicetypes/11/deviceconfigurations/12/connectionmethods/14").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(14);
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("partial scheduled");
        assertThat(jsonModel.<Boolean>get("$.isDefault")).isFalse();
        assertThat(jsonModel.<Integer>get("$.connectionFunctionInfo.id")).isEqualTo(-1);
        assertThat(jsonModel.<String>get("$.connectionFunctionInfo.localizedValue")).isEqualTo("None");

        //A. No connection function set and the deviceProtocolPluggableClass has no support for connection functions
        when(deviceProtocolPluggableClass.getProvidedConnectionFunctions()).thenReturn(Collections.emptyList());
        when(partialScheduledConnectionTask.getConnectionFunction()).thenReturn(Optional.empty());

        response = target("/devicetypes/11/deviceconfigurations/12/connectionmethods/14").request().get(String.class);
        jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(14);
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("partial scheduled");
        assertThat(jsonModel.<Boolean>get("$.isDefault")).isFalse();
        assertThat(jsonModel.<Integer>get("$.connectionFunctionInfo")).isNull();
    }

    @Test
    public void testUpdateConnectionMethodSetComPortPoolToNull() throws Exception {
        InboundConnectionMethodInfo info = new InboundConnectionMethodInfo();
        info.name = "name";
        info.comPortPool = null; // should be set to null
        info.comWindowStart = 6;
        info.comWindowEnd = 12;
        info.connectionTypePluggableClass = new ConnectionMethodInfo.ConnectionTypePluggableClassInfo();
        info.connectionTypePluggableClass.id = 13L;
        info.connectionTypePluggableClass.name = "pluggableClass";
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(12L, OK_VERSION);
        info.protocolDialectConfigurationProperties =  new ConnectionMethodInfo.ProtocolDialectConfigurationPropertiesInfo();
        info.protocolDialectConfigurationProperties.id = 1234L;
        info.protocolDialectConfigurationProperties.name = "Dialectje";
        Response response = target("/devicetypes/11/deviceconfigurations/12/connectionmethods/13").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ArgumentCaptor<InboundComPortPool> comPortPoolArgumentCaptor = ArgumentCaptor.forClass(InboundComPortPool.class);
        verify(partialInboundConnectionTask).setComportPool(comPortPoolArgumentCaptor.capture());
        assertThat(comPortPoolArgumentCaptor.getValue()).isNull();
    }

    @Test
    public void testUpdateConnectionMethodSetConnectionFunction() throws Exception {
        InboundConnectionMethodInfo info = new InboundConnectionMethodInfo();
        info.name = "name";
        info.comPortPool = null;
        info.comWindowStart = 6;
        info.comWindowEnd = 12;
        info.connectionTypePluggableClass = new ConnectionMethodInfo.ConnectionTypePluggableClassInfo();
        info.connectionTypePluggableClass.id = 13L;
        info.connectionTypePluggableClass.name = "pluggableClass";
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(12L, OK_VERSION);
        info.protocolDialectConfigurationProperties =  new ConnectionMethodInfo.ProtocolDialectConfigurationPropertiesInfo();
        info.protocolDialectConfigurationProperties.id = 1234L;
        info.protocolDialectConfigurationProperties.name = "Dialectje";
        info.connectionFunctionInfo = new ConnectionFunctionInfo(connectionFunction_1);

        Response response = target("/devicetypes/11/deviceconfigurations/12/connectionmethods/13").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ArgumentCaptor<ConnectionFunction> connectionFunctionArgumentCaptor = ArgumentCaptor.forClass(ConnectionFunction.class);
        verify(partialInboundConnectionTask).setConnectionFunction(connectionFunctionArgumentCaptor.capture());
        assertThat(connectionFunctionArgumentCaptor.getValue()).isEqualTo(connectionFunction_1);

        info.connectionFunctionInfo = null;

        response = target("/devicetypes/11/deviceconfigurations/12/connectionmethods/13").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        connectionFunctionArgumentCaptor = ArgumentCaptor.forClass(ConnectionFunction.class);
        verify(partialInboundConnectionTask, times(2)).setConnectionFunction(connectionFunctionArgumentCaptor.capture());
        assertThat(connectionFunctionArgumentCaptor.getValue()).isNull();
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
        info.connectionTypePluggableClass = new ConnectionMethodInfo.ConnectionTypePluggableClassInfo();
        info.connectionTypePluggableClass.id = 13L;
        info.connectionTypePluggableClass.name = "pluggableClass";
        info.protocolDialectConfigurationProperties = new ConnectionMethodInfo.ProtocolDialectConfigurationPropertiesInfo();
        info.protocolDialectConfigurationProperties.id = 1234L;
        info.protocolDialectConfigurationProperties.name = "Dialectje";

        info.properties = new ArrayList<>();
        PropertyInfo propertyInfo = new PropertyInfo();
        propertyInfo.key = "connectionTimeOut";
        propertyInfo.name = "connectionTimeOut";
        propertyInfo.required = false;
        TimeDurationInfo timeDurationInfo = new TimeDurationInfo();
        timeDurationInfo.timeUnit = "0"; // INVALID
        propertyInfo.propertyValueInfo = new PropertyValueInfo<>(timeDurationInfo, null, null, true);
        propertyInfo.propertyTypeInfo = new PropertyTypeInfo();
        propertyInfo.propertyTypeInfo.simplePropertyType = com.elster.jupiter.properties.rest.SimplePropertyType.DURATION;
        info.properties.add(propertyInfo);
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(12L, OK_VERSION);
        Response response = target("/devicetypes/11/deviceconfigurations/12/connectionmethods/13").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
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
        info.connectionTypePluggableClass = new ConnectionMethodInfo.ConnectionTypePluggableClassInfo();
        info.connectionTypePluggableClass.id = 14L;
        info.connectionTypePluggableClass.name = "pluggableClass";
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(12L, OK_VERSION);
        info.protocolDialectConfigurationProperties = new ConnectionMethodInfo.ProtocolDialectConfigurationPropertiesInfo();
        info.protocolDialectConfigurationProperties.id = 1234L;
        info.protocolDialectConfigurationProperties.name = "Dialectje";
        Response response = target("/devicetypes/11/deviceconfigurations/12/connectionmethods/14").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ArgumentCaptor<OutboundComPortPool> comPortPoolArgumentCaptor = ArgumentCaptor.forClass(OutboundComPortPool.class);
        verify(partialScheduledConnectionTask).setComportPool(comPortPoolArgumentCaptor.capture());
        assertThat(comPortPoolArgumentCaptor.getValue()).isNull();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        deviceType = mockDeviceType("device", 11);
        deviceConfiguration = mockDeviceConfiguration("config", 12);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        partialInboundConnectionTask = mockPartialInboundConnectionTask(13L, "partial inbound");
        when(partialInboundConnectionTask.getConfiguration()).thenReturn(deviceConfiguration);
        partialScheduledConnectionTask = mockPartialScheduledConnectionTask(14L, "partial scheduled");
        when(partialScheduledConnectionTask.getConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.asList(partialInboundConnectionTask, partialScheduledConnectionTask));
    }

    protected DeviceConfiguration mockDeviceConfiguration(String name, long id) {
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getName()).thenReturn(name);
        when(deviceConfiguration.getId()).thenReturn(id);
        when(deviceConfiguration.getVersion()).thenReturn(1L);
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
        when(propertySpec.getValueFactory()).thenReturn(new TemporalAmountValueFactory());
        when(pluggableClass.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(protocolPluggableService.findConnectionTypePluggableClass(id)).thenReturn(Optional.of(pluggableClass));
        when(deviceConfigurationService.findPartialConnectionTask(id)).thenReturn(Optional.of(partialConnectionTask));
        when(protocolPluggableService.findConnectionTypePluggableClassByNameTranslationKey("pluggableClass")).thenReturn(Optional.of(pluggableClass));
        when(deviceConfigurationService.findAndLockPartialConnectionTaskByIdAndVersion(id, OK_VERSION)).thenReturn(Optional.of(partialConnectionTask));
        when(deviceConfigurationService.findAndLockPartialConnectionTaskByIdAndVersion(id, BAD_VERSION)).thenReturn(Optional.empty());
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(comPortPool.getName()).thenReturn(COM_PORT_POOL_NAME);
        when(partialConnectionTask.getComPortPool()).thenReturn(comPortPool);
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggableClass);
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mockProtocolDialectConfigurationProperties();
        when(partialConnectionTask.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        when(partialConnectionTask.getConnectionFunction()).thenReturn(Optional.empty());
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
        when(protocolPluggableService.findConnectionTypePluggableClass(id)).thenReturn(Optional.of(pluggableClass));
        when(protocolPluggableService.findConnectionTypePluggableClassByNameTranslationKey("pluggableClass")).thenReturn(Optional.of(pluggableClass));
        when(deviceConfigurationService.findPartialConnectionTask(id)).thenReturn(Optional.of(partialConnectionTask));
        when(deviceConfigurationService.findAndLockPartialConnectionTaskByIdAndVersion(id, OK_VERSION)).thenReturn(Optional.of(partialConnectionTask));
        when(deviceConfigurationService.findAndLockPartialConnectionTaskByIdAndVersion(id, BAD_VERSION)).thenReturn(Optional.empty());
        OutboundComPortPool comPortPool = mock(OutboundComPortPool.class);
        when(comPortPool.getName()).thenReturn(COM_PORT_POOL_NAME);
        when(partialConnectionTask.getComPortPool()).thenReturn(comPortPool);
        when(partialConnectionTask.getPluggableClass()).thenReturn(pluggableClass);
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mockProtocolDialectConfigurationProperties();
        when(partialConnectionTask.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        when(partialConnectionTask.getConnectionFunction()).thenReturn(Optional.empty());
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
        when(deviceProtocolPluggableClass.getProvidedConnectionFunctions()).thenReturn(Arrays.asList(connectionFunction_1, connectionFunction_2));
        when(deviceProtocolPluggableClass.getConsumableConnectionFunctions()).thenReturn(Collections.singletonList(connectionFunction_3));
        when(deviceConfigurationService.findDeviceType(id)).thenReturn(Optional.of(deviceType));
        when(deviceConfigurationService.findAndLockDeviceType(id, OK_VERSION)).thenReturn(Optional.of(deviceType));
        when(deviceConfigurationService.findAndLockDeviceType(id, BAD_VERSION)).thenReturn(Optional.empty());
        return deviceType;
    }

    protected ProtocolDialectConfigurationProperties mockProtocolDialectConfigurationProperties() {
        PropertySpec specYes = mock(PropertySpec.class);
        when(specYes.getValueFactory()).thenReturn(new StringFactory());
        when(specYes.getName()).thenReturn("yes");
        when(specYes.getDisplayName()).thenReturn("YES");

        PropertySpec specNo = mock(PropertySpec.class);
        when(specYes.getValueFactory()).thenReturn(new StringFactory());
        when(specYes.getName()).thenReturn("no");
        when(specYes.getDisplayName()).thenReturn("NO");

        PropertySpec specLanguage = mock(PropertySpec.class);
        when(specYes.getValueFactory()).thenReturn(new StringFactory());
        when(specYes.getName()).thenReturn("language");
        when(specYes.getDisplayName()).thenReturn("Language");

        TypedProperties flamish = TypedProperties.empty();
        flamish.setProperty("yes", "joa't");

        PropertyTypeInfo typeInfo = new PropertyTypeInfo();
        typeInfo.simplePropertyType = com.elster.jupiter.properties.rest.SimplePropertyType.TEXT;

        DeviceProtocolDialect dialect = mockDeviceProtocolDialect();

        ProtocolDialectConfigurationProperties properties = mock(ProtocolDialectConfigurationProperties.class);
        when(properties.getId()).thenReturn(11111111L);
        when(properties.getDeviceProtocolDialect()).thenReturn(dialect);
        when(properties.getTypedProperties()).thenReturn(flamish);
        when(properties.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(properties.getTypedProperties()).thenReturn(flamish);
        when(properties.getPropertySpecs()).thenReturn(Arrays.asList(specYes, specNo, specLanguage));

        return properties;
    }

    protected DeviceProtocolDialect mockDeviceProtocolDialect() {
        DeviceProtocolDialect dialect = mock(DeviceProtocolDialect.class);
        when(dialect.getDeviceProtocolDialectName()).thenReturn("Mocked Protocol Dialect");
        when(dialect.getDeviceProtocolDialectDisplayName()).thenReturn("DisplayName of the mocked Protocol Dialect");
        return dialect;
    }

    private ConnectionFunction mockConnectionFunction(int id, String name, String displayName) {
        return new ConnectionFunction() {
            @Override
            public long getId() {
                return id;
            }

            @Override
            public String getConnectionFunctionName() {
                return name;
            }

            @Override
            public String getConnectionFunctionDisplayName() {
                return displayName;
            }
        };
    }
}
