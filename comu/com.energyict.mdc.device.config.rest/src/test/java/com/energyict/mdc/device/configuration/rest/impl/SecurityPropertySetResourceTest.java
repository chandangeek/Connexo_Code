/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.users.Group;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.SecurityPropertySetBuilder;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.device.configuration.rest.SecurityLevelInfo;
import com.energyict.mdc.device.configuration.rest.KeyFunctionTypePrivilegeTranslationKeys;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.Test;
import org.mockito.Matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 9/12/14.
 */
public class SecurityPropertySetResourceTest extends DeviceConfigurationApplicationJerseyTest {

    public static final long OK_VERSION = 24L;
    public static final long BAD_VERSION = 17L;

    @Override
    protected void setupThesaurus() {
        super.setupThesaurus();
        Stream.of(KeyFunctionTypePrivilegeTranslationKeys.values()).forEach(this::mockTranslation);
    }

    private void mockTranslation(KeyFunctionTypePrivilegeTranslationKeys translationKey) {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(translationKey.getDefaultFormat());
        doReturn(messageFormat).when(thesaurus).getFormat(translationKey);
    }

    @Test
    public void addSecurityPropertySetAddsUserActions() throws Exception {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Arrays.asList(DeviceProtocolAuthenticationAccessLevels.values()));
        when(deviceProtocol.getEncryptionAccessLevels()).thenReturn(Arrays.asList(DeviceProtocolEncryptionAccessLevels.values()));

        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);

        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(Optional.of(deviceType));
        when(deviceConfigurationService.findAndLockDeviceType(123L, OK_VERSION)).thenReturn(Optional.of(deviceType));

        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getAuthenticationDeviceAccessLevel()).thenReturn(DeviceProtocolAuthenticationAccessLevels.ONE);
        when(securityPropertySet.getEncryptionDeviceAccessLevel()).thenReturn(DeviceProtocolEncryptionAccessLevels.ONE);

        SecurityPropertySetBuilder builder = mock(SecurityPropertySetBuilder.class);
        when(builder.addUserAction(any(DeviceSecurityUserAction.class))).thenReturn(builder);
        when(builder.authenticationLevel(anyInt())).thenReturn(builder);
        when(builder.encryptionLevel(anyInt())).thenReturn(builder);
        when(builder.build()).thenReturn(securityPropertySet);

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.createSecurityPropertySet(anyString())).thenReturn(builder);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(securityPropertySet.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfigurationService.findDeviceConfiguration(456L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(456L, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));

        SecurityPropertySetInfo securityPropertySetInfo = new SecurityPropertySetInfo();
        securityPropertySetInfo.name = "addSecurityPropertySetAddDefaultPrivileges";
        securityPropertySetInfo.authenticationLevel = SecurityLevelInfo.from(DeviceProtocolAuthenticationAccessLevels.ONE);
        securityPropertySetInfo.authenticationLevelId = DeviceProtocolAuthenticationAccessLevels.ONE.getId();
        securityPropertySetInfo.encryptionLevel = SecurityLevelInfo.from(DeviceProtocolEncryptionAccessLevels.ONE);
        securityPropertySetInfo.encryptionLevelId = DeviceProtocolEncryptionAccessLevels.ONE.getId();

        // Business method
        target("/devicetypes/123/deviceconfigurations/456/securityproperties").request().post(Entity.json(securityPropertySetInfo));

        // Asserts
        verify(builder, atLeastOnce()).addUserAction(any(DeviceSecurityUserAction.class));
    }

    @Test
    public void testGetSecurityPropertySet() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(Optional.of(deviceType));
        Group group1 = mockUserGroup(66L, "Z - user group 1");
        Group group2 = mockUserGroup(67L, "A - user group 2");
        Group group3 = mockUserGroup(68L, "O - user group 1");
        when(userService.getGroups()).thenReturn(Arrays.asList(group2, group1, group3));
        SecurityPropertySet sps1 = mockSecurityPropertySet(101L, "Primary", 1, "Auth1", 1001, "Encrypt1", EnumSet.of(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2));
        when(sps1.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        SecurityPropertySet sps2 = mockSecurityPropertySet(102L, "Secondary", 2, "Auth2", 1002, "Encrypt2", EnumSet.of(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1));
        when(sps2.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1, sps2));
        when(deviceConfigurationService.findDeviceConfiguration(456L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(456L, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));
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
        assertThat(jsonModel.<String>get("$.data[0].executionLevels[0].id")).isEqualTo(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1.getPrivilege());
        assertThat(jsonModel.<String>get("$.data[0].executionLevels[0].name")).isEqualTo("Level 1");
        assertThat(jsonModel.<List>get("$.data[0].executionLevels[0].userRoles")).hasSize(3);
        assertThat(jsonModel.<Integer>get("$.data[0].executionLevels[0].userRoles[0].id")).isEqualTo(67);
        assertThat(jsonModel.<String>get("$.data[0].executionLevels[0].userRoles[0].name")).isEqualTo("A - user group 2");
        assertThat(jsonModel.<Integer>get("$.data[0].executionLevels[0].userRoles[1].id")).isEqualTo(68);
        assertThat(jsonModel.<String>get("$.data[0].executionLevels[0].userRoles[1].name")).isEqualTo("O - user group 1");
        assertThat(jsonModel.<Integer>get("$.data[0].executionLevels[0].userRoles[2].id")).isEqualTo(66);
        assertThat(jsonModel.<String>get("$.data[0].executionLevels[0].userRoles[2].name")).isEqualTo("Z - user group 1");
        assertThat(jsonModel.<String>get("$.data[0].executionLevels[1].id")).isEqualTo(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2.getPrivilege());
        assertThat(jsonModel.<String>get("$.data[0].executionLevels[1].name")).isEqualTo("Level 2");

        assertThat(jsonModel.<String>get("$.data[1].executionLevels[0].id")).isEqualTo(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1.getPrivilege());
        assertThat(jsonModel.<String>get("$.data[1].executionLevels[0].name")).isEqualTo("Level 1");
        assertThat(jsonModel.<String>get("$.data[1].executionLevels[1].id")).isEqualTo(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1.getPrivilege());
        assertThat(jsonModel.<String>get("$.data[1].executionLevels[1].name")).isEqualTo("Level 1");
    }

    @Test
    public void testGetSecurityPropertySetFilteredByPrivilege() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(Optional.of(deviceType));
        Group group1 = mockUserGroup(66L, "Z - user group 1", Arrays.asList(Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1));
        Group group2 = mockUserGroup(67L, "A - user group 2", Arrays.asList(Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4));
        Group group3 = mockUserGroup(68L, "O - user group 3", Collections.emptyList());
        when(userService.getGroups()).thenReturn(Arrays.asList(group2, group1, group3));
        SecurityPropertySet sps1 = mockSecurityPropertySet(101L, "Primary", 1, "Auth1", 1001, "Encrypt1", EnumSet.of(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2));
        when(sps1.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        SecurityPropertySet sps2 = mockSecurityPropertySet(102L, "Secondary", 2, "Auth2", 1002, "Encrypt2", EnumSet.of(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES4, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1));
        when(sps2.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1, sps2));
        when(deviceConfigurationService.findDeviceConfiguration(456L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(456L, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));
        String response = target("/devicetypes/123/deviceconfigurations/456/securityproperties").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);

        assertThat(jsonModel.<List>get("$.data")).hasSize(2);
        assertThat(jsonModel.<List>get("$.data[0].executionLevels")).hasSize(2);
        assertThat(jsonModel.<List>get("$.data[0].executionLevels[0].userRoles")).hasSize(1);
        assertThat(jsonModel.<Integer>get("$.data[0].executionLevels[0].userRoles[0].id")).isEqualTo(66);
        assertThat(jsonModel.<String>get("$.data[0].executionLevels[0].userRoles[0].name")).isEqualTo("Z - user group 1");
        assertThat(jsonModel.<List>get("$.data[0].executionLevels[1].userRoles")).isEmpty();

        assertThat(jsonModel.<List>get("$.data[1].executionLevels")).hasSize(2);
        assertThat(jsonModel.<List>get("$.data[1].executionLevels[0].userRoles")).isEmpty();
        assertThat(jsonModel.<List>get("$.data[1].executionLevels[1].userRoles")).hasSize(1);
        assertThat(jsonModel.<Integer>get("$.data[1].executionLevels[1].userRoles[0].id")).isEqualTo(67);
        assertThat(jsonModel.<String>get("$.data[1].executionLevels[1].userRoles[0].name")).isEqualTo("A - user group 2");
    }

    @Test
    public void testDeleteSecuritySetBadVersion(){
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(15L);
        when(deviceConfiguration.getVersion()).thenReturn(OK_VERSION);
        when(deviceConfigurationService.findDeviceConfiguration(15L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(15L, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(15L, BAD_VERSION)).thenReturn(Optional.empty());

        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(11L);
        when(securityPropertySet.getName()).thenReturn("name");
        when(securityPropertySet.getVersion()).thenReturn(OK_VERSION);
        when(securityPropertySet.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfigurationService.findSecurityPropertySet(11L)).thenReturn(Optional.of(securityPropertySet));
        when(deviceConfigurationService.findAndLockSecurityPropertySetByIdAndVersion(11L, OK_VERSION)).thenReturn(Optional.of(securityPropertySet));
        when(deviceConfigurationService.findAndLockSecurityPropertySetByIdAndVersion(11L, BAD_VERSION)).thenReturn(Optional.empty());

        SecurityPropertySetInfo info = new SecurityPropertySetInfo();
        info.id = 11L;
        info.version = BAD_VERSION;
        info.parent = new VersionInfo<>(15L ,OK_VERSION);

        Response response = target("/devicetypes/123/deviceconfigurations/15/securityproperties/11").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    private Group mockUserGroup(long id, String name) {
        Group mock = mock(Group.class);
        when(mock.hasPrivilege(Matchers.matches("MDC"), Matchers.<String>anyObject())).thenReturn(true);
        when(mock.getName()).thenReturn(name);
        when(mock.getId()).thenReturn(id);
        return mock;
    }

    private Group mockUserGroup(long id, String name, List<String> privileges) {
        Group mock = mock(Group.class);
        when(mock.hasPrivilege(Matchers.matches("MDC"), Matchers.<String>anyObject())).then(invocationOnMock -> privileges.contains(invocationOnMock.getArguments()[1]));
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
        when(authenticationAccessLevel.getTranslation()).thenReturn(authenticationAccessLevelName);
        when(mock.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationAccessLevel);
        EncryptionDeviceAccessLevel encryptionAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionAccessLevel.getId()).thenReturn(encryptionAccessLevelId);
        when(encryptionAccessLevel.getTranslation()).thenReturn(encryptionAccessLevelName);
        when(mock.getEncryptionDeviceAccessLevel()).thenReturn(encryptionAccessLevel);
        when(mock.getUserActions()).thenReturn(userAction);
        when(mock.getVersion()).thenReturn(OK_VERSION);
        return mock;
    }

    private enum DeviceProtocolAuthenticationAccessLevels implements AuthenticationDeviceAccessLevel {
        ONE {
            @Override
            public int getId() {
                return 1;
            }
        },
        TWO {
            @Override
            public int getId() {
                return 2;
            }
        };

        @Override
        public String getTranslation() {
            return "DeviceProtocolAuthenticationAccessLevels" + this.name();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }

    }

    private enum DeviceProtocolEncryptionAccessLevels implements EncryptionDeviceAccessLevel {
        ONE {
            @Override
            public int getId() {
                return 1;
            }
        },
        TWO {
            @Override
            public int getId() {
                return 2;
            }
        };

        @Override
        public String getTranslation() {
            return "DeviceProtocolEncryptionAccessLevels" + this.name();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }

    }

}
