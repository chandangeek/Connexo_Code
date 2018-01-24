/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryParameters;
import com.elster.jupiter.pki.AliasParameterFilter;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityAccessorTypeUpdater;
import com.elster.jupiter.pki.SecurityAccessorUserAction;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.security.Privileges;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.elster.jupiter.users.Group;
import com.energyict.mdc.device.configuration.rest.ExecutionLevelInfo;
import com.energyict.mdc.device.configuration.rest.KeyFunctionTypePrivilegeTranslationKeys;
import com.energyict.mdc.device.configuration.rest.SecurityAccessorInfo;

import com.jayway.jsonpath.JsonModel;
import net.minidev.json.JSONObject;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SecurityAccessorTypeResourceTest extends DeviceConfigurationApplicationJerseyTest {
    private static final String NAME_X = "NameX";
    private static final String NAME_W = "Namew";
    private static final String DESCRIPTION_X = "Epic description";
    private static final String DESCRIPTION_W = "Epic description2";
    private static final String KEY_TYPE_NAME = "AES 128";
    private static final String CERTIFICATE_TYPE_NAME = "Certificate";

    private List<PropertySpec> certificatePropertySpecs;
    private TrustStore trustStore;
    private KeyType keyType, certificateType;
    private SecurityAccessorType certificateAccessorType, keyAccessorType;
    private ClientCertificateWrapper actualClientCertificateWrapper;
    private ClientCertificateWrapper tempClientCertificateWrapper;
    private SecurityAccessor<CertificateWrapper> certificateAccessor;

    @Override
    public void setupThesaurus() {
        // TODO: get rid of base implementation? It seems to interfere with many tests
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        certificatePropertySpecs = Collections.singletonList(mockPropertySpec("alias"));
        trustStore = mockTrustStore(33);
        keyType = mockKeyType(1, KEY_TYPE_NAME, CryptographicType.SymmetricKey);
        certificateType = mockKeyType(12, CERTIFICATE_TYPE_NAME, CryptographicType.TrustedCertificate);
        keyAccessorType = mockKeyAccessorType(1, 2, NAME_X, DESCRIPTION_X);
        certificateAccessorType = mockCertificateAccessorType(2, 1, NAME_W, DESCRIPTION_W);
        actualClientCertificateWrapper = mockClientCertificateWrapper(certificatePropertySpecs, "alias", "comserver", "myAlias");
        tempClientCertificateWrapper = mockClientCertificateWrapper(certificatePropertySpecs, "alias", "newcomserver", "myAlias");
        certificateAccessor = mockClientCertificateAccessor(certificatePropertySpecs, certificateAccessorType, actualClientCertificateWrapper);
        when(securityManagementService.getPropertySpecs(certificateAccessorType)).thenReturn(certificatePropertySpecs);
        when(securityManagementService.getPropertySpecs(eq(certificateType), anyString())).thenReturn(certificatePropertySpecs);
        when(securityManagementService.findCertificateWrapper(anyString())).thenReturn(Optional.empty());
        when(securityManagementService.findCertificateWrapper("comserver")).thenReturn(Optional.of(actualClientCertificateWrapper));
        when(securityManagementService.findCertificateWrapper("newcomserver")).thenReturn(Optional.of(tempClientCertificateWrapper));
        Finder<CertificateWrapper> finder = mockFinder(Collections.singletonList(actualClientCertificateWrapper));
        when(securityManagementService.getAliasesByFilter(any(AliasParameterFilter.class))).thenReturn(finder);
    }

    @Test
    public void testGetAllSecurityAccessorTypes() throws Exception {
        when(securityManagementService.getSecurityAccessorTypes()).thenReturn(Arrays.asList(keyAccessorType, certificateAccessorType));

        Response response = target("/securityaccessors").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());

        assertThat(model.<Number>get("$.total")).isEqualTo(2);
        assertThat(model.<List>get("$.securityaccessors")).hasSize(2);
        assertThat(model.<Number>get("$.securityaccessors[0].id")).isEqualTo(2);
        assertThat(model.<Number>get("$.securityaccessors[0].version")).isEqualTo(1);
        assertThat(model.<String>get("$.securityaccessors[0].name")).isEqualTo(NAME_W);
        assertThat(model.<String>get("$.securityaccessors[0].description")).isEqualTo(DESCRIPTION_W);
        assertThat(model.<Number>get("$.securityaccessors[0].storageMethod")).isNull();
        assertThat(model.<Number>get("$.securityaccessors[0].trustStoreId")).isEqualTo(33);
        assertThat(model.<Number>get("$.securityaccessors[0].keyType.id")).isEqualTo(12);
        assertThat(model.<String>get("$.securityaccessors[0].keyType.name")).isEqualTo(CERTIFICATE_TYPE_NAME);
        assertThat(model.<Boolean>get("$.securityaccessors[0].keyType.requiresDuration")).isFalse();
        assertThat(model.<Number>get("$.securityaccessors[1].id")).isEqualTo(1);
        assertThat(model.<Number>get("$.securityaccessors[1].version")).isEqualTo(2);
        assertThat(model.<String>get("$.securityaccessors[1].name")).isEqualTo(NAME_X);
        assertThat(model.<String>get("$.securityaccessors[1].description")).isEqualTo(DESCRIPTION_X);
        assertThat(model.<Number>get("$.securityaccessors[1].storageMethod")).isEqualTo("SSM");
        assertThat(model.<Number>get("$.securityaccessors[1].keyType.id")).isEqualTo(1);
        assertThat(model.<String>get("$.securityaccessors[1].keyType.name")).isEqualTo(KEY_TYPE_NAME);
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
    public void testGetKeyAccessorType() throws Exception {
        mockUserActions(keyAccessorType);
        when(securityManagementService.findSecurityAccessorTypeById(1)).thenReturn(Optional.of(keyAccessorType));
        when(securityManagementService.getDefaultValues(keyAccessorType)).thenReturn(Optional.empty());

        Response response = target("/securityaccessors/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());

        assertThat(model.<Number>get("$.id")).isEqualTo(1);
        assertThat(model.<Number>get("$.version")).isEqualTo(2);
        assertThat(model.<String>get("$.name")).isEqualTo(NAME_X);
        assertThat(model.<String>get("$.description")).isEqualTo(DESCRIPTION_X);
        assertThat(model.<String>get("$.storageMethod")).isEqualTo("SSM");
        assertThat(model.<Number>get("$.keyType.id")).isEqualTo(1);
        assertThat(model.<String>get("$.keyType.name")).isEqualTo(KEY_TYPE_NAME);
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
        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = 1;
        info.description = DESCRIPTION_X;
        info.name = NAME_X;
        info.storageMethod = "SSM";
        info.duration = new TimeDurationInfo(new TimeDuration(1, TimeDuration.TimeUnit.YEARS));

        SecurityAccessorType.Builder builder = mock(SecurityAccessorType.Builder.class);
        when(securityManagementService.addSecurityAccessorType(NAME_X, keyType)).thenReturn(builder);
        when(builder.keyEncryptionMethod(anyString())).thenReturn(builder);
        when(builder.description(anyString())).thenReturn(builder);
        when(builder.add()).thenReturn(keyAccessorType);

        info.keyType = new KeyTypeInfo();
        info.keyType.id = 1;
        info.keyType.name = KEY_TYPE_NAME;
        info.keyType.requiresDuration = true;

        Response response = target("/securityaccessors").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(securityManagementService).addSecurityAccessorType(NAME_X, keyType);
        verify(builder).description(DESCRIPTION_X);
        verify(builder).duration(info.duration.asTimeDuration());
        verify(builder).keyEncryptionMethod("SSM");
        verify(builder, never()).trustStore(any(TrustStore.class));
        verify(builder).add();
        verify(builder, never()).managedCentrally();
    }

    @Test
    public void testAddCertificateAccessorType() throws Exception {
        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = 1;
        info.description = DESCRIPTION_W;
        info.name = NAME_W;
        info.storageMethod = "SSM";

        SecurityAccessorType.Builder builder = mock(SecurityAccessorType.Builder.class);
        when(securityManagementService.addSecurityAccessorType(NAME_W, certificateType)).thenReturn(builder);
        when(builder.trustStore(any(TrustStore.class))).thenReturn(builder);
        when(builder.keyEncryptionMethod(anyString())).thenReturn(builder);
        when(builder.description(anyString())).thenReturn(builder);
        when(builder.add()).thenReturn(certificateAccessorType);

        info.trustStoreId = 33;
        info.keyType = new KeyTypeInfo();
        info.keyType.id = 12;
        info.keyType.name = CERTIFICATE_TYPE_NAME;
        info.keyType.requiresDuration = false;

        Response response = target("/securityaccessors").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(securityManagementService).addSecurityAccessorType(NAME_W, certificateType);
        verify(builder).description(DESCRIPTION_W);
        verify(builder).duration(null);
        verify(builder).keyEncryptionMethod("SSM");
        verify(builder).trustStore(trustStore);
        verify(builder).add();
        verify(builder, never()).managedCentrally();
    }

    @Test
    public void testEditKeyAccessorType() throws Exception {
        when(securityManagementService.findAndLockSecurityAccessorType(1, 2)).thenReturn(Optional.of(keyAccessorType));
        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = 1;
        info.version = 2;
        info.description = DESCRIPTION_W;
        info.name = NAME_W;
        info.duration = new TimeDurationInfo(new TimeDuration(1, TimeDuration.TimeUnit.YEARS));
        ExecutionLevelInfo executionLevelInfo = new ExecutionLevelInfo();
        executionLevelInfo.id = Privileges.Constants.EDIT_SECURITY_PROPERTIES_1;
        info.editLevels = Collections.singletonList(executionLevelInfo);
        SecurityAccessorTypeUpdater updater = mock(SecurityAccessorTypeUpdater.class);
        when(keyAccessorType.startUpdate()).thenReturn(updater);
        when(updater.complete()).thenReturn(keyAccessorType);

        Response response = target("/securityaccessors/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(keyAccessorType).startUpdate();
        verify(updater).description(info.description);
        verify(updater).name(info.name);
        verify(updater).duration(info.duration.asTimeDuration());
        verify(updater).addUserAction(SecurityAccessorUserAction.EDIT_SECURITY_PROPERTIES_1);
        verify(updater).complete();
    }

    @Test
    public void testEditFailed() throws IOException {
        when(securityManagementService.findAndLockSecurityAccessorType(1, 1)).thenReturn(Optional.empty());
        when(securityManagementService.findSecurityAccessorTypeById(1)).thenReturn(Optional.of(keyAccessorType));
        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = 1;
        info.version = 1;
        info.description = DESCRIPTION_W;
        info.name = NAME_W;
        info.duration = new TimeDurationInfo(new TimeDuration(1, TimeDuration.TimeUnit.YEARS));

        Response response = target("/securityaccessors/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());

        assertThat(model.<String>get("$.error")).contains(NAME_W + " has changed");
        assertThat(model.<Number>get("$.version")).isEqualTo(2);

        verify(keyAccessorType, never()).startUpdate();
    }

    @Test
    public void testDeleteSecurityAccessorType() throws Exception {
        when(securityManagementService.findAndLockSecurityAccessorType(1, 2)).thenReturn(Optional.of(keyAccessorType));

        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = 1;
        info.version = 2;
        info.name = NAME_X;

        Response response = target("/securityaccessors/1").request().method("DELETE", Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());

        verify(keyAccessorType).delete();
    }

    @Test
    public void testDeleteFailed() throws IOException {
        when(securityManagementService.findAndLockSecurityAccessorType(1, 2)).thenReturn(Optional.empty());
        when(securityManagementService.findSecurityAccessorTypeById(1)).thenReturn(Optional.empty());
        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = 1;
        info.version = 2;
        info.name = NAME_X;

        Response response = target("/securityaccessors/1").request().method("DELETE", Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());

        assertThat(model.<String>get("$.error")).contains(NAME_X + " has changed");
        assertThat(model.<Number>get("$.version")).isNull();

        verify(keyAccessorType, never()).delete();
    }

    // Here begin tests on default values

    @Test
    public void testGetCertificateAccessorType() throws Exception {
        mockUserActions(certificateAccessorType);
        when(securityManagementService.findSecurityAccessorTypeById(2)).thenReturn(Optional.of(certificateAccessorType));
        when(certificateAccessorType.isManagedCentrally()).thenReturn(true);
        when(securityManagementService.getDefaultValues(certificateAccessorType)).thenReturn(Optional.of(certificateAccessor));

        Response response = target("/securityaccessors/2").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());

        assertThat(model.<Number>get("$.id")).isEqualTo(2);
        assertThat(model.<Number>get("$.version")).isEqualTo(1);
        assertThat(model.<String>get("$.name")).isEqualTo(NAME_W);
        assertThat(model.<String>get("$.description")).isEqualTo(DESCRIPTION_W);
        assertThat(model.<String>get("$.storageMethod")).isNull();
        assertThat(model.<Number>get("$.keyType.id")).isEqualTo(12);
        assertThat(model.<String>get("$.keyType.name")).isEqualTo(CERTIFICATE_TYPE_NAME);
        assertThat(model.<Boolean>get("$.keyType.requiresDuration")).isFalse();
        assertThat(model.<Number>get("$.duration")).isNull();
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
    public void testPreviewCertificateProperties() throws Exception {
        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.keyType = new KeyTypeInfo();
        info.keyType.name = CERTIFICATE_TYPE_NAME;

        Response response = target("/securityaccessors/previewproperties").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<List>get("$.currentProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.currentProperties[0].key")).isEqualTo("alias");
        assertThat(jsonModel.<JSONObject>get("$.currentProperties[0].propertyValueInfo")).isEmpty();
        assertThat(jsonModel.<String>get("$.currentProperties[0].propertyTypeInfo.propertyValuesResource.possibleValuesURI"))
                .isEqualTo("http://localhost:9998/securityaccessors/certificates/aliases");
        assertThat(jsonModel.<List>get("$.tempProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.tempProperties[0].key")).isEqualTo("alias");
        assertThat(jsonModel.<JSONObject>get("$.tempProperties[0].propertyValueInfo")).isEmpty();
    }

    @Test
    public void testAliasPropertyTypeAheadFiltering() throws Exception {
        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.keyType = new KeyTypeInfo();
        info.keyType.name = CERTIFICATE_TYPE_NAME;

        SecurityAccessorInfo response = target("/securityaccessors/previewproperties").request().post(Entity.json(info), SecurityAccessorInfo.class);
        URI uri = new URI(response.currentProperties.get(0).propertyTypeInfo.propertyValuesResource.possibleValuesURI);
        target(uri.getPath())
                .queryParam("filter", ExtjsFilter.filter().property("alias", "com").create())
                .request()
                .get();
        ArgumentCaptor<AliasParameterFilter> captor = ArgumentCaptor.forClass(AliasParameterFilter.class);
        verify(securityManagementService).getAliasesByFilter(captor.capture());
        assertThat(captor.getValue().searchParam).isEqualTo("alias");
        assertThat(captor.getValue().searchValue).isEqualTo("*com*");
        assertThat(captor.getValue().trustStore).isNull();
    }

    @Test
    public void testAliasPropertyTypeAheadFilteringWithWildCard() throws Exception {
        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.keyType = new KeyTypeInfo();
        info.keyType.name = CERTIFICATE_TYPE_NAME;

        SecurityAccessorInfo response = target("/securityaccessors/previewproperties").request().post(Entity.json(info), SecurityAccessorInfo.class);
        URI uri = new URI(response.tempProperties.get(0).propertyTypeInfo.propertyValuesResource.possibleValuesURI);
        target(uri.getPath())
                .queryParam("filter", ExtjsFilter.filter().property("alias", "com*").create())
                .request()
                .get();
        ArgumentCaptor<AliasParameterFilter> captor = ArgumentCaptor.forClass(AliasParameterFilter.class);
        verify(securityManagementService).getAliasesByFilter(captor.capture());
        assertThat(captor.getValue().searchParam).isEqualTo("alias");
        assertThat(captor.getValue().searchValue).isEqualTo("com*");
        assertThat(captor.getValue().trustStore).isNull();
    }

    @Test
    public void testAliasPropertyTypeAheadFilteringWithAliasAndTrustStore() throws Exception {
        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.keyType = new KeyTypeInfo();
        info.keyType.name = CERTIFICATE_TYPE_NAME;

        SecurityAccessorInfo response = target("/securityaccessors/previewproperties").request().post(Entity.json(info), SecurityAccessorInfo.class);
        URI uri = new URI(response.currentProperties.get(0).propertyTypeInfo.propertyValuesResource.possibleValuesURI);
        Response response1 = target(uri.getPath())
                .queryParam("filter", ExtjsFilter.filter().property("alias", "com*").property("trustStore", 33L).create())
                .request()
                .get();
        ArgumentCaptor<AliasParameterFilter> captor = ArgumentCaptor.forClass(AliasParameterFilter.class);
        verify(securityManagementService).getAliasesByFilter(captor.capture());
        assertThat(captor.getValue().searchParam).isEqualTo("alias");
        assertThat(captor.getValue().searchValue).isEqualTo("com*");
        assertThat(captor.getValue().trustStore).isEqualTo(trustStore);
        JsonModel jsonModel = JsonModel.create((InputStream)response1.getEntity());
        System.out.println(jsonModel.toJson());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.aliases[0].alias")).isEqualTo("myAlias");
    }

    @Test
    public void testAliasPropertyTypeAheadFilteringEmptyAlias() throws Exception {
        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.keyType = new KeyTypeInfo();
        info.keyType.name = CERTIFICATE_TYPE_NAME;

        SecurityAccessorInfo response = target("/securityaccessors/previewproperties").request().post(Entity.json(info), SecurityAccessorInfo.class);
        URI uri = new URI(response.tempProperties.get(0).propertyTypeInfo.propertyValuesResource.possibleValuesURI);
        target(uri.getPath())
                .queryParam("filter", ExtjsFilter.filter().property("alias", "").property("trustStore", 33L).create())
                .request()
                .get();
        ArgumentCaptor<AliasParameterFilter> captor = ArgumentCaptor.forClass(AliasParameterFilter.class);
        verify(securityManagementService).getAliasesByFilter(captor.capture());
        assertThat(captor.getValue().searchParam).isEqualTo("alias");
        assertThat(captor.getValue().searchValue).isEqualTo("*");
        assertThat(captor.getValue().trustStore).isEqualTo(trustStore);
    }

    // TODO: more tests on add, edit, swap, clearTemp, remove with default values

    private TrustStore mockTrustStore(long id) {
        TrustStore trustStore = mock(TrustStore.class);
        when(trustStore.getId()).thenReturn(id);
        when(securityManagementService.findTrustStore(id)).thenReturn(Optional.of(trustStore));
        return trustStore;
    }

    private KeyType mockKeyType(long id, String name, CryptographicType cryptographicType) {
        KeyType keyType = mock(KeyType.class);
        when(keyType.getId()).thenReturn(id);
        when(keyType.getName()).thenReturn(name);
        when(keyType.getCryptographicType()).thenReturn(cryptographicType);
        when(securityManagementService.getKeyType(name)).thenReturn(Optional.of(keyType));
        return keyType;
    }

    private SecurityAccessorType mockKeyAccessorType(long id, long version, String name, String description) {
        SecurityAccessorType securityAccessorType = mock(SecurityAccessorType.class);
        when(securityAccessorType.getName()).thenReturn(name);
        when(securityAccessorType.getDescription()).thenReturn(description);
        when(securityAccessorType.getId()).thenReturn(id);
        when(securityAccessorType.getVersion()).thenReturn(version);
        when(securityAccessorType.getTrustStore()).thenReturn(Optional.empty());
        when(securityAccessorType.getKeyEncryptionMethod()).thenReturn("SSM");
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
        when(securityAccessorType.getKeyType()).thenReturn(certificateType);
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

    private PropertySpec mockPropertySpec(String name) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.getValueType()).thenReturn(String.class);
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        when(propertySpec.getName()).thenReturn(name);
        return propertySpec;
    }

    private ClientCertificateWrapper mockClientCertificateWrapper(List<PropertySpec> propertySpecs, String key, String value, String alias) {
        ClientCertificateWrapper clientCertificateWrapper = mock(ClientCertificateWrapper.class);
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        when(clientCertificateWrapper.getAlias()).thenReturn(alias);
        when(clientCertificateWrapper.getProperties()).thenReturn(map);
        when(clientCertificateWrapper.getPropertySpecs()).thenReturn(propertySpecs);
        when(clientCertificateWrapper.getExpirationTime()).thenReturn(Optional.of(Instant.now()));
        return clientCertificateWrapper;
    }

    private SecurityAccessor<CertificateWrapper> mockClientCertificateAccessor(List<PropertySpec> propertySpecs, SecurityAccessorType certificateKeyAccessorType,
                                                           CertificateWrapper clientCertificateWrapper) {
        SecurityAccessor<CertificateWrapper> securityAccessor1 = mock(SecurityAccessor.class);
        when(securityAccessor1.getTempValue()).thenReturn(Optional.empty());
        when(securityAccessor1.getActualValue()).thenReturn(Optional.of(clientCertificateWrapper));
        when(securityAccessor1.getKeyAccessorType()).thenReturn(certificateKeyAccessorType);
        when(securityAccessor1.getPropertySpecs()).thenReturn(propertySpecs);
        return securityAccessor1;
    }

    <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        when(finder.stream()).thenAnswer(invocation -> list.stream()); // Make sure to answer with a new stream each time
        return finder;
    }
}
