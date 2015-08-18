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
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        when(deviceService.findByUniqueMrid("AX1")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mockAuthenticationDeviceAccessLevel(1, "DlmsSecuritySupportPerClient.authenticationlevel.1");
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = getEncryptionDeviceAccessLevel(2, "Mtu155SecuritySupport.encryptionlevel.2");
        SecurityPropertySet sps1 = mockSecurityPropertySet(1001L, "Set 1", CAN_VIEW, CAN_EDIT, authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
        SecurityProperty securityProperty1 = mockSecurityPropertyWithSpec(sps1, "password", "secret", new StringFactory(), authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
        when(device.getSecurityProperties(sps1)).thenReturn(Arrays.asList(securityProperty1));
        when(device.securityPropertiesAreValid(sps1)).thenReturn(true);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1));

        String response = target("/devices/AX1/securityproperties").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].name")).isEqualTo("Set 1");
        assertThat(jsonModel.<Integer>get("$.securityPropertySets[0].authenticationLevel.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].authenticationLevel.name")).isEqualTo(MessageSeeds.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_1.getDefaultFormat());
        assertThat(jsonModel.<Integer>get("$.securityPropertySets[0].encryptionLevel.id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].encryptionLevel.name")).isEqualTo(MessageSeeds.MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_2.getDefaultFormat());
        assertThat(jsonModel.<List>get("$.securityPropertySets[0].properties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].key")).isEqualTo("password");
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyValueInfo.inheritedValue")).isNull();
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyValueInfo.defaultValue")).isNull();
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyValueInfo.value")).isEqualTo("secret");
        assertThat(jsonModel.<Boolean>get("$.securityPropertySets[0].properties[0].required")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].status.id")).isEqualTo("COMPLETE");
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].status.name")).isEqualTo("Complete");
    }

    @Test
    public void testGetSecurityPropertySetByIdWithViewAndEditPrivilege() throws Exception {
        long deviceConfigId = 6161L;
        long sps1Id = 1234L;
        String devicemRID = "AX1";

        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid(devicemRID)).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getId()).thenReturn(deviceConfigId);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mockAuthenticationDeviceAccessLevel(1, "DlmsSecuritySupportPerClient.authenticationlevel.1");
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = getEncryptionDeviceAccessLevel(2, "Mtu155SecuritySupport.encryptionlevel.2");
        SecurityPropertySet sps1 = mockSecurityPropertySet(sps1Id, "Set 1", CAN_VIEW, CAN_EDIT, authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
        when(deviceConfigurationService.findSecurityPropertySet(sps1Id)).thenReturn(Optional.of(sps1));
        SecurityProperty securityProperty1 = mockSecurityPropertyWithSpec(sps1, "password", "secret", new StringFactory(), authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
        when(device.getSecurityProperties(sps1)).thenReturn(Arrays.asList(securityProperty1));
        when(device.securityPropertiesAreValid(sps1)).thenReturn(true);
        when(sps1.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1));

        String response = target("/devices/"+devicemRID+"/securityproperties/"+sps1Id).request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);

        System.out.println("testje");
        System.out.println(jsonModel.toJson(true));

        assertThat(jsonModel.<String>get("$.name")).isEqualTo("Set 1");
        assertThat(jsonModel.<Integer>get("$.authenticationLevel.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.authenticationLevel.name")).isEqualTo(MessageSeeds.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_1.getDefaultFormat());
        assertThat(jsonModel.<Integer>get("$.encryptionLevel.id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.encryptionLevel.name")).isEqualTo(MessageSeeds.MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_2.getDefaultFormat());
        assertThat(jsonModel.<List>get("$.properties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.properties[0].key")).isEqualTo("password");
        assertThat(jsonModel.<String>get("$.properties[0].propertyValueInfo.inheritedValue")).isNull();
        assertThat(jsonModel.<String>get("$.properties[0].propertyValueInfo.defaultValue")).isNull();
        assertThat(jsonModel.<String>get("$.properties[0].propertyValueInfo.value")).isEqualTo("secret");
        assertThat(jsonModel.<Boolean>get("$.properties[0].required")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
        assertThat(jsonModel.<String>get("$.status.id")).isEqualTo("COMPLETE");
        assertThat(jsonModel.<String>get("$.status.name")).isEqualTo("Complete");
    }

    @Test
    public void testGetSecurityPropertySetByIdDeviceConfigurationMismatch() throws Exception {
        long deviceConfigId = 6161L;
        long sps1Id = 1234L;
        String devicemRID = "AX1";

        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid(devicemRID)).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getId()).thenReturn(deviceConfigId);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mockAuthenticationDeviceAccessLevel(1, "DlmsSecuritySupportPerClient.authenticationlevel.1");
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = getEncryptionDeviceAccessLevel(2, "Mtu155SecuritySupport.encryptionlevel.2");
        SecurityPropertySet sps1 = mockSecurityPropertySet(sps1Id, "Set 1", CAN_VIEW, CAN_EDIT, authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
        when(deviceConfigurationService.findSecurityPropertySet(sps1Id)).thenReturn(Optional.of(sps1));
        SecurityProperty securityProperty1 = mockSecurityPropertyWithSpec(sps1, "password", "secret", new StringFactory(), authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
        when(device.getSecurityProperties(sps1)).thenReturn(Arrays.asList(securityProperty1));
        DeviceConfiguration otherDeviceConfiguration = mock(DeviceConfiguration.class);
        when(otherDeviceConfiguration.getId()).thenReturn(deviceConfigId+1);
        when(sps1.getDeviceConfiguration()).thenReturn(otherDeviceConfiguration);

        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1));
        Response response = target("/devices/"+devicemRID+"/securityproperties/"+sps1Id).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetUnknownSecurityPropertySetById() throws Exception {
        long sps1Id = 1234L;
        String devicemRID = "AX1";

        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid(devicemRID)).thenReturn(Optional.of(device));
        when(deviceConfigurationService.findSecurityPropertySet(sps1Id)).thenReturn(Optional.empty());

        Response response = target("/devices/"+devicemRID+"/securityproperties/"+sps1Id).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testStatusIncompleteForMissingRequiredProperty() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("AX1")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mockAuthenticationDeviceAccessLevel(1, "DlmsSecuritySupportPerClient.authenticationlevel.1");
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = getEncryptionDeviceAccessLevel(2, "Mtu155SecuritySupport.encryptionlevel.2");
        SecurityPropertySet sps1 = mockSecurityPropertySet(1001L, "Set 1", CAN_VIEW, CAN_EDIT, authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
        SecurityProperty securityProperty1 = mockSecurityPropertyWithSpec(sps1, "field1", null, new StringFactory(), authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
        SecurityProperty securityProperty2 = mockSecurityPropertyWithSpec(sps1, "field2", "blabla", new StringFactory(), authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
        SecurityProperty securityProperty3 = mockSecurityPropertyWithSpec(sps1, "field3", "blabla", new StringFactory(), authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
        when(device.getSecurityProperties(sps1)).thenReturn(Arrays.asList(securityProperty1, securityProperty2, securityProperty3));
        when(device.securityPropertiesAreValid(sps1)).thenReturn(false);

        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1));
        String response = target("/devices/AX1/securityproperties").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].status.id")).isEqualTo(MessageSeeds.INCOMPLETE.name());
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].status.name")).isEqualTo(MessageSeeds.INCOMPLETE.getDefaultFormat());
    }

    @Test
    public void testPasswordPropertyWithEditWithoutViewPrivilege() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("AX1")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mockAuthenticationDeviceAccessLevel(1, "DlmsSecuritySupportPerClient.authenticationlevel.1");
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = getEncryptionDeviceAccessLevel(2, "Mtu155SecuritySupport.encryptionlevel.2");
        SecurityPropertySet sps1 = mockSecurityPropertySet(1001L, "Set 1", CAN_NOT_VIEW, CAN_EDIT, authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
        SecurityProperty securityProperty1 = mockSecurityPropertyWithSpec(sps1, "password", "secret", new StringFactory(), authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1));
        when(device.getSecurityProperties(sps1)).thenReturn(Arrays.asList(securityProperty1));
        when(device.securityPropertiesAreValid(sps1)).thenReturn(true);

        String response = target("/devices/AX1/securityproperties").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].name")).isEqualTo("Set 1");
        assertThat(jsonModel.<Integer>get("$.securityPropertySets[0].authenticationLevel.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].authenticationLevel.name")).isEqualTo(MessageSeeds.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_1.getDefaultFormat());
        assertThat(jsonModel.<Integer>get("$.securityPropertySets[0].encryptionLevel.id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].encryptionLevel.name")).isEqualTo(MessageSeeds.MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_2.getDefaultFormat());
        assertThat(jsonModel.<List>get("$.securityPropertySets[0].properties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].key")).isEqualTo("password");
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyValueInfo.inheritedValue")).isNull();
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyValueInfo.defaultValue")).isNull();
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyValueInfo.value")).isNull();
        assertThat(jsonModel.<Boolean>get("$.securityPropertySets[0].properties[0].required")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].status.id")).isEqualTo(MessageSeeds.COMPLETE.name());
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].status.name")).isEqualTo(MessageSeeds.COMPLETE.getDefaultFormat());
    }

    @Test
    public void testPasswordPropertyWithoutEditWithoutViewPrivilege() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("AX1")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mockAuthenticationDeviceAccessLevel(1, "DlmsSecuritySupportPerClient.authenticationlevel.1");
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = getEncryptionDeviceAccessLevel(2, "Mtu155SecuritySupport.encryptionlevel.2");
        SecurityPropertySet sps1 = mockSecurityPropertySet(1001L, "Set 1", CAN_NOT_VIEW, CAN_NOT_EDIT, authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
        SecurityProperty securityProperty1 = mockSecurityPropertyWithSpec(sps1, "password", "secret", new StringFactory(), authenticationDeviceAccessLevel, encryptionDeviceAccessLevel);
        when(device.getSecurityProperties(sps1)).thenReturn(Arrays.asList(securityProperty1));
        when(device.securityPropertiesAreValid(sps1)).thenReturn(true);

        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1));
        String response = target("/devices/AX1/securityproperties").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].name")).isEqualTo("Set 1");
        assertThat(jsonModel.<Integer>get("$.securityPropertySets[0].authenticationLevel.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].authenticationLevel.name")).isEqualTo(MessageSeeds.DLMSSECURITYSUPPORTPERCLIENT_AUTHENTICATIONLEVEL_1.getDefaultFormat());
        assertThat(jsonModel.<Integer>get("$.securityPropertySets[0].encryptionLevel.id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].encryptionLevel.name")).isEqualTo(MessageSeeds.MTU155SECURITYSUPPORT_ENCRYPTIONLEVEL_2.getDefaultFormat());
        assertThat(jsonModel.<List>get("$.securityPropertySets[0].properties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].key")).isEqualTo("password");
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyValueInfo.inheritedValue")).isNull();
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyValueInfo.defaultValue")).isNull();
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyValueInfo.value")).isNull();
        assertThat(jsonModel.<Boolean>get("$.securityPropertySets[0].properties[0].required")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyTypeInfo.simplePropertyType")).isNull();
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].status.id")).isEqualTo(MessageSeeds.COMPLETE.name());
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].status.name")).isEqualTo(MessageSeeds.COMPLETE.getDefaultFormat());
    }

    private <T>  SecurityProperty mockSecurityPropertyWithSpec(SecurityPropertySet securityPropertySet, String name, Object value, ValueFactory<T> valueFactory, AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel, EncryptionDeviceAccessLevel encryptionDeviceAccessLevel) {
        SecurityProperty securityProperty = mock(SecurityProperty.class);
        when(securityProperty.getName()).thenReturn(name);
        when(securityProperty.getValue()).thenReturn(value);
        when(securityProperty.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationDeviceAccessLevel);
        when(securityProperty.getEncryptionDeviceAccessLevel()).thenReturn(encryptionDeviceAccessLevel);
        PropertySpec propertySpec1 = mock(PropertySpec.class);
        when(propertySpec1.getName()).thenReturn(name);
        when(propertySpec1.isRequired()).thenReturn(true);
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

    enum CompleteState {
        COMPLETE(true),
        INCOMPLETE(false);
        private final boolean bool;

        CompleteState(boolean b) {
            this.bool=b;
        }
    }
}
