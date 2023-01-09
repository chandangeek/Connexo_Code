/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.device.configuration.rest.SecurityAccessorInfo;

import com.jayway.jsonpath.JsonModel;
import net.minidev.json.JSONObject;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * >> Setup description <<
 * All tests revolve around a device with mRID BVN001.
 * The device has 1 security accessor for a symmetric key and 1 for a client certificate.
 * The symmetric key accessor has KeyAccessorType 'aes' with id 111L
 * The certificate accessor has KeyAccessorType 'tls1' with id 222L
 *
 * A CertificateWrapper exists, name 'comserver', whether or not it exists on the device depends on the test in question.
 * A 2nd CertificateWrapper exists, named 'newcomserver', whether or not it exists on the device's temp value depends on the test in question.
 * A symmetric key accessor exists, whether or not it is linked to the device depends on the test in question.
 */
public class SecurityAccessorResourceTest extends DeviceDataRestApplicationJerseyTest {

    private SecurityAccessorType symmetricKeyAccessorType;
    private List<PropertySpec> symmetricKeyPropertySpecs;
    private List<PropertySpec> certificatePropertySpecs;
    private SecurityAccessorType certificateKeyAccessorType;
    private DeviceType deviceType;
    private SecurityAccessor clientCertificateAccessor;
    private SecurityAccessor symmetrickeyAccessor;
    private SymmetricKeyWrapper actualSymmetricKeyWrapper;
    private ClientCertificateWrapper actualClientCertificateWrapper;
    private ClientCertificateWrapper tempClientCertificateWrapper;
    private Device device;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        symmetricKeyPropertySpecs = Collections.singletonList(mockPropertySpec("key"));
        certificatePropertySpecs = Collections.singletonList(mockPropertySpec("alias"));

        symmetricKeyAccessorType = mockSymmetricKeyAccessorType(111L, "aes");
        certificateKeyAccessorType = mockCertificateKeyAccessorType(222L, "tls1");

        actualClientCertificateWrapper = mockClientCertificateWrapper(certificatePropertySpecs, "alias", "comserver", "myAlias");
        clientCertificateAccessor = mockClientCertificateAccessor(certificatePropertySpecs, actualClientCertificateWrapper);
        when(clientCertificateAccessor.isEditable()).thenReturn(true);

        actualSymmetricKeyWrapper = mockSymmetricKeyWrapper(symmetricKeyPropertySpecs, "key", "b21nLEkgY2FuJ3QgYmVsaWV2ZSB5b3UgZGVjb2RlZCB0aGlz");
        symmetrickeyAccessor = mockSymmetricKeyAccessor(actualSymmetricKeyWrapper, null);

