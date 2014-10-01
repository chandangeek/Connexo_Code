package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.jayway.jsonpath.JsonModel;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 9/30/14.
 */
public class SecurityPropertySetResourceTest extends DeviceDataRestApplicationJerseyTest {

    @Test
    public void testPasswordPropertyWithViewAndEditPrivilege() throws Exception {
        Device device = mock(Device.class);
        when(deviceDataService.findByUniqueMrid("AX1")).thenReturn(device);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        SecurityPropertySet sps1 = mock(SecurityPropertySet.class);
        when(sps1.getId()).thenReturn(1001L);
        when(sps1.getName()).thenReturn("Set 1");
        when(sps1.getUserActions()).thenReturn(EnumSet.of(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1));
        when(sps1.currentUserIsAllowedToViewDeviceProperties()).thenReturn(true);
        when(sps1.currentUserIsAllowedToEditDeviceProperties()).thenReturn(true);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        when(authenticationDeviceAccessLevel.getId()).thenReturn(1);
        when(authenticationDeviceAccessLevel.getTranslationKey()).thenReturn("DlmsSecuritySupportPerClient.authenticationlevel.1");
        when(sps1.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationDeviceAccessLevel);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionDeviceAccessLevel.getId()).thenReturn(2);
        when(encryptionDeviceAccessLevel.getTranslationKey()).thenReturn("Mtu155SecuritySupport.encryptionlevel.2");
        when(sps1.getEncryptionDeviceAccessLevel()).thenReturn(encryptionDeviceAccessLevel);
        PropertySpec propertySpec1 = mock(PropertySpec.class);
        when(propertySpec1.getName()).thenReturn("password");
        when(propertySpec1.isRequired()).thenReturn(true);
        ValueFactory valueFactory = new StringFactory();
        when(propertySpec1.getValueFactory()).thenReturn(valueFactory);
        Set set = new HashSet<>();
        set.add(propertySpec1);
        when(sps1.getPropertySpecs()).thenReturn(set);
        SecurityProperty securityProperty1 = mock(SecurityProperty.class);
        when(securityProperty1.getName()).thenReturn("password");
        when(securityProperty1.getEncryptionDeviceAccessLevel()).thenReturn(encryptionDeviceAccessLevel);
        when(securityProperty1.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationDeviceAccessLevel);
        when(securityProperty1.getValue()).thenReturn("secret");
        when(device.getSecurityProperties(sps1)).thenReturn(Arrays.asList(securityProperty1));

        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1));
        String response = target("/devices/AX1/securityproperties").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].name")).isEqualTo("Set 1");
        assertThat(jsonModel.<Integer>get("$.securityPropertySets[0].authenticationLevel.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].authenticationLevel.name")).isEqualTo("DlmsSecuritySupportPerClient.authenticationlevel.1");
        assertThat(jsonModel.<Integer>get("$.securityPropertySets[0].encryptionLevel.id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].encryptionLevel.name")).isEqualTo("Mtu155SecuritySupport.encryptionlevel.2");
        assertThat(jsonModel.<List>get("$.securityPropertySets[0].properties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].key")).isEqualTo("password");
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyValueInfo.inheritedValue")).isNull();
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyValueInfo.defaultValue")).isNull();
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyValueInfo.value")).isEqualTo("secret");
        assertThat(jsonModel.<Boolean>get("$.securityPropertySets[0].properties[0].required")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
    }

    @Test
    public void testPasswordPropertyWithEditWithoutViewPrivilege() throws Exception {
        Device device = mock(Device.class);
        when(deviceDataService.findByUniqueMrid("AX1")).thenReturn(device);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        SecurityPropertySet sps1 = mock(SecurityPropertySet.class);
        when(sps1.getId()).thenReturn(1001L);
        when(sps1.getName()).thenReturn("Set 1");
        when(sps1.getUserActions()).thenReturn(EnumSet.of(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1));
        when(sps1.currentUserIsAllowedToViewDeviceProperties()).thenReturn(false);
        when(sps1.currentUserIsAllowedToEditDeviceProperties()).thenReturn(true);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        when(authenticationDeviceAccessLevel.getId()).thenReturn(1);
        when(authenticationDeviceAccessLevel.getTranslationKey()).thenReturn("DlmsSecuritySupportPerClient.authenticationlevel.1");
        when(sps1.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationDeviceAccessLevel);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionDeviceAccessLevel.getId()).thenReturn(2);
        when(encryptionDeviceAccessLevel.getTranslationKey()).thenReturn("Mtu155SecuritySupport.encryptionlevel.2");
        when(sps1.getEncryptionDeviceAccessLevel()).thenReturn(encryptionDeviceAccessLevel);
        PropertySpec propertySpec1 = mock(PropertySpec.class);
        when(propertySpec1.getName()).thenReturn("password");
        when(propertySpec1.isRequired()).thenReturn(true);
        ValueFactory valueFactory = new StringFactory();
        when(propertySpec1.getValueFactory()).thenReturn(valueFactory);
        Set set = new HashSet<>();
        set.add(propertySpec1);
        when(sps1.getPropertySpecs()).thenReturn(set);
        SecurityProperty securityProperty1 = mock(SecurityProperty.class);
        when(securityProperty1.getName()).thenReturn("password");
        when(securityProperty1.getEncryptionDeviceAccessLevel()).thenReturn(encryptionDeviceAccessLevel);
        when(securityProperty1.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationDeviceAccessLevel);
        when(securityProperty1.getValue()).thenReturn("secret");
        when(device.getSecurityProperties(sps1)).thenReturn(Arrays.asList(securityProperty1));

        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1));
        String response = target("/devices/AX1/securityproperties").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].name")).isEqualTo("Set 1");
        assertThat(jsonModel.<Integer>get("$.securityPropertySets[0].authenticationLevel.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].authenticationLevel.name")).isEqualTo("DlmsSecuritySupportPerClient.authenticationlevel.1");
        assertThat(jsonModel.<Integer>get("$.securityPropertySets[0].encryptionLevel.id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].encryptionLevel.name")).isEqualTo("Mtu155SecuritySupport.encryptionlevel.2");
        assertThat(jsonModel.<List>get("$.securityPropertySets[0].properties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].key")).isEqualTo("password");
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyValueInfo.inheritedValue")).isNull();
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyValueInfo.defaultValue")).isNull();
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyValueInfo.value")).isNull();
        assertThat(jsonModel.<Boolean>get("$.securityPropertySets[0].properties[0].required")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
    }

    @Test
    public void testPasswordPropertyWithoutEditWithoutViewPrivilege() throws Exception {
        Device device = mock(Device.class);
        when(deviceDataService.findByUniqueMrid("AX1")).thenReturn(device);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        SecurityPropertySet sps1 = mock(SecurityPropertySet.class);
        when(sps1.getId()).thenReturn(1001L);
        when(sps1.getName()).thenReturn("Set 1");
        when(sps1.getUserActions()).thenReturn(EnumSet.of(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1));
        when(sps1.currentUserIsAllowedToViewDeviceProperties()).thenReturn(false);
        when(sps1.currentUserIsAllowedToEditDeviceProperties()).thenReturn(false);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        when(authenticationDeviceAccessLevel.getId()).thenReturn(1);
        when(authenticationDeviceAccessLevel.getTranslationKey()).thenReturn("DlmsSecuritySupportPerClient.authenticationlevel.1");
        when(sps1.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationDeviceAccessLevel);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionDeviceAccessLevel.getId()).thenReturn(2);
        when(encryptionDeviceAccessLevel.getTranslationKey()).thenReturn("Mtu155SecuritySupport.encryptionlevel.2");
        when(sps1.getEncryptionDeviceAccessLevel()).thenReturn(encryptionDeviceAccessLevel);
        PropertySpec propertySpec1 = mock(PropertySpec.class);
        when(propertySpec1.getName()).thenReturn("password");
        when(propertySpec1.isRequired()).thenReturn(true);
        ValueFactory valueFactory = new StringFactory();
        when(propertySpec1.getValueFactory()).thenReturn(valueFactory);
        Set set = new HashSet<>();
        set.add(propertySpec1);
        when(sps1.getPropertySpecs()).thenReturn(set);
        SecurityProperty securityProperty1 = mock(SecurityProperty.class);
        when(securityProperty1.getName()).thenReturn("password");
        when(securityProperty1.getEncryptionDeviceAccessLevel()).thenReturn(encryptionDeviceAccessLevel);
        when(securityProperty1.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationDeviceAccessLevel);
        when(securityProperty1.getValue()).thenReturn("secret");
        when(device.getSecurityProperties(sps1)).thenReturn(Arrays.asList(securityProperty1));

        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1));
        String response = target("/devices/AX1/securityproperties").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].name")).isEqualTo("Set 1");
        assertThat(jsonModel.<Integer>get("$.securityPropertySets[0].authenticationLevel.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].authenticationLevel.name")).isEqualTo("DlmsSecuritySupportPerClient.authenticationlevel.1");
        assertThat(jsonModel.<Integer>get("$.securityPropertySets[0].encryptionLevel.id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].encryptionLevel.name")).isEqualTo("Mtu155SecuritySupport.encryptionlevel.2");
        assertThat(jsonModel.<List>get("$.securityPropertySets[0].properties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].key")).isEqualTo("password");
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyValueInfo.inheritedValue")).isNull();
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyValueInfo.defaultValue")).isNull();
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyValueInfo.value")).isNull();
        assertThat(jsonModel.<Boolean>get("$.securityPropertySets[0].properties[0].required")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyTypeInfo.simplePropertyType")).isNull();
    }
}
