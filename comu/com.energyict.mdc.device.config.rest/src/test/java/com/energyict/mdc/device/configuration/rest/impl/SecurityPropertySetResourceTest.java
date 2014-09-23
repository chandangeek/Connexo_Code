package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.users.Group;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.jayway.jsonpath.JsonModel;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.mockito.Matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 9/12/14.
 */
public class SecurityPropertySetResourceTest extends DeviceConfigurationApplicationJerseyTest {

    @Test
    public void testGetSecurityPropertySet() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(deviceType);
        Group group1 = mockUserGroup(66L, "Z - user group 1");
        Group group2 = mockUserGroup(67L, "A - user group 2");
        Group group3 = mockUserGroup(68L, "O - user group 1");
        when(userService.getGroups()).thenReturn(Arrays.asList(group2, group1, group3));
        SecurityPropertySet sps1 = mockSecurityPropertySet(101L, "Primary", 1, "Auth1", 1001, "Encrypt1", EnumSet.of(DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION1, DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION2));
        SecurityPropertySet sps2 = mockSecurityPropertySet(102L, "Secondary", 2, "Auth2", 1002, "Encrypt2", EnumSet.of(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1));
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1, sps2));
        Map response = target("/devicetypes/123/deviceconfigurations/456/securityproperties").request().get(Map.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.data")).hasSize(2);
        assertThat(jsonModel.<Integer>get("$.data[0].id")).isEqualTo(101);
        assertThat(jsonModel.<String>get("$.data[0].name")).isEqualTo("Primary");
        assertThat(jsonModel.<Integer>get("$.data[0].authenticationLevelId")).isEqualTo(1);
        assertThat(jsonModel.<Integer>get("$.data[0].authenticationLevel.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.data[0].authenticationLevel.name")).isEqualTo("Auth1");
        assertThat(jsonModel.<Integer>get("$.data[0].encryptionLevelId")).isEqualTo(1001);
        assertThat(jsonModel.<Integer>get("$.data[0].encryptionLevel.id")).isEqualTo(1001);
        assertThat(jsonModel.<String>get("$.data[0].encryptionLevel.name")).isEqualTo("Encrypt1");
        assertThat(jsonModel.<List>get("$.data[0].executionLevels")).hasSize(2);
        assertThat(jsonModel.<String>get("$.data[0].executionLevels[0].id")).isEqualTo(DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION1.getPrivilege());
        assertThat(jsonModel.<String>get("$.data[0].executionLevels[0].name")).isEqualTo("Execute com task (level 1)");
        assertThat(jsonModel.<List>get("$.data[0].executionLevels[0].userRoles")).hasSize(3);
        assertThat(jsonModel.<Integer>get("$.data[0].executionLevels[0].userRoles[0].id")).isEqualTo(67);
        assertThat(jsonModel.<String>get("$.data[0].executionLevels[0].userRoles[0].name")).isEqualTo("A - user group 2");
        assertThat(jsonModel.<Integer>get("$.data[0].executionLevels[0].userRoles[1].id")).isEqualTo(68);
        assertThat(jsonModel.<String>get("$.data[0].executionLevels[0].userRoles[1].name")).isEqualTo("O - user group 1");
        assertThat(jsonModel.<Integer>get("$.data[0].executionLevels[0].userRoles[2].id")).isEqualTo(66);
        assertThat(jsonModel.<String>get("$.data[0].executionLevels[0].userRoles[2].name")).isEqualTo("Z - user group 1");
        assertThat(jsonModel.<String>get("$.data[0].executionLevels[1].id")).isEqualTo(DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION2.getPrivilege());
        assertThat(jsonModel.<String>get("$.data[0].executionLevels[1].name")).isEqualTo("Execute com task (level 2)");

        assertThat(jsonModel.<String>get("$.data[1].executionLevels[0].id")).isEqualTo(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1.getPrivilege());
        assertThat(jsonModel.<String>get("$.data[1].executionLevels[0].name")).isEqualTo("Edit device security properties (level 1)");
        assertThat(jsonModel.<String>get("$.data[1].executionLevels[1].id")).isEqualTo(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1.getPrivilege());
        assertThat(jsonModel.<String>get("$.data[1].executionLevels[1].name")).isEqualTo("View device security properties (level 1)");
    }

    @Test
    public void testGetSecurityPropertySetFilteredByPrivilege() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(deviceType);
        Group group1 = mockUserGroup(66L, "Z - user group 1", Arrays.asList(Privileges.EXECUTE_COM_TASK_1));
        Group group2 = mockUserGroup(67L, "A - user group 2", Arrays.asList(Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_4));
        Group group3 = mockUserGroup(68L, "O - user group 3", Collections.emptyList());
        when(userService.getGroups()).thenReturn(Arrays.asList(group2, group1, group3));
        SecurityPropertySet sps1 = mockSecurityPropertySet(101L, "Primary", 1, "Auth1", 1001, "Encrypt1", EnumSet.of(DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION1, DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION2));
        SecurityPropertySet sps2 = mockSecurityPropertySet(102L, "Secondary", 2, "Auth2", 1002, "Encrypt2", EnumSet.of(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES4, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1));
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1, sps2));
        String response = target("/devicetypes/123/deviceconfigurations/456/securityproperties").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);

        assertThat(jsonModel.<List>get("$.data")).hasSize(2);
        assertThat(jsonModel.<List>get("$.data[0].executionLevels")).hasSize(2);
        assertThat(jsonModel.<List>get("$.data[0].executionLevels[0].userRoles")).hasSize(1);
        assertThat(jsonModel.<Integer>get("$.data[0].executionLevels[0].userRoles[0].id")).isEqualTo(66);
        assertThat(jsonModel.<String>get("$.data[0].executionLevels[0].userRoles[0].name")).isEqualTo("Z - user group 1");
        assertThat(jsonModel.<List>get("$.data[0].executionLevels[1].userRoles")).isEmpty();

        assertThat(jsonModel.<List>get("$.data[1].executionLevels")).hasSize(2);
        assertThat(jsonModel.<List>get("$.data[1].executionLevels[0].userRoles")).hasSize(1);
        assertThat(jsonModel.<Integer>get("$.data[1].executionLevels[0].userRoles[0].id")).isEqualTo(67);
        assertThat(jsonModel.<String>get("$.data[1].executionLevels[0].userRoles[0].name")).isEqualTo("A - user group 2");
        assertThat(jsonModel.<List>get("$.data[1].executionLevels[1].userRoles")).isEmpty();
    }

    private Group mockUserGroup(long id, String name) {
        Group mock = mock(Group.class);
        when(mock.hasPrivilege(Matchers.<String>anyObject())).thenReturn(true);
        when(mock.getName()).thenReturn(name);
        when(mock.getId()).thenReturn(id);
        return mock;
    }

    private Group mockUserGroup(long id, String name, List<String> privileges) {
        Group mock = mock(Group.class);
        when(mock.hasPrivilege(Matchers.<String>anyObject())).then(invocationOnMock -> privileges.contains(invocationOnMock.getArguments()[0]));
        when(mock.getName()).thenReturn(name);
        when(mock.getId()).thenReturn(id);
        return mock;
    }

    private SecurityPropertySet mockSecurityPropertySet(Long id, String name, Integer authenticationAccessLevelId, String authenticationAccessLevelName, Integer encryptionAccessLevelId, String encryptionAccessLevelName, Set<DeviceSecurityUserAction> userAction) {
        SecurityPropertySet mock = mock(SecurityPropertySet.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        AuthenticationDeviceAccessLevel authenticationAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        when(authenticationAccessLevel.getId()).thenReturn(authenticationAccessLevelId);
        when(authenticationAccessLevel.getTranslationKey()).thenReturn(authenticationAccessLevelName);
        when(mock.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationAccessLevel);
        EncryptionDeviceAccessLevel encryptionAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionAccessLevel.getId()).thenReturn(encryptionAccessLevelId);
        when(encryptionAccessLevel.getTranslationKey()).thenReturn(encryptionAccessLevelName);
        when(mock.getEncryptionDeviceAccessLevel()).thenReturn(encryptionAccessLevel);
        when(mock.getUserActions()).thenReturn(userAction);
        return mock;
    }
}
