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
import java.util.List;
import java.util.Set;
import org.junit.Test;

import static com.energyict.mdc.device.data.rest.impl.SecurityPropertySetResourceTest.Editability.CAN_EDIT;
import static com.energyict.mdc.device.data.rest.impl.SecurityPropertySetResourceTest.Editability.CAN_NOT_EDIT;
import static com.energyict.mdc.device.data.rest.impl.SecurityPropertySetResourceTest.Visibility.CAN_NOT_VIEW;
import static com.energyict.mdc.device.data.rest.impl.SecurityPropertySetResourceTest.Visibility.CAN_VIEW;
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
        when(deviceService.findByUniqueMrid("AX1")).thenReturn(device);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mockAuthenticationDeviceAccessLevel(1, "DlmsSecuritySupportPerClient.authenticationlevel.1");
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = getEncryptionDeviceAccessLevel(2, "Mtu155SecuritySupport.encryptionlevel.2");
        SecurityPropertySet sps1 = mockSecurityPropertySet(1001L, "Set 1", CAN_VIEW, CAN_EDIT, authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
        SecurityProperty securityProperty1 = mockSecurityPropertyWithSpec(sps1, "password", "secret", true, new StringFactory(), authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
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
        when(deviceService.findByUniqueMrid("AX1")).thenReturn(device);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mockAuthenticationDeviceAccessLevel(1, "DlmsSecuritySupportPerClient.authenticationlevel.1");
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = getEncryptionDeviceAccessLevel(2, "Mtu155SecuritySupport.encryptionlevel.2");
        SecurityPropertySet sps1 = mockSecurityPropertySet(1001L, "Set 1", CAN_NOT_VIEW, CAN_EDIT, authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
        SecurityProperty securityProperty1 = mockSecurityPropertyWithSpec(sps1, "password", "secret", true, new StringFactory(), authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1));
        when(device.getSecurityProperties(sps1)).thenReturn(Arrays.asList(securityProperty1));
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
        when(deviceService.findByUniqueMrid("AX1")).thenReturn(device);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mockAuthenticationDeviceAccessLevel(1, "DlmsSecuritySupportPerClient.authenticationlevel.1");
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = getEncryptionDeviceAccessLevel(2, "Mtu155SecuritySupport.encryptionlevel.2");
        SecurityPropertySet sps1 = mockSecurityPropertySet(1001L, "Set 1", CAN_NOT_VIEW, CAN_NOT_EDIT, authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
        SecurityProperty securityProperty1 = mockSecurityPropertyWithSpec(sps1, "password", "secret", true, new StringFactory(), authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
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

    private <T>  SecurityProperty mockSecurityPropertyWithSpec(SecurityPropertySet securityPropertySet, String name, Object value, boolean required, ValueFactory<T> valueFactory, AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel, EncryptionDeviceAccessLevel encryptionDeviceAccessLevel) {
        SecurityProperty securityProperty = mock(SecurityProperty.class);
        when(securityProperty.getName()).thenReturn(name);
        when(securityProperty.getValue()).thenReturn(value);
        when(securityProperty.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationDeviceAccessLevel);
        when(securityProperty.getEncryptionDeviceAccessLevel()).thenReturn(encryptionDeviceAccessLevel);
        PropertySpec<T> propertySpec1 = mock(PropertySpec.class);
        when(propertySpec1.getName()).thenReturn(name);
        when(propertySpec1.isRequired()).thenReturn(required);
        when(propertySpec1.getValueFactory()).thenReturn(valueFactory);
        Set<PropertySpec> set = securityPropertySet.getPropertySpecs();
        set.add(propertySpec1);
        when(securityPropertySet.getPropertySpecs()).thenReturn(set);

        return securityProperty;
    }

    private SecurityPropertySet mockSecurityPropertySet(long id, String name, Visibility canView, Editability canEdit, AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel, EncryptionDeviceAccessLevel encryptionDeviceAccessLevel) {
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(id);
        when(securityPropertySet.getName()).thenReturn(name);
        when(securityPropertySet.getUserActions()).thenReturn(EnumSet.of(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1));
        when(securityPropertySet.currentUserIsAllowedToViewDeviceProperties()).thenReturn(canView.bool);
        when(securityPropertySet.currentUserIsAllowedToEditDeviceProperties()).thenReturn(canEdit.bool);
        when(securityPropertySet.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationDeviceAccessLevel);
        when(securityPropertySet.getEncryptionDeviceAccessLevel()).thenReturn(encryptionDeviceAccessLevel);

        return securityPropertySet;
    }

    private EncryptionDeviceAccessLevel getEncryptionDeviceAccessLevel(int encryptionLevelId, String encryptionName) {
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionDeviceAccessLevel.getId()).thenReturn(encryptionLevelId);
        when(encryptionDeviceAccessLevel.getTranslationKey()).thenReturn(encryptionName);
        return encryptionDeviceAccessLevel;
    }

    private AuthenticationDeviceAccessLevel mockAuthenticationDeviceAccessLevel(int authLevelId, String authName) {
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        when(authenticationDeviceAccessLevel.getId()).thenReturn(authLevelId);
        when(authenticationDeviceAccessLevel.getTranslationKey()).thenReturn(authName);
        return authenticationDeviceAccessLevel;
    }

    enum Visibility {
        CAN_VIEW(true),
        CAN_NOT_VIEW(false);
        private final boolean bool;

        Visibility(boolean b) {
            this.bool = b;
        }
    }

    enum Editability {
        CAN_EDIT(true),
        CAN_NOT_EDIT(false);
        private final boolean bool;

        Editability(boolean b) {
            this.bool = b;
        }
    }
}
