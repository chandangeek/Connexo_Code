/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityAccessorTypeUpdater;
import com.elster.jupiter.pki.SecurityAccessorUserAction;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.security.Privileges;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.elster.jupiter.users.Group;
import com.energyict.mdc.device.configuration.rest.ExecutionLevelInfo;
import com.energyict.mdc.device.configuration.rest.KeyFunctionTypePrivilegeTranslationKeys;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SecurityAccessorTypeResourceTest extends DeviceConfigurationApplicationJerseyTest {
    private static final String DESCRIPTION = "NEW DESCRIPTION";
    private static final String NAME = "NEW NAME";

    @Override
    public void setupThesaurus() {
        // TODO: get rid of base implementation? It seems to interfere with many tests
    }

    @Test
    public void testGetAllSecurityAccessorTypes() throws Exception {
        SecurityAccessorType securityAccessorType1 = mockKeyAccessorType(1, 2, "NameX", "Epic description");
        SecurityAccessorType securityAccessorType2 = mockCertificateAccessorType(2, 1, "Namew", "Epic description2");
        when(securityManagementService.getSecurityAccessorTypes()).thenReturn(Arrays.asList(securityAccessorType1, securityAccessorType2));

        Response response = target("/securityaccessors").request().get();
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
    public void testNotFoundSecurityAccessorType() throws Exception {
        when(securityManagementService.findSecurityAccessorTypeById(1)).thenReturn(Optional.empty());
        Response response = target("/securityaccessors/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetSecurityAccessorType() throws Exception {
        SecurityAccessorType securityAccessorType = mockKeyAccessorType(1, 1, NAME, DESCRIPTION);
        mockUserActions(securityAccessorType);
        when(securityManagementService.findSecurityAccessorTypeById(1)).thenReturn(Optional.of(securityAccessorType));

        Response response = target("/securityaccessors/1").request().get();
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
    public void testAddKeyAccessorType() throws Exception {
        SecurityAccessorInfo info = new SecurityAccessorInfo();
        info.id = 1;
        info.description = DESCRIPTION;
        info.name = NAME;
        info.storageMethod = "SSM";
        info.duration = new TimeDurationInfo(new TimeDuration(1, TimeDuration.TimeUnit.YEARS));

        KeyType keyType = mock(KeyType.class);
        when(keyType.getId()).thenReturn(1L);
        when(keyType.getName()).thenReturn("AES 128");
        when(keyType.getCryptographicType()).thenReturn(CryptographicType.SymmetricKey);

        when(securityManagementService.getKeyType("AES 128")).thenReturn(Optional.of(keyType));

        SecurityAccessorType.Builder builder = mock(SecurityAccessorType.Builder.class);
        when(securityManagementService.addSecurityAccessorType(NAME, keyType)).thenReturn(builder);
        when(builder.keyEncryptionMethod(anyString())).thenReturn(builder);
        when(builder.description(anyString())).thenReturn(builder);
        SecurityAccessorType addedKeyFunctionTypeDoesntMatter = mockKeyAccessorType(1, 1, NAME, DESCRIPTION);
        when(builder.add()).thenReturn(addedKeyFunctionTypeDoesntMatter);

        info.keyType = new KeyTypeInfo();
        info.keyType.id = 1;
        info.keyType.name = "AES 128";
        info.keyType.requiresDuration = true;

        Response response = target("/securityaccessors").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(securityManagementService).addSecurityAccessorType(NAME, keyType);
        verify(builder).description(DESCRIPTION);
        verify(builder).duration(info.duration.asTimeDuration());
        verify(builder).keyEncryptionMethod("SSM");
        verify(builder, never()).trustStore(any(TrustStore.class));
        verify(builder).add();
    }

    @Test
    public void testAddCertificateAccessorType() throws Exception {
        SecurityAccessorInfo info = new SecurityAccessorInfo();
        info.id = 1;
        info.description = DESCRIPTION;
        info.name = NAME;
        info.storageMethod = "SSM";

        KeyType keyType = mock(KeyType.class);
        when(keyType.getId()).thenReturn(2L);
        when(keyType.getName()).thenReturn("Certificate");
        when(keyType.getCryptographicType()).thenReturn(CryptographicType.TrustedCertificate);
        TrustStore trustStore = mock(TrustStore.class);
        when(trustStore.getId()).thenReturn(33L);

        when(securityManagementService.getKeyType("Certificate")).thenReturn(Optional.of(keyType));
        when(securityManagementService.findTrustStore(33)).thenReturn(Optional.of(trustStore));

        SecurityAccessorType.Builder builder = mock(SecurityAccessorType.Builder.class);
        when(securityManagementService.addSecurityAccessorType(NAME, keyType)).thenReturn(builder);
        when(builder.trustStore(any(TrustStore.class))).thenReturn(builder);
        when(builder.keyEncryptionMethod(anyString())).thenReturn(builder);
        when(builder.description(anyString())).thenReturn(builder);
        SecurityAccessorType addedKeyFunctionTypeDoesntMatter = mockCertificateAccessorType(1, 1, NAME, DESCRIPTION);
        when(builder.add()).thenReturn(addedKeyFunctionTypeDoesntMatter);

        info.trustStoreId = 33;
        info.keyType = new KeyTypeInfo();
        info.keyType.id = 1;
        info.keyType.name = "Certificate";
        info.keyType.requiresDuration = false;

        Response response = target("/securityaccessors").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(securityManagementService).addSecurityAccessorType(NAME, keyType);
        verify(builder).description(DESCRIPTION);
        verify(builder).duration(null);
        verify(builder).keyEncryptionMethod("SSM");
        verify(builder).trustStore(trustStore);
        verify(builder).add();
    }

    @Test
    public void testEditSecurityAccessorType() throws Exception {
        SecurityAccessorType securityAccessorType = mockKeyAccessorType(1, 1, "Name", "Epic description");
        when(securityManagementService.findAndLockSecurityAccessorType(1, 1)).thenReturn(Optional.of(securityAccessorType));
        SecurityAccessorInfo info = new SecurityAccessorInfo();
        info.id = 1;
        info.version = 1;
        info.description = "New Description";
        info.name = "New name";
        info.duration = new TimeDurationInfo(new TimeDuration(1, TimeDuration.TimeUnit.YEARS));
        ExecutionLevelInfo executionLevelInfo = new ExecutionLevelInfo();
        executionLevelInfo.id = Privileges.Constants.EDIT_SECURITY_PROPERTIES_1;
        info.editLevels = Collections.singletonList(executionLevelInfo);
        SecurityAccessorTypeUpdater updater = mock(SecurityAccessorTypeUpdater.class);
        when(securityAccessorType.startUpdate()).thenReturn(updater);
        when(updater.complete()).thenReturn(securityAccessorType);

        Response response = target("/securityaccessors/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(securityAccessorType).startUpdate();
        verify(updater).description(info.description);
        verify(updater).name(info.name);
        verify(updater).duration(info.duration.asTimeDuration());
        verify(updater).addUserAction(SecurityAccessorUserAction.EDIT_SECURITY_PROPERTIES_1);
        verify(updater).complete();
    }

    @Test
    public void testEditFailed() throws IOException {
        SecurityAccessorType securityAccessorType = mockKeyAccessorType(1, 2, "New_Key_Name", "Epic description");
        when(securityManagementService.findAndLockSecurityAccessorType(1, 1)).thenReturn(Optional.empty());
        when(securityManagementService.findSecurityAccessorTypeById(1)).thenReturn(Optional.of(securityAccessorType));
        SecurityAccessorInfo info = new SecurityAccessorInfo();
        info.id = 1;
        info.version = 1;
        info.description = "New Description";
        info.name = "New key name";
        info.duration = new TimeDurationInfo(new TimeDuration(1, TimeDuration.TimeUnit.YEARS));

        Response response = target("/securityaccessors/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());

        assertThat(model.<String>get("$.error")).contains("New key name has changed");
        assertThat(model.<Number>get("$.version")).isEqualTo(2);
    }

    @Test
    public void testDeleteSecurityAccessorType() throws Exception {
        SecurityAccessorType securityAccessorType = mockKeyAccessorType(1, 1, "Name", "Epic description");
        when(securityManagementService.findAndLockSecurityAccessorType(1, 1)).thenReturn(Optional.of(securityAccessorType));

        SecurityAccessorInfo info = new SecurityAccessorInfo();
        info.id = 1;
        info.version = 1;
        info.name = "Name";

        Response response = target("/securityaccessors/1").request().method("DELETE", Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(securityAccessorType).delete();
    }

    @Test
    public void testDeleteFailed() throws IOException {
        when(securityManagementService.findAndLockSecurityAccessorType(1, 1)).thenReturn(Optional.empty());
        when(securityManagementService.findSecurityAccessorTypeById(1)).thenReturn(Optional.empty());
        SecurityAccessorInfo info = new SecurityAccessorInfo();
        info.id = 1;
        info.version = 1;
        info.name = "Key name";

        Response response = target("/securityaccessors/1").request().method("DELETE", Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());

        assertThat(model.<String>get("$.error")).contains("Key name has changed");
        assertThat(model.<Number>get("$.version")).isNull();
    }

    private SecurityAccessorType mockKeyAccessorType(long id, long version, String name, String description) {
        SecurityAccessorType securityAccessorType = mock(SecurityAccessorType.class);
        when(securityAccessorType.getName()).thenReturn(name);
        when(securityAccessorType.getDescription()).thenReturn(description);
        when(securityAccessorType.getId()).thenReturn(id);
        when(securityAccessorType.getVersion()).thenReturn(version);
        when(securityAccessorType.getTrustStore()).thenReturn(Optional.empty());
        when(securityAccessorType.getKeyEncryptionMethod()).thenReturn("SSM");
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
