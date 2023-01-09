/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.CertificateWrapperStatus;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyPurpose;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityAccessorTypeUpdater;
import com.elster.jupiter.pki.SecurityAccessorUserAction;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.security.Privileges;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.elster.jupiter.users.Group;
import com.energyict.mdc.device.configuration.rest.ExecutionLevelInfo;
import com.energyict.mdc.device.configuration.rest.KeyFunctionTypePrivilegeTranslationKeys;
import com.energyict.mdc.device.configuration.rest.SecurityAccessorInfo;

import com.jayway.jsonpath.JsonModel;
import net.minidev.json.JSONArray;
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
import java.util.stream.Collectors;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
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
        actualClientCertificateWrapper = mockClientCertificateWrapper(certificatePropertySpecs, "alias", "comserver", "myAlias1");
        tempClientCertificateWrapper = mockClientCertificateWrapper(certificatePropertySpecs, "alias", "newcomserver", "myAlias2");
        certificateAccessor = mockClientCertificateAccessor(3, certificatePropertySpecs, certificateAccessorType, actualClientCertificateWrapper);
        when(securityManagementService.getPropertySpecs(certificateAccessorType)).thenReturn(certificatePropertySpecs);
        when(securityManagementService.getPropertySpecs(eq(certificateType), anyString())).thenReturn(certificatePropertySpecs);
        when(securityManagementService.findCertificateWrapper(anyString())).thenReturn(Optional.empty());
        when(securityManagementService.findCertificateWrapper("comserver")).thenReturn(Optional.of(actualClientCertificateWrapper));
        when(securityManagementService.findCertificateWrapper("newcomserver")).thenReturn(Optional.of(tempClientCertificateWrapper));
        Finder<CertificateWrapper> finder = mockFinder(Collections.singletonList(actualClientCertificateWrapper));
        when(securityManagementService.getAliasesByFilter(any(SecurityManagementService.AliasSearchFilter.class))).thenReturn(finder);
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

        SecurityAccessorType.Builder builder = FakeBuilder.initBuilderStub(keyAccessorType, SecurityAccessorType.Builder.class);
        when(securityManagementService.addSecurityAccessorType(NAME_X, keyType)).thenReturn(builder);

        info.keyType = new KeyTypeInfo();
        info.keyType.id = 1;
        info.keyType.name = KEY_TYPE_NAME;
        info.keyType.requiresDuration = true;
        info.purpose = new IdWithNameInfo(SecurityAccessorType.Purpose.DEVICE_OPERATIONS.name(), null);

        Response response = target("/securityaccessors").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(securityManagementService).addSecurityAccessorType(NAME_X, keyType);
        verify(builder).description(DESCRIPTION_X);
        verify(builder).duration(info.duration.asTimeDuration());
        verify(builder).keyEncryptionMethod("SSM");
        verify(builder).purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS);
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

        SecurityAccessorType.Builder builder = FakeBuilder.initBuilderStub(certificateAccessorType, SecurityAccessorType.Builder.class);
        when(securityManagementService.addSecurityAccessorType(NAME_W, certificateType)).thenReturn(builder);

        info.trustStoreId = 33;
        info.keyType = new KeyTypeInfo();
        info.keyType.id = 12;
        info.keyType.name = CERTIFICATE_TYPE_NAME;
        info.keyType.requiresDuration = false;
        info.purpose = new IdWithNameInfo(SecurityAccessorType.Purpose.DEVICE_OPERATIONS.name(), null);

        Response response = target("/securityaccessors").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(securityManagementService).addSecurityAccessorType(NAME_W, certificateType);
        verify(builder).description(DESCRIPTION_W);
        verify(builder).duration(null);
        verify(builder).keyEncryptionMethod("SSM");
        verify(builder).purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS);
        verify(builder).trustStore(trustStore);
        verify(builder).add();
        verify(builder, never()).managedCentrally();

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
        assertThat(model.<String>get("$.purpose.id")).isEqualTo("DEVICE_OPERATIONS");
        assertThat(model.<String>get("$.purpose.name")).isEqualTo("Device operations");
        assertThat(model.<Object>get("$.defaultValue")).isNull();
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
    public void testGetCertificateAccessorTypeWithDefaultValues() throws Exception {
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
        assertThat(model.<List>get("$.defaultValue.currentProperties")).hasSize(1);
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].key")).isEqualTo("alias");
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].propertyValueInfo.value")).isEqualTo("comserver");
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].propertyTypeInfo.propertyValuesResource.possibleValuesURI"))
                .isEqualTo("http://localhost:9998/securityaccessors/certificates/aliases");
        assertThat(model.<List>get("$.defaultValue.tempProperties")).hasSize(1);
        assertThat(model.<String>get("$.defaultValue.tempProperties[0].key")).isEqualTo("alias");
        assertThat(model.<JSONObject>get("$.defaultValue.tempProperties[0].propertyValueInfo")).isEmpty();
        assertThat(model.<String>get("$.defaultValue.tempProperties[0].propertyTypeInfo.propertyValuesResource.possibleValuesURI"))
                .isEqualTo("http://localhost:9998/securityaccessors/certificates/aliases");
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
        assertThat(jsonModel.<String>get("$.tempProperties[0].propertyTypeInfo.propertyValuesResource.possibleValuesURI"))
                .isEqualTo("http://localhost:9998/securityaccessors/certificates/aliases");
    }

    @Test
    public void testAliasPropertyTypeAheadFiltering() throws Exception {
        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.keyType = new KeyTypeInfo();
        info.keyType.name = CERTIFICATE_TYPE_NAME;

        SecurityAccessorInfo response = target("/securityaccessors/previewproperties").request().post(Entity.json(info), SecurityAccessorInfo.class);
        URI uri = new URI(response.currentProperties.get(0).propertyTypeInfo.propertyValuesResource.possibleValuesURI);
        target(uri.getPath())
                .queryParam("alias", "com")
                .request()
                .get();
        ArgumentCaptor<SecurityManagementService.AliasSearchFilter> captor = ArgumentCaptor.forClass(SecurityManagementService.AliasSearchFilter.class);
        verify(securityManagementService).getAliasesByFilter(captor.capture());
        assertThat(captor.getValue().alias).isEqualTo("*com*");
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
                .queryParam("alias", "com*")
                .request()
                .get();
        ArgumentCaptor<SecurityManagementService.AliasSearchFilter> captor = ArgumentCaptor.forClass(SecurityManagementService.AliasSearchFilter.class);
        verify(securityManagementService).getAliasesByFilter(captor.capture());
        assertThat(captor.getValue().alias).isEqualTo("com*");
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
                .queryParam("alias", "my*")
                .queryParam("trustStore", 33)
                .request()
                .get();
        ArgumentCaptor<SecurityManagementService.AliasSearchFilter> captor = ArgumentCaptor.forClass(SecurityManagementService.AliasSearchFilter.class);
        verify(securityManagementService).getAliasesByFilter(captor.capture());
        assertThat(captor.getValue().alias).isEqualTo("my*");
        assertThat(captor.getValue().trustStore).isEqualTo(trustStore);
        JsonModel jsonModel = JsonModel.create((InputStream) response1.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.aliases[0].alias")).isEqualTo("myAlias1");
    }

    @Test
    public void testAliasPropertyTypeAheadFilteringEmptyAlias() throws Exception {
        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.keyType = new KeyTypeInfo();
        info.keyType.name = CERTIFICATE_TYPE_NAME;

        SecurityAccessorInfo response = target("/securityaccessors/previewproperties").request().post(Entity.json(info), SecurityAccessorInfo.class);
        URI uri = new URI(response.tempProperties.get(0).propertyTypeInfo.propertyValuesResource.possibleValuesURI);
        target(uri.getPath())
                .queryParam("alias", "")
                .queryParam("trustStore", 33)
                .request()
                .get();
        ArgumentCaptor<SecurityManagementService.AliasSearchFilter> captor = ArgumentCaptor.forClass(SecurityManagementService.AliasSearchFilter.class);
        verify(securityManagementService).getAliasesByFilter(captor.capture());
        assertThat(captor.getValue().alias).isEqualTo("*");
        assertThat(captor.getValue().trustStore).isEqualTo(trustStore);
    }

    @Test
    public void testAddCertificateAccessorTypeWithDefaultActiveValue() throws Exception {
        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = 1;
        info.description = DESCRIPTION_W;
        info.name = NAME_W;
        info.storageMethod = "SSM";

        SecurityAccessorType.Builder builder = FakeBuilder.initBuilderStub(certificateAccessorType, SecurityAccessorType.Builder.class);
        when(securityManagementService.addSecurityAccessorType(NAME_W, certificateType)).thenReturn(builder);
        when(securityManagementService.setDefaultValues(eq(certificateAccessorType), any(CertificateWrapper.class), any(CertificateWrapper.class)))
                .thenReturn(certificateAccessor);

        info.trustStoreId = 33;
        info.keyType = new KeyTypeInfo();
        info.keyType.id = 12;
        info.keyType.name = CERTIFICATE_TYPE_NAME;
        info.keyType.requiresDuration = false;
        info.purpose = new IdWithNameInfo(SecurityAccessorType.Purpose.DEVICE_OPERATIONS.name(), null);

        info.defaultValue = createDefaultValue(null, "comserver", null);

        Response response = target("/securityaccessors").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(securityManagementService).addSecurityAccessorType(NAME_W, certificateType);
        verify(builder).description(DESCRIPTION_W);
        verify(builder).duration(null);
        verify(builder).keyEncryptionMethod("SSM");
        verify(builder).trustStore(trustStore);
        verify(builder).managedCentrally();
        verify(builder).add();

        verify(securityManagementService).setDefaultValues(certificateAccessorType, actualClientCertificateWrapper, null);

        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.defaultValue.version")).isEqualTo(3);
        assertThat(model.<List>get("$.defaultValue.currentProperties")).hasSize(1);
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].key")).isEqualTo("alias");
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].propertyValueInfo.value")).isEqualTo("comserver");
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].propertyTypeInfo.propertyValuesResource.possibleValuesURI"))
                .isEqualTo("http://localhost:9998/securityaccessors/certificates/aliases");
        assertThat(model.<List>get("$.defaultValue.tempProperties")).hasSize(1);
        assertThat(model.<String>get("$.defaultValue.tempProperties[0].key")).isEqualTo("alias");
        assertThat(model.<JSONObject>get("$.defaultValue.tempProperties[0].propertyValueInfo")).isEmpty();
    }

    @Test
    public void testAddCertificateAccessorTypeWithDefaultValues() throws Exception {
        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = 1;
        info.description = DESCRIPTION_W;
        info.name = NAME_W;
        info.storageMethod = "SSM";

        SecurityAccessorType.Builder builder = FakeBuilder.initBuilderStub(certificateAccessorType, SecurityAccessorType.Builder.class);
        when(securityManagementService.addSecurityAccessorType(NAME_W, certificateType)).thenReturn(builder);
        when(securityManagementService.setDefaultValues(eq(certificateAccessorType), any(CertificateWrapper.class), any(CertificateWrapper.class)))
                .thenReturn(certificateAccessor);
        when(certificateAccessor.getTempValue()).thenReturn(Optional.of(tempClientCertificateWrapper));

        info.trustStoreId = 33;
        info.keyType = new KeyTypeInfo();
        info.keyType.id = 12;
        info.keyType.name = CERTIFICATE_TYPE_NAME;
        info.keyType.requiresDuration = false;
        info.purpose = new IdWithNameInfo(SecurityAccessorType.Purpose.DEVICE_OPERATIONS.name(), null);

        info.defaultValue = createDefaultValue(null, "comserver", "newcomserver");

        Response response = target("/securityaccessors").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(securityManagementService).addSecurityAccessorType(NAME_W, certificateType);
        verify(builder).description(DESCRIPTION_W);
        verify(builder).duration(null);
        verify(builder).keyEncryptionMethod("SSM");
        verify(builder).trustStore(trustStore);
        verify(builder).managedCentrally();
        verify(builder).add();

        verify(securityManagementService).setDefaultValues(certificateAccessorType, actualClientCertificateWrapper, tempClientCertificateWrapper);

        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.defaultValue.version")).isEqualTo(3);
        assertThat(model.<List>get("$.defaultValue.currentProperties")).hasSize(1);
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].key")).isEqualTo("alias");
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].propertyValueInfo.value")).isEqualTo("comserver");
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].propertyTypeInfo.propertyValuesResource.possibleValuesURI"))
                .isEqualTo("http://localhost:9998/securityaccessors/certificates/aliases");
        assertThat(model.<List>get("$.defaultValue.tempProperties")).hasSize(1);
        assertThat(model.<String>get("$.defaultValue.tempProperties[0].key")).isEqualTo("alias");
        assertThat(model.<String>get("$.defaultValue.tempProperties[0].propertyValueInfo.value")).isEqualTo("newcomserver");
        assertThat(model.<String>get("$.defaultValue.tempProperties[0].propertyTypeInfo.propertyValuesResource.possibleValuesURI"))
                .isEqualTo("http://localhost:9998/securityaccessors/certificates/aliases");
    }

    @Test
    public void testAddCertificateAccessorTypeWithOnlyPassiveDefaultValue() throws Exception {
        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = 1;
        info.description = DESCRIPTION_W;
        info.name = NAME_W;
        info.storageMethod = "SSM";

        SecurityAccessorType.Builder builder = FakeBuilder.initBuilderStub(certificateAccessorType, SecurityAccessorType.Builder.class);
        when(securityManagementService.addSecurityAccessorType(NAME_W, certificateType)).thenReturn(builder);
        when(securityManagementService.setDefaultValues(eq(certificateAccessorType), any(CertificateWrapper.class), any(CertificateWrapper.class)))
                .thenReturn(certificateAccessor);

        info.trustStoreId = 33;
        info.keyType = new KeyTypeInfo();
        info.keyType.id = 12;
        info.keyType.name = CERTIFICATE_TYPE_NAME;
        info.keyType.requiresDuration = false;
        info.purpose = new IdWithNameInfo(SecurityAccessorType.Purpose.DEVICE_OPERATIONS.name(), null);

        info.defaultValue = createDefaultValue(null, null, "newcomserver");

        Response response = target("/securityaccessors").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        verify(securityManagementService, never()).setDefaultValues(eq(certificateAccessorType), any(CertificateWrapper.class), any(CertificateWrapper.class));

        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Boolean>get("$.success")).isFalse();
        assertThat(model.<String>get("$.errors[0].msg")).isEqualTo("This field is required.");
        assertThat(model.<String>get("$.errors[0].id")).isEqualTo("defaultValue.currentProperties.alias");
    }

    @Test
    public void testUpdateCertificateAccessorTypeWithDefaultValues_ChangeActualAndSetTemp() throws Exception {
        when(securityManagementService.findAndLockSecurityAccessorType(2, 1)).thenReturn(Optional.of(certificateAccessorType));
        when(certificateAccessorType.isManagedCentrally()).thenReturn(true);
        when(securityManagementService.lockDefaultValues(certificateAccessorType, 3)).thenReturn(Optional.of(certificateAccessor));

        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = 2;
        info.version = 1;
        info.description = DESCRIPTION_W;
        info.name = NAME_W;
        SecurityAccessorTypeUpdater updater = mock(SecurityAccessorTypeUpdater.class);
        when(certificateAccessorType.startUpdate()).thenReturn(updater);
        when(updater.complete()).thenReturn(certificateAccessorType);

        info.defaultValue = createDefaultValue(3L, "newcomserver", "comserver");

        Response response = target("/securityaccessors/2").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(certificateAccessorType).startUpdate();
        verify(updater).description(info.description);
        verify(updater).name(info.name);
        verify(updater).complete();
        verify(certificateAccessor).setActualValue(tempClientCertificateWrapper);
        verify(certificateAccessor).setTempValue(actualClientCertificateWrapper);
        verify(certificateAccessor).save();

        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.defaultValue.version")).isEqualTo(3);
        assertThat(model.<List>get("$.defaultValue.currentProperties")).hasSize(1);
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].key")).isEqualTo("alias");
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].propertyValueInfo.value")).isEqualTo("newcomserver");
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].propertyTypeInfo.propertyValuesResource.possibleValuesURI"))
                .isEqualTo("http://localhost:9998/securityaccessors/certificates/aliases");
        assertThat(model.<List>get("$.defaultValue.tempProperties")).hasSize(1);
        assertThat(model.<String>get("$.defaultValue.tempProperties[0].key")).isEqualTo("alias");
        assertThat(model.<String>get("$.defaultValue.tempProperties[0].propertyValueInfo.value")).isEqualTo("comserver");
        assertThat(model.<String>get("$.defaultValue.tempProperties[0].propertyTypeInfo.propertyValuesResource.possibleValuesURI"))
                .isEqualTo("http://localhost:9998/securityaccessors/certificates/aliases");
    }

    @Test
    public void testUpdateCertificateAccessorTypeWithDefaultValues_OnlyUnsetTemp() throws Exception {
        when(securityManagementService.findAndLockSecurityAccessorType(2, 1)).thenReturn(Optional.of(certificateAccessorType));
        when(certificateAccessorType.isManagedCentrally()).thenReturn(true);
        when(securityManagementService.lockDefaultValues(certificateAccessorType, 3)).thenReturn(Optional.of(certificateAccessor));
        when(certificateAccessor.getTempValue()).thenReturn(Optional.of(tempClientCertificateWrapper));

        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = 2;
        info.version = 1;
        info.description = DESCRIPTION_W;
        info.name = NAME_W;
        SecurityAccessorTypeUpdater updater = mock(SecurityAccessorTypeUpdater.class);
        when(certificateAccessorType.startUpdate()).thenReturn(updater);
        when(updater.complete()).thenReturn(certificateAccessorType);

        info.defaultValue = createDefaultValue(3L, "comserver", null);

        Response response = target("/securityaccessors/2").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(certificateAccessorType).startUpdate();
        verify(updater).description(info.description);
        verify(updater).name(info.name);
        verify(updater).complete();
        verify(certificateAccessor).clearTempValue();
        verify(certificateAccessor).save();

        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.defaultValue.version")).isEqualTo(3);
        assertThat(model.<List>get("$.defaultValue.currentProperties")).hasSize(1);
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].key")).isEqualTo("alias");
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].propertyValueInfo.value")).isEqualTo("comserver");
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].propertyTypeInfo.propertyValuesResource.possibleValuesURI"))
                .isEqualTo("http://localhost:9998/securityaccessors/certificates/aliases");
        assertThat(model.<List>get("$.defaultValue.tempProperties")).hasSize(1);
        assertThat(model.<String>get("$.defaultValue.tempProperties[0].key")).isEqualTo("alias");
        assertThat(model.<JSONObject>get("$.defaultValue.tempProperties[0].propertyValueInfo")).isEmpty();
        assertThat(model.<String>get("$.defaultValue.tempProperties[0].propertyTypeInfo.propertyValuesResource.possibleValuesURI"))
                .isEqualTo("http://localhost:9998/securityaccessors/certificates/aliases");
    }

    @Test
    public void testUpdateCertificateAccessorTypeWithDefaultValues_OnlyChangeActual() throws Exception {
        when(securityManagementService.findAndLockSecurityAccessorType(2, 1)).thenReturn(Optional.of(certificateAccessorType));
        when(certificateAccessorType.isManagedCentrally()).thenReturn(true);
        when(securityManagementService.lockDefaultValues(certificateAccessorType, 3)).thenReturn(Optional.of(certificateAccessor));
        when(certificateAccessor.getTempValue()).thenReturn(Optional.of(tempClientCertificateWrapper));

        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = 2;
        info.version = 1;
        info.description = DESCRIPTION_W;
        info.name = NAME_W;
        SecurityAccessorTypeUpdater updater = mock(SecurityAccessorTypeUpdater.class);
        when(certificateAccessorType.startUpdate()).thenReturn(updater);
        when(updater.complete()).thenReturn(certificateAccessorType);

        info.defaultValue = createDefaultValue(3L, "newcomserver", "newcomserver");

        Response response = target("/securityaccessors/2").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(certificateAccessorType).startUpdate();
        verify(updater).description(info.description);
        verify(updater).name(info.name);
        verify(updater).complete();
        verify(certificateAccessor).setActualValue(tempClientCertificateWrapper);
        verify(certificateAccessor).save();

        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.defaultValue.version")).isEqualTo(3);
        assertThat(model.<List>get("$.defaultValue.currentProperties")).hasSize(1);
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].key")).isEqualTo("alias");
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].propertyValueInfo.value")).isEqualTo("newcomserver");
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].propertyTypeInfo.propertyValuesResource.possibleValuesURI"))
                .isEqualTo("http://localhost:9998/securityaccessors/certificates/aliases");
        assertThat(model.<List>get("$.defaultValue.tempProperties")).hasSize(1);
        assertThat(model.<String>get("$.defaultValue.tempProperties[0].key")).isEqualTo("alias");
        assertThat(model.<String>get("$.defaultValue.tempProperties[0].propertyValueInfo.value")).isEqualTo("newcomserver");
        assertThat(model.<String>get("$.defaultValue.tempProperties[0].propertyTypeInfo.propertyValuesResource.possibleValuesURI"))
                .isEqualTo("http://localhost:9998/securityaccessors/certificates/aliases");
    }

    @Test
    public void testUpdateCertificateAccessorTypeWithDefaultValues_UnchangedValues() throws Exception {
        when(securityManagementService.findAndLockSecurityAccessorType(2, 1)).thenReturn(Optional.of(certificateAccessorType));
        when(certificateAccessorType.isManagedCentrally()).thenReturn(true);
        when(securityManagementService.lockDefaultValues(certificateAccessorType, 3)).thenReturn(Optional.of(certificateAccessor));

        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = 2;
        info.version = 1;
        info.description = DESCRIPTION_W;
        info.name = NAME_W;
        SecurityAccessorTypeUpdater updater = mock(SecurityAccessorTypeUpdater.class);
        when(certificateAccessorType.startUpdate()).thenReturn(updater);
        when(updater.complete()).thenReturn(certificateAccessorType);

        info.defaultValue = createDefaultValue(3L, "comserver", null);

        Response response = target("/securityaccessors/2").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(certificateAccessorType).startUpdate();
        verify(updater).description(info.description);
        verify(updater).name(info.name);
        verify(updater).complete();
        verify(certificateAccessor, never()).setActualValue(any(CertificateWrapper.class));
        verify(certificateAccessor, never()).setTempValue(any(CertificateWrapper.class));
        verify(certificateAccessor, never()).clearActualValue();
        verify(certificateAccessor, never()).clearTempValue();
        verify(certificateAccessor, never()).save();

        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.defaultValue.version")).isEqualTo(3);
        assertThat(model.<List>get("$.defaultValue.currentProperties")).hasSize(1);
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].key")).isEqualTo("alias");
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].propertyValueInfo.value")).isEqualTo("comserver");
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].propertyTypeInfo.propertyValuesResource.possibleValuesURI"))
                .isEqualTo("http://localhost:9998/securityaccessors/certificates/aliases");
        assertThat(model.<List>get("$.defaultValue.tempProperties")).hasSize(1);
        assertThat(model.<String>get("$.defaultValue.tempProperties[0].key")).isEqualTo("alias");
        assertThat(model.<JSONObject>get("$.defaultValue.tempProperties[0].propertyValueInfo")).isEmpty();
        assertThat(model.<String>get("$.defaultValue.tempProperties[0].propertyTypeInfo.propertyValuesResource.possibleValuesURI"))
                .isEqualTo("http://localhost:9998/securityaccessors/certificates/aliases");
    }

    @Test
    public void testUpdateCertificateAccessorTypeWithDefaultValues_UnsetActualAndChangeTemp() throws Exception {
        when(securityManagementService.findAndLockSecurityAccessorType(2, 1)).thenReturn(Optional.of(certificateAccessorType));
        when(certificateAccessorType.isManagedCentrally()).thenReturn(true);
        when(securityManagementService.lockDefaultValues(certificateAccessorType, 3)).thenReturn(Optional.of(certificateAccessor));
        when(certificateAccessor.getTempValue()).thenReturn(Optional.of(tempClientCertificateWrapper));

        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = 2;
        info.version = 1;
        info.description = DESCRIPTION_W;
        info.name = NAME_W;
        SecurityAccessorTypeUpdater updater = mock(SecurityAccessorTypeUpdater.class);
        when(certificateAccessorType.startUpdate()).thenReturn(updater);
        when(updater.complete()).thenReturn(certificateAccessorType);

        info.defaultValue = createDefaultValue(3L, null, "comserver");

        Response response = target("/securityaccessors/2").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        verify(certificateAccessor, never()).setActualValue(any(CertificateWrapper.class));
        verify(certificateAccessor, never()).setTempValue(any(CertificateWrapper.class));
        verify(certificateAccessor, never()).clearActualValue();
        verify(certificateAccessor, never()).clearTempValue();
        verify(certificateAccessor, never()).save();

        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Boolean>get("$.success")).isFalse();
        assertThat(model.<String>get("$.errors[0].msg")).isEqualTo("This field is required.");
        assertThat(model.<String>get("$.errors[0].id")).isEqualTo("defaultValue.currentProperties.alias");
    }

    @Test
    public void testUpdateCertificateAccessorTypeWithDefaultValuesFailedDueToMissingValue() throws Exception {
        when(securityManagementService.findAndLockSecurityAccessorType(2, 1)).thenReturn(Optional.of(certificateAccessorType));
        when(certificateAccessorType.isManagedCentrally()).thenReturn(true);

        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = 2;
        info.version = 1;
        info.description = DESCRIPTION_W;
        info.name = NAME_W;
        SecurityAccessorTypeUpdater updater = mock(SecurityAccessorTypeUpdater.class);
        when(certificateAccessorType.startUpdate()).thenReturn(updater);
        when(updater.complete()).thenReturn(certificateAccessorType);

        Response response = target("/securityaccessors/2").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        verify(certificateAccessor, never()).setActualValue(any(CertificateWrapper.class));
        verify(certificateAccessor, never()).setTempValue(any(CertificateWrapper.class));
        verify(certificateAccessor, never()).clearActualValue();
        verify(certificateAccessor, never()).clearTempValue();
        verify(certificateAccessor, never()).save();

        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Boolean>get("$.success")).isFalse();
        assertThat(model.<String>get("$.errors[0].msg")).isEqualTo("This field is required");
        assertThat(model.<String>get("$.errors[0].id")).isEqualTo("defaultValue");
    }

    @Test
    public void testUpdateCertificateAccessorTypeWithDefaultValuesFailedDueToConflict() throws Exception {
        when(securityManagementService.findAndLockSecurityAccessorType(2, 1)).thenReturn(Optional.of(certificateAccessorType));
        when(certificateAccessorType.isManagedCentrally()).thenReturn(true);
        when(securityManagementService.lockDefaultValues(certificateAccessorType, 2)).thenReturn(Optional.empty());
        when(securityManagementService.getDefaultValues(certificateAccessorType)).thenReturn(Optional.of(certificateAccessor));

        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = 2;
        info.version = 1;
        info.description = DESCRIPTION_W;
        info.name = NAME_W;
        SecurityAccessorTypeUpdater updater = mock(SecurityAccessorTypeUpdater.class);
        when(certificateAccessorType.startUpdate()).thenReturn(updater);
        when(updater.complete()).thenReturn(certificateAccessorType);

        info.defaultValue = createDefaultValue(2L, "newcomserver", "comserver");

        Response response = target("/securityaccessors/2").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());

        verify(certificateAccessor, never()).setActualValue(any(CertificateWrapper.class));
        verify(certificateAccessor, never()).setTempValue(any(CertificateWrapper.class));
        verify(certificateAccessor, never()).clearActualValue();
        verify(certificateAccessor, never()).clearTempValue();
        verify(certificateAccessor, never()).save();

        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<String>get("$.error")).contains(NAME_W + " has changed");
        assertThat(model.<Number>get("$.version")).isEqualTo(3);
        assertThat(model.<Number>get("$.parent.id")).isEqualTo(2);
        assertThat(model.<Number>get("$.parent.version")).isEqualTo(1);
    }

    @Test
    public void testClearTempValue() throws Exception {
        when(securityManagementService.findAndLockSecurityAccessorType(2, 1)).thenReturn(Optional.of(certificateAccessorType));
        when(certificateAccessorType.isManagedCentrally()).thenReturn(true);
        when(securityManagementService.lockDefaultValues(certificateAccessorType, 3)).thenReturn(Optional.of(certificateAccessor));
        when(certificateAccessor.getTempValue()).thenReturn(Optional.of(tempClientCertificateWrapper));

        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = 2;
        info.version = 1;
        info.defaultValue = createDefaultValue(3L, null, null);

        Response response = target("/securityaccessors/2/tempvalue").request().method("DELETE", Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(certificateAccessor).clearTempValue();

        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.defaultValue.version")).isEqualTo(3);
        assertThat(model.<List>get("$.defaultValue.currentProperties")).hasSize(1);
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].key")).isEqualTo("alias");
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].propertyValueInfo.value")).isEqualTo("comserver");
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].propertyTypeInfo.propertyValuesResource.possibleValuesURI"))
                .isEqualTo("http://localhost:9998/securityaccessors/certificates/aliases");
        assertThat(model.<List>get("$.defaultValue.tempProperties")).hasSize(1);
        assertThat(model.<String>get("$.defaultValue.tempProperties[0].key")).isEqualTo("alias");
        assertThat(model.<JSONObject>get("$.defaultValue.tempProperties[0].propertyValueInfo")).isEmpty();
        assertThat(model.<String>get("$.defaultValue.tempProperties[0].propertyTypeInfo.propertyValuesResource.possibleValuesURI"))
                .isEqualTo("http://localhost:9998/securityaccessors/certificates/aliases");
    }

    @Test
    public void testClearTempValueFailed() throws IOException {
        when(securityManagementService.findAndLockSecurityAccessorType(2, 1)).thenReturn(Optional.of(certificateAccessorType));
        when(certificateAccessorType.isManagedCentrally()).thenReturn(true);
        when(securityManagementService.lockDefaultValues(certificateAccessorType, 2)).thenReturn(Optional.empty());
        when(securityManagementService.getDefaultValues(certificateAccessorType)).thenReturn(Optional.of(certificateAccessor));

        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = 2;
        info.version = 1;
        info.defaultValue = createDefaultValue(2L, null, null);

        Response response = target("/securityaccessors/2/tempvalue").request().method("DELETE", Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());

        verify(certificateAccessor, never()).clearTempValue();

        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<String>get("$.error")).contains(NAME_W + " has changed");
        assertThat(model.<Number>get("$.version")).isEqualTo(3);
        assertThat(model.<Number>get("$.parent.id")).isEqualTo(2);
        assertThat(model.<Number>get("$.parent.version")).isEqualTo(1);
    }

    @Test
    public void testSwapValues() throws Exception {
        when(securityManagementService.findAndLockSecurityAccessorType(2, 1)).thenReturn(Optional.of(certificateAccessorType));
        when(certificateAccessorType.isManagedCentrally()).thenReturn(true);
        when(securityManagementService.lockDefaultValues(certificateAccessorType, 3)).thenReturn(Optional.of(certificateAccessor));
        when(certificateAccessor.getTempValue()).thenReturn(Optional.of(tempClientCertificateWrapper));

        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = 2;
        info.version = 1;
        info.defaultValue = createDefaultValue(3L, null, null);

        Response response = target("/securityaccessors/2/swap").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(certificateAccessor).swapValues();

        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.defaultValue.version")).isEqualTo(3);
        assertThat(model.<List>get("$.defaultValue.currentProperties")).hasSize(1);
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].key")).isEqualTo("alias");
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].propertyValueInfo.value")).isEqualTo("newcomserver");
        assertThat(model.<String>get("$.defaultValue.currentProperties[0].propertyTypeInfo.propertyValuesResource.possibleValuesURI"))
                .isEqualTo("http://localhost:9998/securityaccessors/certificates/aliases");
        assertThat(model.<List>get("$.defaultValue.tempProperties")).hasSize(1);
        assertThat(model.<String>get("$.defaultValue.tempProperties[0].key")).isEqualTo("alias");
        assertThat(model.<String>get("$.defaultValue.tempProperties[0].propertyValueInfo.value")).isEqualTo("comserver");
        assertThat(model.<String>get("$.defaultValue.tempProperties[0].propertyTypeInfo.propertyValuesResource.possibleValuesURI"))
                .isEqualTo("http://localhost:9998/securityaccessors/certificates/aliases");
    }

    @Test
    public void testSwapValuesFailed() throws IOException {
        when(securityManagementService.findAndLockSecurityAccessorType(2, 1)).thenReturn(Optional.of(certificateAccessorType));
        when(certificateAccessorType.isManagedCentrally()).thenReturn(true);
        when(securityManagementService.lockDefaultValues(certificateAccessorType, 2)).thenReturn(Optional.empty());
        when(securityManagementService.getDefaultValues(certificateAccessorType)).thenReturn(Optional.of(certificateAccessor));
        when(certificateAccessor.getTempValue()).thenReturn(Optional.of(tempClientCertificateWrapper));

        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = 2;
        info.version = 1;
        info.defaultValue = createDefaultValue(2L, null, null);

        Response response = target("/securityaccessors/2/swap").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());

        verify(certificateAccessor, never()).swapValues();

        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<String>get("$.error")).contains(NAME_W + " has changed");
        assertThat(model.<Number>get("$.version")).isEqualTo(3);
        assertThat(model.<Number>get("$.parent.id")).isEqualTo(2);
        assertThat(model.<Number>get("$.parent.version")).isEqualTo(1);
    }

    @Test
    public void testDeleteSecurityAccessorTypeWithDefaultValues() throws Exception {
        when(securityManagementService.findAndLockSecurityAccessorType(2, 1)).thenReturn(Optional.of(certificateAccessorType));
        when(certificateAccessorType.isManagedCentrally()).thenReturn(true);
        when(securityManagementService.lockDefaultValues(certificateAccessorType, 3)).thenReturn(Optional.of(certificateAccessor));

        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = 2;
        info.version = 1;
        info.defaultValue = createDefaultValue(3L, null, null);

        Response response = target("/securityaccessors/2").request().method("DELETE", Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());

        verify(certificateAccessor).delete();
        verify(certificateAccessorType).delete();
    }

    @Test
    public void testAliasSource() throws Exception {
        CertificateWrapper cert1 = mock(CertificateWrapper.class);
        CertificateWrapper cert2 = mock(CertificateWrapper.class);
        CertificateWrapper cert3 = mock(CertificateWrapper.class);

        when(cert1.getWrapperStatus()).thenReturn(CertificateWrapperStatus.NATIVE);
        when(cert2.getWrapperStatus()).thenReturn(CertificateWrapperStatus.OBSOLETE);
        when(cert3.getWrapperStatus()).thenReturn(CertificateWrapperStatus.REVOKED);
        when(cert1.getAlias()).thenReturn("al1");
        when(cert2.getAlias()).thenReturn("al2");
        when(cert3.getAlias()).thenReturn("al3");

        Finder<CertificateWrapper> finder = mockFinder(Arrays.asList(cert1, cert2, cert3));
        when(securityManagementService.getAliasesByFilter(any(SecurityManagementService.AliasSearchFilter.class)))
                .thenReturn(finder);

        Response response = target("/securityaccessors/certificates/aliases").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        List<String> aliases = model.<JSONArray>get("aliases").stream()
                .map(o -> (String) (((JSONObject) o).get("alias")))
                .sorted()
                .collect(Collectors.toList());

        assertThat(aliases).hasSize(2).contains("al1", "al2");
    }

    @Test
    public void testDeleteWithDefaultValuesFailed() throws IOException {
        when(securityManagementService.findAndLockSecurityAccessorType(2, 1)).thenReturn(Optional.of(certificateAccessorType));
        when(certificateAccessorType.isManagedCentrally()).thenReturn(true);
        when(securityManagementService.lockDefaultValues(certificateAccessorType, 2)).thenReturn(Optional.empty());
        when(securityManagementService.getDefaultValues(certificateAccessorType)).thenReturn(Optional.of(certificateAccessor));

        SecurityAccessorTypeInfo info = new SecurityAccessorTypeInfo();
        info.id = 2;
        info.version = 1;
        info.defaultValue = createDefaultValue(2L, null, null);

        Response response = target("/securityaccessors/2").request().method("DELETE", Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());

        assertThat(model.<String>get("$.error")).contains(NAME_W + " has changed");
        assertThat(model.<Number>get("$.version")).isEqualTo(3);
        assertThat(model.<Number>get("$.parent.id")).isEqualTo(2);
        assertThat(model.<Number>get("$.parent.version")).isEqualTo(1);

        verify(certificateAccessor, never()).delete();
        verify(certificateAccessorType, never()).delete();
    }

    private SecurityAccessorInfo createDefaultValue(Long version, String activeAlias, String passiveAlias) {
        SecurityAccessorInfo info = new SecurityAccessorInfo();
        info.version = version;
        info.currentProperties = Collections.singletonList(createPropertyInfo("alias", activeAlias));
        info.tempProperties = Collections.singletonList(createPropertyInfo("alias", passiveAlias));
        return info;
    }

    private PropertyInfo createPropertyInfo(String key, String value) {
        PropertyInfo propertyInfo = new PropertyInfo();
        propertyInfo.key = key;
        propertyInfo.propertyValueInfo = new PropertyValueInfo<>(value, null);
        return propertyInfo;
    }

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
        when(securityAccessorType.getPurpose()).thenReturn(SecurityAccessorType.Purpose.DEVICE_OPERATIONS);
        when(securityAccessorType.getKeyType()).thenReturn(keyType);
        when(securityAccessorType.getKeyPurpose()).thenReturn(new KeyPurpose() {
            @Override
            public String getId() {
                return "KEY";
            }

            @Override
            public String getName() {
                return "NAME";
            }
        });
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
        when(securityAccessorType.getPurpose()).thenReturn(SecurityAccessorType.Purpose.DEVICE_OPERATIONS);
        when(securityAccessorType.getKeyPurpose()).thenReturn(new KeyPurpose() {
            @Override
            public String getId() {
                return "KEY";
            }

            @Override
            public String getName() {
                return "NAME";
            }
        });
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

    private SecurityAccessor<CertificateWrapper> mockClientCertificateAccessor(long version, List<PropertySpec> propertySpecs,
                                                                               SecurityAccessorType certificateKeyAccessorType,
                                                                               CertificateWrapper clientCertificateWrapper) {
        SecurityAccessor<CertificateWrapper> securityAccessor1 = mock(SecurityAccessor.class);
        when(securityAccessor1.getVersion()).thenReturn(version);
        when(securityAccessor1.getTempValue()).thenReturn(Optional.empty());
        when(securityAccessor1.getActualValue()).thenReturn(Optional.of(clientCertificateWrapper));
        doAnswer(invocation -> {
            when(securityAccessor1.getTempValue()).thenReturn(Optional.ofNullable(invocation.getArgumentAt(0, CertificateWrapper.class)));
            return null;
        }).when(securityAccessor1).setTempValue(any(CertificateWrapper.class));
        doAnswer(invocation -> {
            when(securityAccessor1.getActualValue()).thenReturn(Optional.ofNullable(invocation.getArgumentAt(0, CertificateWrapper.class)));
            return null;
        }).when(securityAccessor1).setActualValue(any(CertificateWrapper.class));
        doAnswer(invocation -> {
            when(securityAccessor1.getTempValue()).thenReturn(Optional.empty());
            return null;
        }).when(securityAccessor1).clearTempValue();
        doAnswer(invocation -> {
            Optional<CertificateWrapper> temp = securityAccessor1.getTempValue();
            Optional<CertificateWrapper> actual = securityAccessor1.getActualValue();
            when(securityAccessor1.getTempValue()).thenReturn(actual);
            when(securityAccessor1.getActualValue()).thenReturn(temp);
            return null;
        }).when(securityAccessor1).swapValues();
        when(securityAccessor1.getSecurityAccessorType()).thenReturn(certificateKeyAccessorType);
        when(securityAccessor1.getPropertySpecs()).thenReturn(propertySpecs);
        return securityAccessor1;
    }
}