        deviceType = mock(DeviceType.class);
        when(deviceType.getSecurityAccessorTypes()).thenReturn(Arrays.asList(symmetricKeyAccessorType, certificateKeyAccessorType));
        when(deviceType.getDefaultKeyOfSecurityAccessorType(any(SecurityAccessorType.class))).thenReturn(Optional.of("ABCDABCD"));
        device = mock(Device.class);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getSecurityAccessors()).thenReturn(Arrays.asList(clientCertificateAccessor, symmetrickeyAccessor));
        when(device.getSecurityAccessor(any(SecurityAccessorType.class))).thenReturn(Optional.empty());
        when(device.getSecurityAccessor(certificateKeyAccessorType)).thenReturn(Optional.ofNullable(clientCertificateAccessor));
        when(device.getSecurityAccessor(symmetricKeyAccessorType)).thenReturn(Optional.ofNullable(symmetrickeyAccessor));
        when(deviceService.findDeviceByName("BVN001")).thenReturn(Optional.of(device));

        when(securityManagementService.getPropertySpecs(symmetricKeyAccessorType)).thenReturn(symmetricKeyPropertySpecs);
        when(securityManagementService.getPropertySpecs(certificateKeyAccessorType)).thenReturn(certificatePropertySpecs);
        when(securityManagementService.findCertificateWrapper(anyString())).thenReturn(Optional.empty());
        when(securityManagementService.findCertificateWrapper("comserver")).thenReturn(Optional.of(actualClientCertificateWrapper));
        tempClientCertificateWrapper = mockClientCertificateWrapper(certificatePropertySpecs, "alias", "newcomserver", "myAlias");
        when(securityManagementService.findCertificateWrapper("newcomserver")).thenReturn(Optional.of(tempClientCertificateWrapper));
        when(deviceService.findAndLockKeyAccessorByIdAndVersion(any(Device.class), any(SecurityAccessorType.class), anyLong())).thenReturn(Optional.empty());
        when(deviceService.findAndLockKeyAccessorByIdAndVersion(device, symmetricKeyAccessorType, 11L)).thenReturn(Optional.of(symmetrickeyAccessor));
        when(deviceService.findAndLockKeyAccessorByIdAndVersion(device, certificateKeyAccessorType, 22L)).thenReturn(Optional.of(clientCertificateAccessor));

        Finder<CertificateWrapper> finder = mockFinder(Collections.singletonList(actualClientCertificateWrapper));
        when(securityManagementService.getAliasesByFilter(any(SecurityManagementService.AliasSearchFilter.class))).thenReturn(finder);
    }

    @Test
    public void testGetCertificatesWithoutTempValue() throws Exception {
        when(clientCertificateAccessor.isEditable()).thenReturn(false);

        Response response = target("/devices/BVN001/securityaccessors/certificates").request().get();
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<List>get("$.certificates")).hasSize(1);
        assertThat(jsonModel.<String>get("$.certificates[0].name")).isEqualTo("tls1");
        assertThat(jsonModel.<List>get("$.certificates[0].currentProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.certificates[0].currentProperties[0].key")).isEqualTo("alias");
        assertThat(jsonModel.<String>get("$.certificates[0].currentProperties[0].propertyValueInfo.value")).isEqualTo("comserver");
        assertThat(jsonModel.<String>get("$.certificates[0].currentProperties[0].propertyTypeInfo.propertyValuesResource.possibleValuesURI")).isEqualTo("http://localhost:9998/devices/x/securityaccessors/certificates/aliases");
        assertThat(jsonModel.<List>get("$.certificates[0].tempProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.certificates[0].tempProperties[0].key")).isEqualTo("alias");
        assertThat(jsonModel.<JSONObject>get("$.certificates[0].tempProperties[0].propertyValueInfo")).isEmpty();
        assertThat(jsonModel.<Boolean>get("$.certificates[0].editable")).isFalse();
    }

    @Test
    public void testAliasPropertyTypeAheadFiltering() throws Exception {
        SecurityAccessorInfo response = target("/devices/BVN001/securityaccessors/certificates/222").request().get(SecurityAccessorInfo.class);
        URI uri = new URI(response.currentProperties.get(0).propertyTypeInfo.propertyValuesResource.possibleValuesURI);
        Response response1 = target(uri.getPath())
                .queryParam("alias", "com")
                .request()
                .get();
        ArgumentCaptor<SecurityManagementService.AliasSearchFilter> captor = ArgumentCaptor.forClass(SecurityManagementService.AliasSearchFilter.class);
        verify(securityManagementService, times(1)).getAliasesByFilter(captor.capture());
        assertThat(captor.getValue().alias).isEqualTo("*com*");
        assertThat(captor.getValue().trustStore).isNull();
    }

    @Test
    public void testAliasPropertyTypeAheadFilteringWithWildCard() throws Exception {
        SecurityAccessorInfo response = target("/devices/BVN001/securityaccessors/certificates/222").request().get(SecurityAccessorInfo.class);
        URI uri = new URI(response.currentProperties.get(0).propertyTypeInfo.propertyValuesResource.possibleValuesURI);
        Response response1 = target(uri.getPath())
                .queryParam("alias", "com*")
                .request()
                .get();
        ArgumentCaptor<SecurityManagementService.AliasSearchFilter> captor = ArgumentCaptor.forClass(SecurityManagementService.AliasSearchFilter.class);
        verify(securityManagementService, times(1)).getAliasesByFilter(captor.capture());
        assertThat(captor.getValue().alias).isEqualTo("com*");
        assertThat(captor.getValue().trustStore).isNull();
    }

    @Test
    public void testAliasPropertyTypeAheadFilteringWithAliasAndTrustStore() throws Exception {
        SecurityAccessorInfo response = target("/devices/BVN001/securityaccessors/certificates/222").request().get(SecurityAccessorInfo.class);
        URI uri = new URI(response.currentProperties.get(0).propertyTypeInfo.propertyValuesResource.possibleValuesURI);
        TrustStore trustStore = mock(TrustStore.class);
        when(securityManagementService.findTrustStore(14L)).thenReturn(Optional.ofNullable(trustStore));
        Response response1 = target(uri.getPath())
                .queryParam("alias", "com*")
                .queryParam("trustStore", 14L)
                .request()
                .get();
        ArgumentCaptor<SecurityManagementService.AliasSearchFilter> captor = ArgumentCaptor.forClass(SecurityManagementService.AliasSearchFilter.class);
        verify(securityManagementService, times(1)).getAliasesByFilter(captor.capture());
        assertThat(captor.getValue().alias).isEqualTo("com*");
        assertThat(captor.getValue().trustStore).isEqualTo(trustStore);
        JsonModel jsonModel = JsonModel.create((InputStream)response1.getEntity());
        System.out.println(jsonModel.toJson());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.aliases[0].alias")).isEqualTo("myAlias");
    }

    @Test
    public void testAliasPropertyTypeAheadFilteringEmptyAlias() throws Exception {
        SecurityAccessorInfo response = target("/devices/BVN001/securityaccessors/certificates/222").request().get(SecurityAccessorInfo.class);
        URI uri = new URI(response.currentProperties.get(0).propertyTypeInfo.propertyValuesResource.possibleValuesURI);
        TrustStore trustStore = mock(TrustStore.class);
        when(securityManagementService.findTrustStore(16L)).thenReturn(Optional.ofNullable(trustStore));

        Response response1 = target(uri.getPath())
                .queryParam("alias", "")
                .queryParam("trustStore", 16L)
                .request()
                .get();
        ArgumentCaptor<SecurityManagementService.AliasSearchFilter> captor = ArgumentCaptor.forClass(SecurityManagementService.AliasSearchFilter.class);
        verify(securityManagementService, times(1)).getAliasesByFilter(captor.capture());
        assertThat(captor.getValue().alias).isEqualTo("*");
        assertThat(captor.getValue().trustStore).isEqualTo(trustStore);
    }

    /**
     * 1st security accessor type (tls1) has a security accessor (without temp value)
     * 2nd security accessor type (tls2) does not have a security accessor at all.
     * @throws Exception
     */
    @Test
    public void testGetCertificatesWithoutValueForKeyAccessorType() throws Exception {

        SecurityAccessorType securityAccessorType = mockCertificateKeyAccessorType(2L, "tls2");
        when(deviceType.getSecurityAccessorTypes()).thenReturn(Arrays.asList(certificateKeyAccessorType, symmetricKeyAccessorType, securityAccessorType));
        PropertySpec propertySpec = mockPropertySpec("alias");
        when(securityManagementService.getPropertySpecs(securityAccessorType)).thenReturn(Arrays.asList(propertySpec));

        Response response = target("/devices/BVN001/securityaccessors/certificates").request().get();
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<List>get("$.certificates")).hasSize(2);

        assertThat(jsonModel.<String>get("$.certificates[0].name")).isEqualTo("tls1");
        assertThat(jsonModel.<List>get("$.certificates[0].currentProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.certificates[0].currentProperties[0].key")).isEqualTo("alias");
        assertThat(jsonModel.<String>get("$.certificates[0].currentProperties[0].propertyValueInfo.value")).isEqualTo("comserver");
        assertThat(jsonModel.<List>get("$.certificates[0].tempProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.certificates[0].tempProperties[0].key")).isEqualTo("alias");
        assertThat(jsonModel.<JSONObject>get("$.certificates[0].tempProperties[0].propertyValueInfo")).isEmpty();
        assertThat(jsonModel.<Boolean>get("$.certificates[0].editable")).isTrue();

        assertThat(jsonModel.<String>get("$.certificates[1].name")).isEqualTo("tls2");
        assertThat(jsonModel.<List>get("$.certificates[1].currentProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.certificates[1].currentProperties[0].key")).isEqualTo("alias");
        assertThat(jsonModel.<JSONObject>get("$.certificates[1].currentProperties[0].propertyValueInfo")).isEmpty();
        assertThat(jsonModel.<List>get("$.certificates[1].tempProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.certificates[1].tempProperties[0].key")).isEqualTo("alias");
        assertThat(jsonModel.<JSONObject>get("$.certificates[1].tempProperties[0].propertyValueInfo")).isEmpty();
        assertThat(jsonModel.<Boolean>get("$.certificates[1].editable")).isTrue();
    }

    @Test
    public void testGetKeysWithoutTempValueWithAllPermissions() throws Exception {
        Response response = target("/devices/BVN001/securityaccessors/keys").request().get();
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<List>get("$.keys")).hasSize(1);
        assertThat(jsonModel.<Boolean>get("$.keys[0].hasTempValue")).isFalse();
        assertThat(jsonModel.<List>get("$.keys[0].currentProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.keys[0].currentProperties[0].key")).isEqualTo("key");
        assertThat(jsonModel.<Boolean>get("$.keys[0].currentProperties[0].propertyValueInfo.propertyHasValue")).isTrue();
        assertThat(jsonModel.<String>get("$.keys[0].currentProperties[0].propertyValueInfo.value")).isEqualTo("b21nLEkgY2FuJ3QgYmVsaWV2ZSB5b3UgZGVjb2RlZCB0aGlz");
        assertThat(jsonModel.<List>get("$.keys[0].tempProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.keys[0].tempProperties[0].key")).isEqualTo("key");
        assertThat(jsonModel.<Boolean>get("$.keys[0].editable")).isTrue();
    }

    @Test
    public void testGetKeysWithoutTempValueWithViewPermissions() throws Exception {
        when(symmetricKeyAccessorType.isCurrentUserAllowedToViewProperties("MDC")).thenReturn(true);
        when(symmetricKeyAccessorType.isCurrentUserAllowedToEditProperties("MDC")).thenReturn(false);

        Response response = target("/devices/BVN001/securityaccessors/keys").request().get();
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<List>get("$.keys")).hasSize(1);
        assertThat(jsonModel.<Boolean>get("$.keys[0].hasTempValue")).isFalse();
        assertThat(jsonModel.<List>get("$.keys[0].currentProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.keys[0].currentProperties[0].key")).isEqualTo("key");
        assertThat(jsonModel.<Boolean>get("$.keys[0].currentProperties[0].propertyValueInfo.propertyHasValue")).isTrue();
        assertThat(jsonModel.<String>get("$.keys[0].currentProperties[0].propertyValueInfo.value")).isNull();
        assertThat(jsonModel.<List>get("$.keys[0].tempProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.keys[0].tempProperties[0].key")).isEqualTo("key");
    }

    @Test
    public void testGetKeysWithoutTempValueWithoutPermissions() throws Exception {
        when(symmetricKeyAccessorType.isCurrentUserAllowedToViewProperties("MDC")).thenReturn(false);
        when(symmetricKeyAccessorType.isCurrentUserAllowedToEditProperties("MDC")).thenReturn(false);

        Response response = target("/devices/BVN001/securityaccessors/keys").request().get();
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<List>get("$.keys")).hasSize(1);
        assertThat(jsonModel.<Boolean>get("$.keys[0].hasTempValue")).isFalse();
        assertThat(jsonModel.<List>get("$.keys[0].currentProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.keys[0].currentProperties[0].key")).isEqualTo("key");
        assertThat(jsonModel.<Boolean>get("$.keys[0].currentProperties[0].propertyValueInfo.propertyHasValue")).isNull();
        assertThat(jsonModel.<String>get("$.keys[0].currentProperties[0].propertyValueInfo.value")).isNull();
        assertThat(jsonModel.<List>get("$.keys[0].tempProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.keys[0].tempProperties[0].key")).isEqualTo("key");
    }

    @Test
    public void testGetKeysWithTempValue() throws Exception {
        SymmetricKeyWrapper tempSymmetricKeyWrapper = mockSymmetricKeyWrapper(symmetricKeyPropertySpecs, "key", "oldtempvalue");
        symmetrickeyAccessor = mockSymmetricKeyAccessor(actualSymmetricKeyWrapper, tempSymmetricKeyWrapper);
        when(device.getSecurityAccessor(symmetricKeyAccessorType)).thenReturn(Optional.of(symmetrickeyAccessor));
        when(deviceService.findAndLockKeyAccessorByIdAndVersion(device, symmetricKeyAccessorType, 11L)).thenReturn(Optional.of(symmetrickeyAccessor));

        Response response = target("/devices/BVN001/securityaccessors/keys").request().get();
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<List>get("$.keys")).hasSize(1);
        assertThat(jsonModel.<Boolean>get("$.keys[0].hasTempValue")).isTrue();
        assertThat(jsonModel.<List>get("$.keys[0].currentProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.keys[0].currentProperties[0].key")).isEqualTo("key");
        assertThat(jsonModel.<String>get("$.keys[0].currentProperties[0].propertyValueInfo.value")).isEqualTo("b21nLEkgY2FuJ3QgYmVsaWV2ZSB5b3UgZGVjb2RlZCB0aGlz");
        assertThat(jsonModel.<List>get("$.keys[0].tempProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.keys[0].tempProperties[0].key")).isEqualTo("key");
        assertThat(jsonModel.<String>get("$.keys[0].tempProperties[0].propertyValueInfo.value")).isEqualTo("oldtempvalue");
    }

    @Test
    public void setActualAndTempOnExistingKeyAccessorWithoutTemp() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.version = 11L;
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("key", "actualKey"));
        securityAccessorInfo.tempProperties = Arrays.asList(createPropertyInfo("key", "tempKey"));

        SymmetricKeyWrapper symmetricKeyWrapper = mockSymmetricKeyWrapper(symmetricKeyPropertySpecs, null, null);
        when(securityManagementService.newSymmetricKeyWrapper(symmetricKeyAccessorType)).thenReturn(symmetricKeyWrapper);
        Response response = target("/devices/BVN001/securityaccessors/keys/111").request().put(Entity.json(securityAccessorInfo));

        verify(symmetrickeyAccessor, never()).setActualValue(any(SymmetricKeyWrapper.class));
        ArgumentCaptor<Map> actualMapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(actualSymmetricKeyWrapper, times(1)).setProperties(actualMapArgumentCaptor.capture());
        assertThat(actualMapArgumentCaptor.getValue()).contains(MapEntry.entry("key", "actualKey"));

        verify(symmetrickeyAccessor, times(1)).setTempValue(symmetricKeyWrapper);
        ArgumentCaptor<Map> tempMapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(symmetricKeyWrapper, times(1)).setProperties(tempMapArgumentCaptor.capture());
        assertThat(tempMapArgumentCaptor.getValue()).contains(MapEntry.entry("key", "tempKey"));
    }

    @Test
    public void setActualAndTempOnExistingCertificateAccessorWithoutTemp() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.version = 22L;
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("alias", "comserver"));
        securityAccessorInfo.tempProperties = Arrays.asList(createPropertyInfo("alias", "newcomserver"));

        Response response = target("/devices/BVN001/securityaccessors/certificates/222").request().put(Entity.json(securityAccessorInfo));

        verify(clientCertificateAccessor, never()).setActualValue(any(CertificateWrapper.class));
        verify(actualClientCertificateWrapper, never()).setProperties(any(Map.class));

        verify(clientCertificateAccessor, times(1)).setTempValue(tempClientCertificateWrapper);
        verify(tempClientCertificateWrapper, never()).setProperties(any(Map.class));
        verify(tempClientCertificateWrapper, never()).delete();
    }

    @Test
    public void setActualAndTempOnExistingCertificateAccessorWithTempWithoutActual() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.version = 22L;
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("alias", "comserver"));
        securityAccessorInfo.tempProperties = Arrays.asList(createPropertyInfo("alias", "newcomserver"));

        CertificateWrapper tempCertificateWrapper = mockClientCertificateWrapper(certificatePropertySpecs, "alias", "tempAlias", "myAlias");
        clientCertificateAccessor = mock(SecurityAccessor.class);
        when(clientCertificateAccessor.getTempValue()).thenReturn(Optional.of(tempCertificateWrapper));
        when(clientCertificateAccessor.getActualValue()).thenReturn(Optional.empty());
        when(clientCertificateAccessor.getSecurityAccessorType()).thenReturn(certificateKeyAccessorType);
        when(clientCertificateAccessor.getPropertySpecs()).thenReturn(certificatePropertySpecs);
        when(device.getSecurityAccessor(certificateKeyAccessorType)).thenReturn(Optional.of(clientCertificateAccessor));
        when(deviceService.findAndLockKeyAccessorByIdAndVersion(device, certificateKeyAccessorType, 22L)).thenReturn(Optional.of(clientCertificateAccessor));

        Response response = target("/devices/BVN001/securityaccessors/certificates/222").request().put(Entity.json(securityAccessorInfo));

        verify(clientCertificateAccessor, times(1)).setActualValue(actualClientCertificateWrapper);
        verify(actualClientCertificateWrapper, never()).setProperties(any(Map.class));

        verify(clientCertificateAccessor, times(1)).setTempValue(tempClientCertificateWrapper);
        verify(tempClientCertificateWrapper, never()).setProperties(any(Map.class));
        verify(tempClientCertificateWrapper, never()).delete();
    }

    @Test
    public void setActualAndNoTempOnExistingCertificateAccessorWithTempWithoutActual() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.version = 22L;
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("alias", "comserver"));
        securityAccessorInfo.tempProperties = Collections.emptyList();

        CertificateWrapper tempCertificateWrapper = mockClientCertificateWrapper(certificatePropertySpecs, "alias", "tempAlias", "myAlias");
        clientCertificateAccessor = mock(SecurityAccessor.class);
        when(clientCertificateAccessor.getTempValue()).thenReturn(Optional.of(tempCertificateWrapper));
        when(clientCertificateAccessor.getActualValue()).thenReturn(Optional.empty());
        when(clientCertificateAccessor.getSecurityAccessorType()).thenReturn(certificateKeyAccessorType);
        when(clientCertificateAccessor.getPropertySpecs()).thenReturn(certificatePropertySpecs);
        when(device.getSecurityAccessor(certificateKeyAccessorType)).thenReturn(Optional.of(clientCertificateAccessor));
        when(deviceService.findAndLockKeyAccessorByIdAndVersion(device, certificateKeyAccessorType, 22L)).thenReturn(Optional.of(clientCertificateAccessor));

        Response response = target("/devices/BVN001/securityaccessors/certificates/222").request().put(Entity.json(securityAccessorInfo));

        verify(clientCertificateAccessor, times(1)).setActualValue(actualClientCertificateWrapper);
        verify(actualClientCertificateWrapper, never()).setProperties(any(Map.class));

        verify(clientCertificateAccessor, never()).setTempValue(tempClientCertificateWrapper);
        verify(tempClientCertificateWrapper, never()).setProperties(any(Map.class));
        verify(clientCertificateAccessor, times(1)).clearTempValue();
    }

    @Test
    public void setNoActualAndNoTempOnExistingCertificateAccessorWithActual() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.version = 22L;
        securityAccessorInfo.currentProperties = Collections.emptyList();
        securityAccessorInfo.tempProperties = Collections.emptyList();

        Response response = target("/devices/BVN001/securityaccessors/certificates/222").request().put(Entity.json(securityAccessorInfo));

        verify(clientCertificateAccessor, never()).setActualValue(actualClientCertificateWrapper);
        verify(actualClientCertificateWrapper, never()).setProperties(any(Map.class));

        verify(clientCertificateAccessor, never()).setTempValue(tempClientCertificateWrapper);
        verify(tempClientCertificateWrapper, never()).setProperties(any(Map.class));
        verify(clientCertificateAccessor, times(1)).delete();
    }

    @Test
    public void setActualAndNoTempOnExistingKeyAccessorWithoutTemp() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.version = 11L;
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("key", "actualKey"));
        securityAccessorInfo.tempProperties = Arrays.asList(createPropertyInfo("key", null));

        SymmetricKeyWrapper symmetricKeyWrapper = mockSymmetricKeyWrapper(symmetricKeyPropertySpecs, null, null);
        when(securityManagementService.newSymmetricKeyWrapper(symmetricKeyAccessorType)).thenReturn(symmetricKeyWrapper);

        Response response = target("/devices/BVN001/securityaccessors/keys/111").request().put(Entity.json(securityAccessorInfo));
        verify(symmetrickeyAccessor, never()).setActualValue(any(SymmetricKeyWrapper.class));
        ArgumentCaptor<Map> actualMapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(actualSymmetricKeyWrapper, times(1)).setProperties(actualMapArgumentCaptor.capture());
        assertThat(actualMapArgumentCaptor.getValue()).contains(MapEntry.entry("key", "actualKey"));

        verify(symmetrickeyAccessor, never()).setTempValue(symmetricKeyWrapper);
        verify(symmetricKeyWrapper, never()).setProperties(any(Map.class));
    }

    @Test
    public void setActualAndNoTempOnExistingCertificateAccessorWithoutTemp() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.version = 22L;
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("alias", "comserver"));
        securityAccessorInfo.tempProperties = Arrays.asList(createPropertyInfo("alias", null));

        Response response = target("/devices/BVN001/securityaccessors/certificates/222").request().put(Entity.json(securityAccessorInfo));

        verify(clientCertificateAccessor, never()).setActualValue(any(CertificateWrapper.class));
        verify(actualClientCertificateWrapper, never()).setProperties(any(Map.class));

        verify(clientCertificateAccessor, never()).setTempValue(tempClientCertificateWrapper);
        verify(tempClientCertificateWrapper, never()).setProperties(any(Map.class));
        verify(tempClientCertificateWrapper, never()).delete();
    }

    @Test
    public void setTempAndNoActualOnExistingKeyAccessorWithoutTemp() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.version = 11L;
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("key", null));
        securityAccessorInfo.tempProperties = Arrays.asList(createPropertyInfo("key", "tempKey"));

        SymmetricKeyWrapper symmetricKeyWrapper = mockSymmetricKeyWrapper(symmetricKeyPropertySpecs, null, null);
        when(securityManagementService.newSymmetricKeyWrapper(symmetricKeyAccessorType)).thenReturn(symmetricKeyWrapper);
        Response response = target("/devices/BVN001/securityaccessors/keys/111").request().put(Entity.json(securityAccessorInfo));

        verify(symmetrickeyAccessor, never()).setActualValue(any(SymmetricKeyWrapper.class));
        verify(actualSymmetricKeyWrapper, never()).setProperties(any(Map.class));
        verify(symmetrickeyAccessor, times(1)).delete();
    }

    @Test
    public void setTempAndNoActualOnExistingCertificateAccessorWithoutTemp() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.version = 22L;
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("alias", null));
        securityAccessorInfo.tempProperties = Arrays.asList(createPropertyInfo("alias", "newcomserver"));

        Response response = target("/devices/BVN001/securityaccessors/certificates/222").request().put(Entity.json(securityAccessorInfo));

        verify(clientCertificateAccessor, times(1)).delete();
    }

    @Test
    public void setActualAndTempOnExistingKeyAccessorWithTemp() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.version = 11L;
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("key", "actualKey"));
        securityAccessorInfo.tempProperties = Arrays.asList(createPropertyInfo("key", "tempKey"));

        SymmetricKeyWrapper tempSymmetricKeyWrapper = mockSymmetricKeyWrapper(symmetricKeyPropertySpecs, "key", "oldtempvalue");
        symmetrickeyAccessor = mockSymmetricKeyAccessor(actualSymmetricKeyWrapper, tempSymmetricKeyWrapper);
        when(device.getSecurityAccessor(symmetricKeyAccessorType)).thenReturn(Optional.of(symmetrickeyAccessor));
        when(deviceService.findAndLockKeyAccessorByIdAndVersion(device, symmetricKeyAccessorType, 11L)).thenReturn(Optional.of(symmetrickeyAccessor));
        Response response = target("/devices/BVN001/securityaccessors/keys/111").request().put(Entity.json(securityAccessorInfo));

        verify(symmetrickeyAccessor, never()).setActualValue(any(SymmetricKeyWrapper.class));
        ArgumentCaptor<Map> actualMapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(actualSymmetricKeyWrapper, times(1)).setProperties(actualMapArgumentCaptor.capture());
        assertThat(actualMapArgumentCaptor.getValue()).contains(MapEntry.entry("key", "actualKey"));

        verify(symmetrickeyAccessor, never()).setTempValue(any(SymmetricKeyWrapper.class));
        ArgumentCaptor<Map> tempMapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(tempSymmetricKeyWrapper, times(1)).setProperties(tempMapArgumentCaptor.capture());
        assertThat(tempMapArgumentCaptor.getValue()).contains(MapEntry.entry("key", "tempKey"));
    }

    @Test
    public void setActualAndTempOnExistingCertificateAccessorWithTemp() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.version = 22L;
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("alias", "comserver"));
        securityAccessorInfo.tempProperties = Arrays.asList(createPropertyInfo("alias", "newcomserver"));

        when(clientCertificateAccessor.getTempValue()).thenReturn(Optional.of(tempClientCertificateWrapper));

        Response response = target("/devices/BVN001/securityaccessors/certificates/222").request().put(Entity.json(securityAccessorInfo));

        verify(clientCertificateAccessor, never()).setActualValue(any(CertificateWrapper.class));
        verify(actualClientCertificateWrapper, never()).setProperties(any(Map.class));

        verify(clientCertificateAccessor, never()).setTempValue(any(SecurityValueWrapper.class));
        verify(tempClientCertificateWrapper, never()).setProperties(any(Map.class));
    }

    @Test
    public void setActualAndNewTempOnExistingCertificateAccessorWithTemp() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.version = 22L;
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("alias", "comserver"));
        securityAccessorInfo.tempProperties = Arrays.asList(createPropertyInfo("alias", "newnewcomserver"));

        when(clientCertificateAccessor.getTempValue()).thenReturn(Optional.of(tempClientCertificateWrapper));
        ClientCertificateWrapper newNewComserver = mockClientCertificateWrapper(certificatePropertySpecs, "alias", "newnewcomserver", "myAlias");
        when(securityManagementService.findCertificateWrapper("newnewcomserver")).thenReturn(Optional.of(newNewComserver));

        Response response = target("/devices/BVN001/securityaccessors/certificates/222").request().put(Entity.json(securityAccessorInfo));

        verify(clientCertificateAccessor, never()).setActualValue(any(CertificateWrapper.class));
        verify(actualClientCertificateWrapper, never()).setProperties(any(Map.class));

        verify(clientCertificateAccessor, times(1)).setTempValue(newNewComserver);
        verify(tempClientCertificateWrapper, never()).setProperties(any(Map.class));
    }

    @Test
    public void setActualAndTempOnExistingKeyAccessorWithTempWithIdenticalValues() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.version = 11L;
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("key", "b21nLEkgY2FuJ3QgYmVsaWV2ZSB5b3UgZGVjb2RlZCB0aGlz"));
        securityAccessorInfo.tempProperties = Arrays.asList(createPropertyInfo("key", "oldtempvalue"));

        SymmetricKeyWrapper tempSymmetricKeyWrapper = mockSymmetricKeyWrapper(symmetricKeyPropertySpecs, "key", "oldtempvalue");
        symmetrickeyAccessor = mockSymmetricKeyAccessor(actualSymmetricKeyWrapper, tempSymmetricKeyWrapper);
        when(device.getSecurityAccessor(symmetricKeyAccessorType)).thenReturn(Optional.of(symmetrickeyAccessor));
        when(deviceService.findAndLockKeyAccessorByIdAndVersion(device, symmetricKeyAccessorType, 11L)).thenReturn(Optional.of(symmetrickeyAccessor));

        Response response = target("/devices/BVN001/securityaccessors/keys/111").request().put(Entity.json(securityAccessorInfo));

        verify(symmetrickeyAccessor, never()).setActualValue(any(SymmetricKeyWrapper.class));
        verify(actualSymmetricKeyWrapper, never()).setProperties(any(Map.class));

        verify(symmetrickeyAccessor, never()).setTempValue(any(SymmetricKeyWrapper.class));
        verify(tempSymmetricKeyWrapper, never()).setProperties(any(Map.class));
    }

    @Test
    public void setActualAndTempOnNonExistingKeyAccessor() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.version = 11L;
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("key", "actualKey"));
        securityAccessorInfo.tempProperties = Arrays.asList(createPropertyInfo("key", "tempKey"));

        SymmetricKeyWrapper actualSymmetricKeyWrapper = mockSymmetricKeyWrapper(symmetricKeyPropertySpecs, null, null);
        SymmetricKeyWrapper tempSymmetricKeyWrapper = mockSymmetricKeyWrapper(symmetricKeyPropertySpecs, null, null);
        when(securityManagementService.newSymmetricKeyWrapper(symmetricKeyAccessorType)).thenReturn(actualSymmetricKeyWrapper, tempSymmetricKeyWrapper);
        when(device.getSecurityAccessor(symmetricKeyAccessorType)).thenReturn(Optional.empty());

        SymmetricKeyWrapper symmetricKeyWrapper = mockSymmetricKeyWrapper(symmetricKeyPropertySpecs, null, null);
        SecurityAccessor toBeCreatedSecurityAccessor = mockSymmetricKeyAccessor(symmetricKeyWrapper, null);

        when(device.newSecurityAccessor(symmetricKeyAccessorType)).thenReturn(toBeCreatedSecurityAccessor);

        Response response = target("/devices/BVN001/securityaccessors/keys/111").request().put(Entity.json(securityAccessorInfo));

        verify(toBeCreatedSecurityAccessor, times(1)).setActualValue(any(SymmetricKeyWrapper.class));
        ArgumentCaptor<Map> actualMapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(actualSymmetricKeyWrapper, times(1)).setProperties(actualMapArgumentCaptor.capture());
        assertThat(actualMapArgumentCaptor.getValue()).contains(MapEntry.entry("key", "actualKey"));

        verify(toBeCreatedSecurityAccessor, times(1)).setTempValue(tempSymmetricKeyWrapper);
        ArgumentCaptor<Map> tempMapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(tempSymmetricKeyWrapper, times(1)).setProperties(tempMapArgumentCaptor.capture());
        assertThat(tempMapArgumentCaptor.getValue()).contains(MapEntry.entry("key", "tempKey"));
    }

    @Test
    public void setActualAndTempOnNonExistingCertificateAccessor() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.version = 22L;
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("alias", "comserver"));
        securityAccessorInfo.tempProperties = Arrays.asList(createPropertyInfo("alias", "newcomserver"));

        when(clientCertificateAccessor.getTempValue()).thenReturn(Optional.of(tempClientCertificateWrapper));
        when(device.getSecurityAccessor(certificateKeyAccessorType)).thenReturn(Optional.empty());
        when(device.newSecurityAccessor(certificateKeyAccessorType)).thenReturn(clientCertificateAccessor);
        Response response = target("/devices/BVN001/securityaccessors/certificates/222").request().put(Entity.json(securityAccessorInfo));

        verify(clientCertificateAccessor, times(1)).setActualValue(actualClientCertificateWrapper);
        verify(actualClientCertificateWrapper, never()).setProperties(any(Map.class));

        verify(clientCertificateAccessor, times(1)).setTempValue(tempClientCertificateWrapper);
        verify(tempClientCertificateWrapper, never()).setProperties(any(Map.class));
    }

    @Test
    public void testDeleteTempOnKeyAccessorWithTempAndActual() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.version = 11L;
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("key", "b21nLEkgY2FuJ3QgYmVsaWV2ZSB5b3UgZGVjb2RlZCB0aGlz"));
        securityAccessorInfo.tempProperties = Collections.emptyList();

        SymmetricKeyWrapper tempSymmetricKeyWrapper = mockSymmetricKeyWrapper(symmetricKeyPropertySpecs, "key", "oldtempvalue");
        symmetrickeyAccessor = mockSymmetricKeyAccessor(actualSymmetricKeyWrapper, tempSymmetricKeyWrapper);
        when(device.getSecurityAccessor(symmetricKeyAccessorType)).thenReturn(Optional.ofNullable(symmetrickeyAccessor));
        when(deviceService.findAndLockKeyAccessorByIdAndVersion(device, symmetricKeyAccessorType, 11L)).thenReturn(Optional.of(symmetrickeyAccessor));

        Response response = target("/devices/BVN001/securityaccessors/keys/111").request().put(Entity.json(securityAccessorInfo));

        verify(symmetrickeyAccessor, never()).setActualValue(any(SymmetricKeyWrapper.class));
        verify(actualSymmetricKeyWrapper, never()).setProperties(any(Map.class));

        verify(symmetrickeyAccessor, times(1)).clearTempValue();
    }

    @Test
    public void testGenerateTempValue() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.version = 11L;
        securityAccessorInfo.currentProperties = Collections.emptyList();
        securityAccessorInfo.tempProperties = Collections.emptyList();

        Response response = target("/devices/BVN001/securityaccessors/keys/111/temp").request().post(Entity.json(securityAccessorInfo));
        verify(symmetrickeyAccessor, times(1)).renew();
    }

    @Test
    public void testSwapValues() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.version = 11L;
        securityAccessorInfo.currentProperties = Collections.emptyList();
        securityAccessorInfo.tempProperties = Collections.emptyList();

        Response response = target("/devices/BVN001/securityaccessors/keys/111/swap").request().put(Entity.json(securityAccessorInfo));
        verify(symmetrickeyAccessor, times(1)).swapValues();
    }

    private PropertyInfo createPropertyInfo(String key, String value) {
        PropertyInfo propertyInfo = new PropertyInfo();
        propertyInfo.key = key;
        propertyInfo.propertyValueInfo = new PropertyValueInfo<>(value, null);
        return propertyInfo;
    }

    private SecurityAccessor mockClientCertificateAccessor(List<PropertySpec> propertySpecs, SecurityValueWrapper clientCertificateWrapper) {
        SecurityAccessor securityAccessor1 = mock(SecurityAccessor.class);
        when(securityAccessor1.getTempValue()).thenReturn(Optional.empty());
        when(securityAccessor1.getActualValue()).thenReturn(Optional.of(clientCertificateWrapper));
        when(securityAccessor1.getSecurityAccessorType()).thenReturn(certificateKeyAccessorType);
        when(securityAccessor1.getPropertySpecs()).thenReturn(propertySpecs);
        return securityAccessor1;
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

    protected PropertySpec mockPropertySpec(String name) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.getValueType()).thenReturn(String.class);
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        when(propertySpec.getName()).thenReturn(name);
        return propertySpec;
    }

    private SecurityAccessorType mockCertificateKeyAccessorType(long id, String name) {
        SecurityAccessorType keyAccessorType = mock(SecurityAccessorType.class);
        when(keyAccessorType.getId()).thenReturn(id);
        when(keyAccessorType.getName()).thenReturn(name);
        when(keyAccessorType.getDuration()).thenReturn(Optional.of(TimeDuration.years(1)));
        when(keyAccessorType.getDescription()).thenReturn("description");
        when(keyAccessorType.isCurrentUserAllowedToViewProperties("MDC")).thenReturn(true);
        when(keyAccessorType.isCurrentUserAllowedToEditProperties("MDC")).thenReturn(true);
        KeyType keyType = mock(KeyType.class);
        when(keyType.getCryptographicType()).thenReturn(CryptographicType.ClientCertificate);
        when(keyAccessorType.getKeyType()).thenReturn(keyType);
        return keyAccessorType;
    }

    private SecurityAccessor<SecurityValueWrapper> mockSymmetricKeyAccessor(SecurityValueWrapper actual, SecurityValueWrapper temp) {
        SecurityAccessor<SecurityValueWrapper> securityAccessor1 = mock(SecurityAccessor.class);
        when(securityAccessor1.getPropertySpecs()).thenReturn(symmetricKeyPropertySpecs);
        when(securityAccessor1.getTempValue()).thenReturn(Optional.ofNullable(temp));
        when(securityAccessor1.getActualValue()).thenReturn(Optional.ofNullable(actual));
        when(securityAccessor1.getSecurityAccessorType()).thenReturn(symmetricKeyAccessorType);
        when(securityAccessor1.getVersion()).thenReturn(11L);
        return securityAccessor1;
    }

    private SymmetricKeyWrapper mockSymmetricKeyWrapper(List<PropertySpec> propertySpecs, String key, String value) {
        SymmetricKeyWrapper symmetricKeyWrapper = mock(SymmetricKeyWrapper.class);
        if (key!=null && value!=null) {
            Map<String, Object> map = new HashMap<>();
            map.put(key, value);
            when(symmetricKeyWrapper.getProperties()).thenReturn(map);
        } else {
            when(symmetricKeyWrapper.getProperties()).thenReturn(Collections.emptyMap());
        }
        when(symmetricKeyWrapper.getPropertySpecs()).thenReturn(propertySpecs);
        when(symmetricKeyWrapper.getExpirationTime()).thenReturn(Optional.of(Instant.now()));
        return symmetricKeyWrapper;
    }

    private SecurityAccessorType mockSymmetricKeyAccessorType(long id, String name) {
        SecurityAccessorType keyAccessorType = mock(SecurityAccessorType.class);
        when(keyAccessorType.getId()).thenReturn(id);
        when(keyAccessorType.getName()).thenReturn(name);
        when(keyAccessorType.getDuration()).thenReturn(Optional.of(TimeDuration.years(1)));
        when(keyAccessorType.getDescription()).thenReturn("description");
        when(keyAccessorType.isCurrentUserAllowedToViewProperties("MDC")).thenReturn(true);
        when(keyAccessorType.isCurrentUserAllowedToEditProperties("MDC")).thenReturn(true);
        KeyType keyType = mock(KeyType.class);
        when(keyType.getCryptographicType()).thenReturn(CryptographicType.SymmetricKey);
        when(keyAccessorType.getKeyType()).thenReturn(keyType);
        return keyAccessorType;
    }
}
