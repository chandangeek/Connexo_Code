/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceProtocolConfigurationProperties;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceProtocolPropertiesResourceTest extends DeviceDataRestApplicationJerseyTest {

    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private DeviceProtocolConfigurationProperties properties;
    private TypedProperties typedProperties;
    @Mock
    private DeviceType deviceType;
    @Mock
    private Device device;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME);
        when(propertySpec.isRequired()).thenReturn(false);
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        when(deviceType.getId()).thenReturn(11L);
        when(deviceType.getName()).thenReturn("device");
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getPropertySpecs()).thenReturn(Arrays.asList(propertySpec));
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocolPluggableClass.getId()).thenReturn(17L);
        when(deviceProtocolPluggableClass.getName()).thenReturn("some protocol");
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolPluggableClass.getId())).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(protocolPluggableService.findAndLockDeviceProtocolPluggableClassByIdAndVersion(deviceProtocolPluggableClass.getId(), 1L)).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(properties.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        typedProperties = TypedProperties.empty();
        typedProperties.setProperty(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, "0x7");
        when(properties.getTypedProperties()).thenReturn(typedProperties);
        when(properties.getPropertySpecs()).thenReturn(Arrays.asList(propertySpec));
        when(deviceConfiguration.getDeviceProtocolProperties()).thenReturn(properties);
        List<DeviceConfiguration> deviceConfigurations = new ArrayList<>(1);
        deviceConfigurations.add(deviceConfiguration);
        when(deviceConfigurationService.findDeviceType(11)).thenReturn(Optional.of(deviceType));
        when(deviceType.getConfigurations()).thenReturn(deviceConfigurations);
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceProtocolProperties()).thenReturn(typedProperties);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getVersion()).thenReturn(1L);
        when(deviceService.findDeviceByName("ZABF010000080004")).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByNameAndVersion("ZABF010000080004", 1L)).thenReturn(Optional.of(device));

    }

    @Test
    public void testGetDeviceProtocolProperties() {
        PropertyInfo propertyInfo = new PropertyInfo(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, new PropertyValueInfo<>("0x7", null), new PropertyTypeInfo(), false);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        String response = target("/devices/ZABF010000080004/protocols/1").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(17);
        assertThat(jsonModel.<String>get("$.properties[0].key")).isEqualTo(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME);
        assertThat(jsonModel.<String>get("$.properties[0].propertyValueInfo.value")).isEqualTo("0x7");
    }

    @Test
    public void testGetDeviceProtocolPropertiesNonExistingDevice() {
        when(this.deviceService.findDeviceByName(anyString())).thenReturn(Optional.<Device>empty());
        Response response = target("/devices/FAKE/protocols/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testSetDeviceProtocolProperty() {
        PropertyInfo propertyInfo = new PropertyInfo();
        propertyInfo.key =LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME;
        propertyInfo.name=LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME;
        propertyInfo.propertyValueInfo = new PropertyValueInfo<>("0x99", null, null, true);
        propertyInfo.propertyTypeInfo = new PropertyTypeInfo();
        propertyInfo.propertyTypeInfo.simplePropertyType= com.elster.jupiter.properties.rest.SimplePropertyType.TEXT;

        DeviceProtocolInfo protocolInfo = new DeviceProtocolInfo();
        protocolInfo.version = 1L;
        protocolInfo.properties = Arrays.asList(propertyInfo);
        protocolInfo.parent = new VersionInfo<>("ZABF010000080004", 1L);
        when(propertyValueInfoService.findPropertyValue(any(), any())).thenReturn(propertyInfo.getPropertyValueInfo().getValue());
        Response response = target("devices/ZABF010000080004/protocols/17").request().put(Entity.json(protocolInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(device).setProtocolProperty(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, "0x99");
    }

}
