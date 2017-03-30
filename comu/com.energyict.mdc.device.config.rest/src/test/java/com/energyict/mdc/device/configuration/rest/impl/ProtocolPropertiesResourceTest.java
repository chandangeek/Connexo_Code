/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceProtocolConfigurationProperties;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.configuration.rest.ProtocolInfo;
import com.energyict.mdc.protocol.api.DeviceProtocolProperty;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProtocolPropertiesResourceTest extends BaseLoadProfileTest {

    private final long deviceTypeId = 11L;
    private final int deviceConfigurationId = 12;

    @Test
    public void testGetDeviceProtocolProperties() {
        DeviceProtocolConfigurationProperties properties = mock(DeviceProtocolConfigurationProperties.class);
        mockDeviceConfiguration(properties);
        PropertyInfo propertyInfo = new PropertyInfo();
        propertyInfo.key = DeviceProtocolProperty.CALL_HOME_ID.javaFieldName();
        propertyInfo.propertyValueInfo = new PropertyValueInfo<>("0x7", null, null, true);
        propertyInfo.propertyTypeInfo = new PropertyTypeInfo();
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        String response = target("/devicetypes/11/deviceconfigurations/12/protocols/1").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(7);
        assertThat(jsonModel.<String>get("$.properties[0].key")).isEqualTo(DeviceProtocolProperty.CALL_HOME_ID.javaFieldName());
        assertThat(jsonModel.<String>get("$.properties[0].propertyValueInfo.value")).isEqualTo("0x7");
    }

    @Test
    public void testSetDeviceProtocolProperty() {
        DeviceProtocolConfigurationProperties properties = mock(DeviceProtocolConfigurationProperties.class);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(properties);
        PropertyInfo propertyInfo = new PropertyInfo();
        propertyInfo.key =DeviceProtocolProperty.CALL_HOME_ID.javaFieldName();
        propertyInfo.name=DeviceProtocolProperty.CALL_HOME_ID.javaFieldName();
        propertyInfo.propertyValueInfo = new PropertyValueInfo<>("0x99", null, null, true);
        propertyInfo.propertyTypeInfo = new PropertyTypeInfo();
        propertyInfo.propertyTypeInfo.simplePropertyType= SimplePropertyType.TEXT;

        ProtocolInfo protocolInfo = new ProtocolInfo();
        protocolInfo.properties = Arrays.asList(propertyInfo);
        protocolInfo.deviceConfiguration = new DeviceConfigurationInfo();
        protocolInfo.deviceConfiguration.id = deviceConfigurationId;
        protocolInfo.deviceConfiguration.version = OK_VERSION;
        protocolInfo.deviceConfiguration.parent = new VersionInfo<>(deviceTypeId, 1L);
        when(propertyValueInfoService.findPropertyValue(any(), any())).thenReturn(propertyInfo.getPropertyValueInfo().getValue());
        Response response = target("/devicetypes/11/deviceconfigurations/12/protocols/7").request().put(Entity.json(protocolInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(properties).setProperty(DeviceProtocolProperty.CALL_HOME_ID.javaFieldName(), "0x99");
        verify(deviceConfiguration).save();
    }

    @Test
    public void testSetDeviceProtocolPropertyBadVersion() {
        DeviceProtocolConfigurationProperties properties = mock(DeviceProtocolConfigurationProperties.class);
        mockDeviceConfiguration(properties);

        ProtocolInfo protocolInfo = new ProtocolInfo();
        protocolInfo.deviceConfiguration = new DeviceConfigurationInfo();
        protocolInfo.deviceConfiguration.id = 12L;
        protocolInfo.deviceConfiguration.version = 1L;
        protocolInfo.deviceConfiguration.parent = new VersionInfo<>(deviceTypeId, 2L);

        Response response = target("/devicetypes/11/deviceconfigurations/12/protocols/7").request().put(Entity.json(protocolInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    private DeviceConfiguration mockDeviceConfiguration(DeviceProtocolConfigurationProperties properties) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(DeviceProtocolProperty.CALL_HOME_ID.javaFieldName());
        when(propertySpec.isRequired()).thenReturn(false);
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        DeviceType deviceType = mockDeviceType("device", 11, Arrays.asList(propertySpec));
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(deviceConfigurationId);
        when(properties.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(DeviceProtocolProperty.CALL_HOME_ID.javaFieldName(), "0x7");
        when(properties.getTypedProperties()).thenReturn(typedProperties);
        when(properties.getPropertySpecs()).thenReturn(Arrays.asList(propertySpec));
        when(deviceConfiguration.getDeviceProtocolProperties()).thenReturn(properties);
        List<DeviceConfiguration> deviceConfigurations = new ArrayList<>(1);
        deviceConfigurations.add(deviceConfiguration);
        when(deviceConfigurationService.findDeviceType(deviceTypeId)).thenReturn(Optional.of(deviceType));
        when(deviceType.getConfigurations()).thenReturn(deviceConfigurations);
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(deviceConfigurationService.findAndLockDeviceType(deviceTypeId, 1L)).thenReturn(Optional.of(deviceType));
        when(deviceConfigurationService.findAndLockDeviceType(deviceTypeId, 2L)).thenReturn(Optional.empty());
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(deviceConfigurationId, 1L)).thenReturn(Optional.of(deviceConfiguration));
        return deviceConfiguration;
    }

}