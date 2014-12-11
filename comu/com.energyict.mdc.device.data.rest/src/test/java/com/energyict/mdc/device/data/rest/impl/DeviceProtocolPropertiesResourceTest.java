package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceProtocolConfigurationProperties;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.configuration.rest.ProtocolInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.pluggable.rest.impl.properties.SimplePropertyType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.jayway.jsonpath.JsonModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
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
        BasicPropertySpec<String> propertySpec = new BasicPropertySpec<>("callHomeId", new StringFactory());
        when(deviceType.getId()).thenReturn(11L);
        when(deviceType.getName()).thenReturn("device");
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getPropertySpecs()).thenReturn(Arrays.asList(propertySpec));
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocolPluggableClass.getId()).thenReturn(17L);
        when(deviceProtocolPluggableClass.getName()).thenReturn("some protocol");
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        when(properties.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        typedProperties = TypedProperties.empty();
        typedProperties.setProperty("callHomeId", "0x7");
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
        when(deviceService.findByUniqueMrid("ZABF010000080004")).thenReturn(device);

    }

    @Test
    public void testGetDeviceProtocolProperties() {

        String response = target("/devices/ZABF010000080004/protocols/1").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(17);
        assertThat(jsonModel.<String>get("$.properties[0].key")).isEqualTo("callHomeId");
        assertThat(jsonModel.<String>get("$.properties[0].propertyValueInfo.value")).isEqualTo("0x7");
    }

    @Test
    public void testGetDeviceProtocolPropertiesNonExistingDevice() {
        Response response = target("/devices/FAKE/protocols/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testSetDeviceProtocolProperty() {
        PropertyInfo propertyInfo = new PropertyInfo();
        propertyInfo.key="callHomeId";
        propertyInfo.propertyValueInfo = new PropertyValueInfo<>("0x99", null, null, true);
        propertyInfo.propertyTypeInfo = new PropertyTypeInfo();
        propertyInfo.propertyTypeInfo.simplePropertyType= SimplePropertyType.TEXT;

        ProtocolInfo protocolInfo = new ProtocolInfo();
        protocolInfo.properties = Arrays.asList(propertyInfo);

        Response response = target("devices/ZABF010000080004/protocols/17").request().put(Entity.json(protocolInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(device).setProtocolProperty("callHomeId", "0x99");
        verify(device).save();
    }

}
