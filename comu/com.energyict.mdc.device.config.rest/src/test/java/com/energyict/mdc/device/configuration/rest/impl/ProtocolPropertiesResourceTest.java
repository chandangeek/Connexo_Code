package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceProtocolConfigurationProperties;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.pluggable.rest.impl.properties.SimplePropertyType;
import com.jayway.jsonpath.JsonModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProtocolPropertiesResourceTest extends BaseLoadProfileTest {

    @Test
    public void testGetDeviceProtocolProperties() {
        DeviceProtocolConfigurationProperties properties = mock(DeviceProtocolConfigurationProperties.class);
        mockDeviceConfiguration(properties);
        String response = target("/devicetypes/11/deviceconfigurations/12/protocolproperties").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.properties[0].key")).isEqualTo("callHomeId");
        assertThat(jsonModel.<String>get("$.properties[0].propertyValueInfo.value")).isEqualTo("0x7");
    }

    @Test
    public void testSetDeviceProtocolProperty() {
        DeviceProtocolConfigurationProperties properties = mock(DeviceProtocolConfigurationProperties.class);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(properties);
        PropertyInfo propertyInfo = new PropertyInfo();
        propertyInfo.key="callHomeId";
        propertyInfo.propertyValueInfo = new PropertyValueInfo<>("0x99", null, null, true);
        propertyInfo.propertyTypeInfo = new PropertyTypeInfo();
        propertyInfo.propertyTypeInfo.simplePropertyType= SimplePropertyType.TEXT;


        Response response = target("/devicetypes/11/deviceconfigurations/12/protocolproperties").request().put(Entity.json(Arrays.asList(propertyInfo)));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(properties).setProperty("callHomeId", "0x99");
        verify(deviceConfiguration).save();
    }

    private DeviceConfiguration mockDeviceConfiguration(DeviceProtocolConfigurationProperties properties) {
        BasicPropertySpec<String> propertySpec = new BasicPropertySpec<>("callHomeId", new StringFactory());
        DeviceType deviceType = mockDeviceType("device", 11, Arrays.asList(propertySpec));
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration("config", 12);
        when(properties.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty("callHomeId", "0x7");
        when(properties.getTypedProperties()).thenReturn(typedProperties);
        when(properties.getPropertySpecs()).thenReturn(Arrays.asList(propertySpec));
        when(deviceConfiguration.getDeviceProtocolProperties()).thenReturn(properties);
        List<DeviceConfiguration> deviceConfigurations = new ArrayList<>(1);
        deviceConfigurations.add(deviceConfiguration);
        when(deviceConfigurationService.findDeviceType(11)).thenReturn(Optional.of(deviceType));
        when(deviceType.getConfigurations()).thenReturn(deviceConfigurations);
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        return deviceConfiguration;
    }
}
