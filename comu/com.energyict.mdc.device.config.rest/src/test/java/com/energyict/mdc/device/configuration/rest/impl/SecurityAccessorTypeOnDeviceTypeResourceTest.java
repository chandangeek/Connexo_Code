/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityAccessorUserAction;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.security.Privileges;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.users.Group;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.configuration.rest.KeyFunctionTypePrivilegeTranslationKeys;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SecurityAccessorTypeOnDeviceTypeResourceTest extends DeviceConfigurationApplicationJerseyTest {
    private static final String DESCRIPTION = "NEW DESCRIPTION";
    private static final String NAME = "NEW NAME";

    @Override
    public void setupThesaurus() {
        // TODO: get rid of base implementation? It seems to interfere with many tests
    }

    @Test
    public void testGetAllSecurityAccessorTypesOfDeviceType() throws Exception {
        DeviceType deviceType = mockDeviceType("device type 1", 66);
        SecurityAccessorType securityAccessorType1 = mockKeyAccessorType(1, 2, "NameX", "Epic description");
        SecurityAccessorType securityAccessorType2 = mockCertificateAccessorType(2, 1, "Namew", "Epic description2");
        when(deviceConfigurationService.findDeviceType(66)).thenReturn(Optional.of(deviceType));
        when(deviceType.getSecurityAccessorTypes()).thenReturn(Arrays.asList(securityAccessorType1, securityAccessorType2));

        Response response = target("/devicetypes/66/securityaccessors").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());

        assertThat(model.<Number>get("$.total")).isEqualTo(2);
        assertThat(model.<List>get("$.securityaccessors")).hasSize(2);
        assertThat(model.<Number>get("$.securityaccessors[0].id")).isEqualTo(2);
        assertThat(model.<Number>get("$.securityaccessors[0].version")).isEqualTo(1);
        assertThat(model.<String>get("$.securityaccessors[0].name")).isEqualTo("Namew");
        assertThat(model.<String>get("$.securityaccessors[0].description")).isEqualTo("Epic description2");
        assertThat(model.<Number>get("$.securityaccessors[0].storageMethod")).isNull();
        assertThat(model.<Number>get("$.securityaccessors[0].trustStoreId")).isEqualTo(33);
        assertThat(model.<Number>get("$.securityaccessors[0].keyType.id")).isEqualTo(12);
        assertThat(model.<String>get("$.securityaccessors[0].keyType.name")).isEqualTo("Certificate");
        assertThat(model.<Boolean>get("$.securityaccessors[0].keyType.requiresDuration")).isFalse();
        assertThat(model.<Number>get("$.securityaccessors[1].id")).isEqualTo(1);
        assertThat(model.<Number>get("$.securityaccessors[1].version")).isEqualTo(2);
        assertThat(model.<String>get("$.securityaccessors[1].name")).isEqualTo("NameX");
        assertThat(model.<String>get("$.securityaccessors[1].description")).isEqualTo("Epic description");
        assertThat(model.<Number>get("$.securityaccessors[1].storageMethod")).isEqualTo("SSM");
        assertThat(model.<Number>get("$.securityaccessors[1].keyType.id")).isEqualTo(1);
        assertThat(model.<String>get("$.securityaccessors[1].keyType.name")).isEqualTo("Name of the keytype");
        assertThat(model.<Boolean>get("$.securityaccessors[1].keyType.requiresDuration")).isTrue();
        assertThat(model.<Number>get("$.securityaccessors[1].duration.count")).isEqualTo(2);
        assertThat(model.<Number>get("$.securityaccessors[1].duration.asSeconds")).isEqualTo(5356800);
        assertThat(model.<String>get("$.securityaccessors[1].duration.localizedTimeUnit")).isEqualTo("months");
        assertThat(model.<String>get("$.securityaccessors[1].duration.timeUnit")).isEqualTo("months");
    }

    @Test
    public void testNotFoundDeviceTypeDuringGetAssignedAccessors() throws Exception {
        when(deviceConfigurationService.findDeviceType(66)).thenReturn(Optional.empty());
        Response response = target("/devicetypes/66/securityaccessors").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetAllSecurityAccessorTypesUnassignedToDeviceType() throws Exception {
        DeviceType deviceType = mockDeviceType("device type 1", 66);
        SecurityAccessorType securityAccessorType1 = mockKeyAccessorType(1, 2, "NameX", "Epic description");
        SecurityAccessorType securityAccessorType2 = mockCertificateAccessorType(2, 1, "Namew", "Epic description2");
        when(securityManagementService.getSecurityAccessorTypes(SecurityAccessorType.Purpose.DEVICE_OPERATIONS))
                .thenReturn(Arrays.asList(securityAccessorType1, securityAccessorType2));
        when(deviceConfigurationService.findDeviceType(66)).thenReturn(Optional.of(deviceType));
        when(deviceType.getSecurityAccessorTypes()).thenReturn(Collections.singletonList(securityAccessorType1));

        Response response = target("/devicetypes/66/securityaccessors/unassigned").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());

        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.securityaccessors")).hasSize(1);
        assertThat(model.<Number>get("$.securityaccessors[0].id")).isEqualTo(2);
        assertThat(model.<Number>get("$.securityaccessors[0].version")).isEqualTo(1);
        assertThat(model.<String>get("$.securityaccessors[0].name")).isEqualTo("Namew");
        assertThat(model.<String>get("$.securityaccessors[0].description")).isEqualTo("Epic description2");
        assertThat(model.<Number>get("$.securityaccessors[0].storageMethod")).isNull();
        assertThat(model.<Number>get("$.securityaccessors[0].trustStoreId")).isEqualTo(33);
        assertThat(model.<Number>get("$.securityaccessors[0].keyType.id")).isEqualTo(12);
        assertThat(model.<String>get("$.securityaccessors[0].keyType.name")).isEqualTo("Certificate");
        assertThat(model.<Boolean>get("$.securityaccessors[0].keyType.requiresDuration")).isFalse();
    }

    @Test
    public void testNotFoundDeviceTypeDuringGetUnassignedAccessors() throws Exception {
        when(deviceConfigurationService.findDeviceType(66)).thenReturn(Optional.empty());
        Response response = target("/devicetypes/66/securityaccessors/unassigned").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetSecurityAccessorType() throws Exception {
        DeviceType deviceType = mockDeviceType("device type 1", 66);
        SecurityAccessorType securityAccessorType = mockKeyAccessorType(1, 1, NAME, DESCRIPTION);
        mockUserActions(securityAccessorType);
        when(deviceConfigurationService.findDeviceType(66)).thenReturn(Optional.of(deviceType));
        when(deviceType.getSecurityAccessorTypes()).thenReturn(Collections.singletonList(securityAccessorType));

        Response response = target("/devicetypes/66/securityaccessors/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());

        assertThat(model.<Number>get("$.id")).isEqualTo(1);
        assertThat(model.<Number>get("$.version")).isEqualTo(1);
        assertThat(model.<String>get("$.name")).isEqualTo(NAME);
        assertThat(model.<String>get("$.description")).isEqualTo(DESCRIPTION);
        assertThat(model.<Number>get("$.storageMethod")).isEqualTo("SSM");
        assertThat(model.<Number>get("$.keyType.id")).isEqualTo(1);
        assertThat(model.<String>get("$.keyType.name")).isEqualTo("Name of the keytype");
        assertThat(model.<Boolean>get("$.keyType.requiresDuration")).isTrue();
        assertThat(model.<Number>get("$.duration.count")).isEqualTo(2);
        assertThat(model.<Number>get("$.duration.asSeconds")).isEqualTo(5356800);
        assertThat(model.<String>get("$.duration.localizedTimeUnit")).isEqualTo("months");
        assertThat(model.<String>get("$.duration.timeUnit")).isEqualTo("months");
        assertThat(model.<List>get("$.defaultEditLevels")).hasSize(4);
        assertThat(model.<List>get("$.defaultViewLevels")).hasSize(4);
        assertThat(model.<List>get("$.editLevels")).hasSize(1);
        assertThat(model.<String>get("$.editLevels[0].id")).isEqualTo(Privileges.Constants.EDIT_SECURITY_PROPERTIES_1);
        assertThat(model.<String>get("$.editLevels[0].name")).isEqualTo(KeyFunctionTypePrivilegeTranslationKeys.EDIT_1.getDefaultFormat());
        assertThat(model.<List>get("$.editLevels[0].userRoles")).hasSize(1);
        assertThat(model.<Number>get("$.editLevels[0].userRoles[0].id")).isEqualTo(11);
        assertThat(model.<String>get("$.editLevels[0].userRoles[0].name")).isEqualTo("Group");
        assertThat(model.<List>get("$.viewLevels")).hasSize(1);
        assertThat(model.<String>get("$.viewLevels[0].id")).isEqualTo(Privileges.Constants.VIEW_SECURITY_PROPERTIES_1);
        assertThat(model.<String>get("$.viewLevels[0].name")).isEqualTo(KeyFunctionTypePrivilegeTranslationKeys.VIEW_1.getDefaultFormat());
        assertThat(model.<List>get("$.viewLevels[0].userRoles")).hasSize(1);
        assertThat(model.<Number>get("$.viewLevels[0].userRoles[0].id")).isEqualTo(11);
        assertThat(model.<String>get("$.viewLevels[0].userRoles[0].name")).isEqualTo("Group");
    }

    @Test
    public void testNotFoundDeviceTypeDuringGetAssignedAccessor() throws Exception {
        when(deviceConfigurationService.findDeviceType(66)).thenReturn(Optional.empty());
        Response response = target("/devicetypes/66/securityaccessors/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testNotFoundAccessorOnDeviceType() throws Exception {
        DeviceType deviceType = mockDeviceType("device type 1", 66);
        when(deviceConfigurationService.findDeviceType(66)).thenReturn(Optional.of(deviceType));
        when(deviceType.getSecurityAccessorTypes()).thenReturn(Collections.emptyList());
        Response response = target("/devicetypes/66/securityaccessors/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testAddKeyAccessorTypesOnDeviceType() throws Exception {
        DeviceType deviceType = mockDeviceType("device type 1", 66);
        when(deviceConfigurationService.findAndLockDeviceType(66, 13)).thenReturn(Optional.of(deviceType));

        SecurityAccessorTypeInfo info1 = new SecurityAccessorTypeInfo();
        info1.id = 1;
        info1.version = 2;
        SecurityAccessorTypeInfo info2 = new SecurityAccessorTypeInfo();
        info2.id = 2;
        info2.version = 1;

        SecurityAccessorType securityAccessorType1 = mockKeyAccessorType(1, 2, "NameX", "Epic description");
        SecurityAccessorType securityAccessorType2 = mockCertificateAccessorType(2, 1, "Namew", "Epic description2");
        when(securityManagementService.findAndLockSecurityAccessorType(1, 2)).thenReturn(Optional.of(securityAccessorType1));
        when(securityManagementService.findAndLockSecurityAccessorType(2, 1)).thenReturn(Optional.of(securityAccessorType2));
        when(deviceType.addSecurityAccessorTypes(anyVararg())).thenReturn(true);

        SecurityAccessorsForDeviceTypeInfo info = new SecurityAccessorsForDeviceTypeInfo();
        info.name = deviceType.getName();
        info.version = 13;
        info.securityAccessors = Arrays.asList(info1, info2);

        Response response = target("/devicetypes/66/securityaccessors").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceType).addSecurityAccessorTypes(securityAccessorType1, securityAccessorType2);
        verify(deviceType).update();
    }

    @Test
    public void testSetDefaultKeySecurityAccessorTypeValueOnDeviceType() throws Exception {
        DeviceType deviceType = mockDeviceType("device type 1", 66);
        when(deviceConfigurationService.findAndLockDeviceType(66, 13)).thenReturn(Optional.of(deviceType));

        SecurityAccessorType securityAccessorType1 = mockKeyAccessorType(1, 2, "NameX", "Epic description");
        when(securityManagementService.findAndLockSecurityAccessorType(1, 2)).thenReturn(Optional.of(securityAccessorType1));
        when(deviceType.getSecurityAccessorTypes()).thenReturn(Arrays.asList(securityAccessorType1));

        ServiceKeyDefultValueInfo info = new ServiceKeyDefultValueInfo();
        info.value = "ABCDABCD";

        Response response = target("/devicetypes/66/securityaccessors/1/defaultkey").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceType).updateDefaultKeyOfSecurityAccessorType(securityAccessorType1, info.value);
    }

    @Test
    public void testAddKeyAccessorTypesFailedDueToDeviceTypeConflict() throws Exception {
        DeviceType deviceType = mockDeviceType("device type 1", 66);
        when(deviceConfigurationService.findAndLockDeviceType(66, 13)).thenReturn(Optional.empty());
        when(deviceConfigurationService.findDeviceType(66)).thenReturn(Optional.of(deviceType));

        SecurityAccessorTypeInfo info1 = new SecurityAccessorTypeInfo();
        info1.id = 1;
        info1.version = 2;
        SecurityAccessorTypeInfo info2 = new SecurityAccessorTypeInfo();
        info2.id = 2;
        info2.version = 1;
        SecurityAccessorsForDeviceTypeInfo info = new SecurityAccessorsForDeviceTypeInfo();
        info.name = deviceType.getName();
        info.version = 13;
        info.securityAccessors = Arrays.asList(info1, info2);

        Response response = target("/devicetypes/66/securityaccessors").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());

        assertThat(model.<String>get("$.error")).contains("device type 1 has changed");
        assertThat(model.<Number>get("$.version")).isEqualTo(24);
        verify(deviceType, never()).addSecurityAccessorTypes(anyVararg());
        verify(deviceType, never()).update();
    }

    @Test
    public void testAddKeyAccessorTypesFailedDueToSecurityAccessorTypeConflict() throws Exception {
        DeviceType deviceType = mockDeviceType("device type 1", 66);
        when(deviceConfigurationService.findAndLockDeviceType(66, 13)).thenReturn(Optional.of(deviceType));

        SecurityAccessorTypeInfo info1 = new SecurityAccessorTypeInfo();
        info1.id = 1;
        info1.version = 2;
        SecurityAccessorTypeInfo info2 = new SecurityAccessorTypeInfo();
        info2.id = 2;
        info2.version = 1;
        info2.name = "Certificate";

        SecurityAccessorType securityAccessorType1 = mockKeyAccessorType(1, 2, "NameX", "Epic description");
        when(securityManagementService.findAndLockSecurityAccessorType(1, 2)).thenReturn(Optional.of(securityAccessorType1));
        when(securityManagementService.findAndLockSecurityAccessorType(2, 1)).thenReturn(Optional.empty());
        when(securityManagementService.findSecurityAccessorTypeById(2)).thenReturn(Optional.empty());

        SecurityAccessorsForDeviceTypeInfo info = new SecurityAccessorsForDeviceTypeInfo();
        info.name = deviceType.getName();
        info.version = 13;
        info.securityAccessors = Arrays.asList(info1, info2);

        Response response = target("/devicetypes/66/securityaccessors").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());

        assertThat(model.<String>get("$.error")).contains("Certificate has changed");
        assertThat(model.<Number>get("$.version")).isNull();
        verify(deviceType, never()).addSecurityAccessorTypes(anyVararg());
        verify(deviceType, never()).update();
    }

    @Test
    public void testDeleteSecurityAccessorTypeFromDeviceType() throws Exception {
        DeviceType deviceType = mockDeviceType("device type 1", 66);
        when(deviceConfigurationService.findAndLockDeviceType(66, 13)).thenReturn(Optional.of(deviceType));
        SecurityAccessorType securityAccessorType = mockKeyAccessorType(1, 1, "Name", "Epic description");
        when(deviceType.getSecurityAccessorTypes()).thenReturn(Collections.singletonList(securityAccessorType));
        when(deviceType.removeSecurityAccessorType(securityAccessorType)).thenReturn(true);

        SecurityAccessorsForDeviceTypeInfo info = new SecurityAccessorsForDeviceTypeInfo();
        info.name = deviceType.getName();
        info.version = 13;

        Response response = target("/devicetypes/66/securityaccessors/1").request().method("DELETE", Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(deviceType).removeSecurityAccessorType(securityAccessorType);
        verify(deviceType).update();
    }

    @Test
    public void testDeleteSecurityAccessorTypeFailedDueToNotFoundAccessorType() throws Exception {
        DeviceType deviceType = mockDeviceType("device type 1", 66);
        when(deviceConfigurationService.findAndLockDeviceType(66, 13)).thenReturn(Optional.of(deviceType));
        when(deviceType.getSecurityAccessorTypes()).thenReturn(Collections.emptyList());

        SecurityAccessorsForDeviceTypeInfo info = new SecurityAccessorsForDeviceTypeInfo();
        info.name = deviceType.getName();
        info.version = 13;

        Response response = target("/devicetypes/66/securityaccessors/1").request().method("DELETE", Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        verify(deviceType, never()).removeSecurityAccessorType(any(SecurityAccessorType.class));
        verify(deviceType, never()).update();
    }

    @Test
    public void testDeleteSecurityAccessorTypeFailedDueToDeviceTypeConflict() throws Exception {
        DeviceType deviceType = mockDeviceType("device type 1", 66);
        when(deviceConfigurationService.findAndLockDeviceType(66, 13)).thenReturn(Optional.empty());
        when(deviceConfigurationService.findDeviceType(66)).thenReturn(Optional.of(deviceType));

        SecurityAccessorsForDeviceTypeInfo info = new SecurityAccessorsForDeviceTypeInfo();
        info.name = deviceType.getName();
        info.version = 13;

        Response response = target("/devicetypes/66/securityaccessors/1").request().method("DELETE", Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());

        assertThat(model.<String>get("$.error")).contains("device type 1 has changed");
        assertThat(model.<Number>get("$.version")).isEqualTo(24);
        verify(deviceType, never()).removeSecurityAccessorType(any(SecurityAccessorType.class));
        verify(deviceType, never()).update();
    }

    private SecurityAccessorType mockKeyAccessorType(long id, long version, String name, String description) {
        SecurityAccessorType securityAccessorType = mock(SecurityAccessorType.class);
        when(securityAccessorType.getName()).thenReturn(name);
        when(securityAccessorType.getDescription()).thenReturn(description);
        when(securityAccessorType.getId()).thenReturn(id);
        when(securityAccessorType.getVersion()).thenReturn(version);
        when(securityAccessorType.getTrustStore()).thenReturn(Optional.empty());
        when(securityAccessorType.getKeyEncryptionMethod()).thenReturn("SSM");
        when(securityAccessorType.getPurpose()).thenReturn(SecurityAccessorType.Purpose.DEVICE_OPERATIONS);
        KeyType keyType = mock(KeyType.class);
        when(keyType.getId()).thenReturn(1L);
        when(keyType.getName()).thenReturn("Name of the keytype");
        when(keyType.getCryptographicType()).thenReturn(CryptographicType.SymmetricKey);
        when(securityAccessorType.getKeyType()).thenReturn(keyType);
        TimeDuration validityPeriod = TimeDuration.months(2);
        when(securityAccessorType.getDuration()).thenReturn(Optional.of(validityPeriod));
        return securityAccessorType;
    }

    private SecurityAccessorType mockCertificateAccessorType(long id, long version, String name, String description) {
        SecurityAccessorType securityAccessorType = mock(SecurityAccessorType.class);
        when(securityAccessorType.getName()).thenReturn(name);
        when(securityAccessorType.getDescription()).thenReturn(description);
        when(securityAccessorType.getId()).thenReturn(id);
        when(securityAccessorType.getVersion()).thenReturn(version);
        when(securityAccessorType.getPurpose()).thenReturn(SecurityAccessorType.Purpose.DEVICE_OPERATIONS);
        TrustStore trustStore = mock(TrustStore.class);
        when(trustStore.getId()).thenReturn(33L);
        when(securityAccessorType.getTrustStore()).thenReturn(Optional.of(trustStore));
        KeyType keyType = mock(KeyType.class);
        when(keyType.getId()).thenReturn(12L);
        when(keyType.getName()).thenReturn("Certificate");
        when(keyType.getCryptographicType()).thenReturn(CryptographicType.TrustedCertificate);
        when(securityAccessorType.getKeyType()).thenReturn(keyType);
        when(securityAccessorType.getDuration()).thenReturn(Optional.empty());
        return securityAccessorType;
    }

    private void mockUserActions(SecurityAccessorType securityAccessorTypeMock) {
        when(securityAccessorTypeMock.getUserActions()).thenReturn(EnumSet.of(
                SecurityAccessorUserAction.VIEW_SECURITY_PROPERTIES_1,
                SecurityAccessorUserAction.EDIT_SECURITY_PROPERTIES_1));
        Group group = mock(Group.class);
        when(group.getId()).thenReturn(11L);
        when(group.getName()).thenReturn("Group");
        when(group.hasPrivilege("MDC", SecurityAccessorUserAction.VIEW_SECURITY_PROPERTIES_1.getPrivilege())).thenReturn(true);
        when(group.hasPrivilege("MDC", SecurityAccessorUserAction.EDIT_SECURITY_PROPERTIES_1.getPrivilege())).thenReturn(true);
        when(userService.getGroups()).thenReturn(Collections.singletonList(group));
    }
}
