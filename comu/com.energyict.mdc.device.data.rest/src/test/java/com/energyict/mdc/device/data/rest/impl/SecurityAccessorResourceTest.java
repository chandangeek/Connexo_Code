/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.properties.rest.impl.PropertyValueInfoServiceImpl;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.KeyAccessor;

import com.jayway.jsonpath.JsonModel;
import net.minidev.json.JSONObject;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.io.InputStream;
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 4/4/17.
 */
public class SecurityAccessorResourceTest extends DeviceDataRestApplicationJerseyTest {

    private KeyAccessorType symmetricKeyAccessorType;
    private List<PropertySpec> symmetricKeyPropertySpecs;
    private List<PropertySpec> certificatePropertySpecs;
    private KeyAccessorType certificateKeyAccessorType;
    private DeviceType deviceType;
    private KeyAccessor clientCertificateAccessor;
    private KeyAccessor symmetrickeyAccessor;
    private SymmetricKeyWrapper actualSymmetricKeyWrapper;
    private ClientCertificateWrapper actualClientCertificateWrapper;
    private ClientCertificateWrapper tempClientCertificateWrapper;
    private Device device;

    @Override
    protected Application getApplication() {
        DeviceApplication application = (DeviceApplication) super.getApplication();
        PropertyValueInfoServiceImpl propertyValueInfoService = new PropertyValueInfoServiceImpl();
        propertyValueInfoService.activate();
        application.setPropertyValueInfoService(propertyValueInfoService); // use the real thing
        return application;
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        symmetricKeyPropertySpecs = Arrays.asList(mockPropertySpec("key"));
        certificatePropertySpecs = Arrays.asList(mockPropertySpec("alias"));

        symmetricKeyAccessorType = mockSymmetricKeyAccessorType(111L, "aes");
        certificateKeyAccessorType = mockCertificateKeyAccessorType(222L, "tls1");

        actualClientCertificateWrapper = mockClientCertificateWrapper(certificatePropertySpecs, "alias", "comserver");
        clientCertificateAccessor = mockClientCertificateAccessor(certificatePropertySpecs, actualClientCertificateWrapper);

        actualSymmetricKeyWrapper = mockSymmetricKeyWrapper(symmetricKeyPropertySpecs, "key", "b21nLEkgY2FuJ3QgYmVsaWV2ZSB5b3UgZGVjb2RlZCB0aGlz");
        symmetrickeyAccessor = mockSymmetricKeyAccessor(actualSymmetricKeyWrapper, null);

        deviceType = mock(DeviceType.class);
        when(deviceType.getKeyAccessorTypes()).thenReturn(Arrays.asList(symmetricKeyAccessorType, certificateKeyAccessorType));
        device = mock(Device.class);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getKeyAccessors()).thenReturn(Arrays.asList(clientCertificateAccessor, symmetrickeyAccessor));
        when(device.getKeyAccessor(any(KeyAccessorType.class))).thenReturn(Optional.empty());
        when(device.getKeyAccessor(certificateKeyAccessorType)).thenReturn(Optional.ofNullable(clientCertificateAccessor));
        when(device.getKeyAccessor(symmetricKeyAccessorType)).thenReturn(Optional.ofNullable(symmetrickeyAccessor));
        when(deviceService.findDeviceByName("BVN001")).thenReturn(Optional.of(device));

        when(pkiService.getPropertySpecs(symmetricKeyAccessorType)).thenReturn(symmetricKeyPropertySpecs);
        when(pkiService.getPropertySpecs(certificateKeyAccessorType)).thenReturn(certificatePropertySpecs);
        when(pkiService.findCertificateWrapper(anyString())).thenReturn(Optional.empty());
        when(pkiService.findCertificateWrapper("comserver")).thenReturn(Optional.of(actualClientCertificateWrapper));
        tempClientCertificateWrapper = mockClientCertificateWrapper(certificatePropertySpecs, "alias", "newcomserver");
        when(pkiService.findCertificateWrapper("newcomserver")).thenReturn(Optional.of(tempClientCertificateWrapper));
    }

    @Test
    public void testGetCertificatesWithoutTempValue() throws Exception {

        Response response = target("/devices/BVN001/securityaccessors/certificates").request().get();
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<List>get("$.certificates")).hasSize(1);
        assertThat(jsonModel.<String>get("$.certificates[0].name")).isEqualTo("tls1");
        assertThat(jsonModel.<List>get("$.certificates[0].currentProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.certificates[0].currentProperties[0].key")).isEqualTo("alias");
        assertThat(jsonModel.<String>get("$.certificates[0].currentProperties[0].propertyValueInfo.value")).isEqualTo("comserver");
        assertThat(jsonModel.<List>get("$.certificates[0].tempProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.certificates[0].tempProperties[0].key")).isEqualTo("alias");
        assertThat(jsonModel.<JSONObject>get("$.certificates[0].tempProperties[0].propertyValueInfo")).isEmpty();
    }

    /**
     * 1st key accessor type (tls1) has a key accessor (without temp value)
     * 2nd key accessor type (tls2) does not have a key accessor at all.
     * @throws Exception
     */
    @Test
    public void testGetCertificatesWithoutValueForKeyAccessorType() throws Exception {

        KeyAccessorType keyAccessorType = mockCertificateKeyAccessorType(2L, "tls2");
        when(deviceType.getKeyAccessorTypes()).thenReturn(Arrays.asList(certificateKeyAccessorType, symmetricKeyAccessorType, keyAccessorType));
        PropertySpec propertySpec = mockPropertySpec("alias");
        when(pkiService.getPropertySpecs(keyAccessorType)).thenReturn(Arrays.asList(propertySpec));

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

        assertThat(jsonModel.<String>get("$.certificates[1].name")).isEqualTo("tls2");
        assertThat(jsonModel.<List>get("$.certificates[1].currentProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.certificates[1].currentProperties[0].key")).isEqualTo("alias");
        assertThat(jsonModel.<JSONObject>get("$.certificates[1].currentProperties[0].propertyValueInfo")).isEmpty();
        assertThat(jsonModel.<List>get("$.certificates[1].tempProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.certificates[1].tempProperties[0].key")).isEqualTo("alias");
        assertThat(jsonModel.<JSONObject>get("$.certificates[1].tempProperties[0].propertyValueInfo")).isEmpty();
    }

    @Test
    public void testGetKeysWithoutTempValue() throws Exception {

        Response response = target("/devices/BVN001/securityaccessors/keys").request().get();
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<List>get("$.keys")).hasSize(1);
        assertThat(jsonModel.<List>get("$.keys[0].currentProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.keys[0].currentProperties[0].key")).isEqualTo("key");
        assertThat(jsonModel.<String>get("$.keys[0].currentProperties[0].propertyValueInfo.value")).isEqualTo("b21nLEkgY2FuJ3QgYmVsaWV2ZSB5b3UgZGVjb2RlZCB0aGlz");
        assertThat(jsonModel.<List>get("$.keys[0].tempProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.keys[0].tempProperties[0].key")).isEqualTo("key");
        assertThat(jsonModel.<JSONObject>get("$.keys[0].tempProperties[0].propertyValueInfo")).isEmpty();
    }

    @Test
    public void setActualAndTempOnExistingKeyAccessorWithoutTemp() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("key", "actualKey"));
        securityAccessorInfo.tempProperties = Arrays.asList(createPropertyInfo("key", "tempKey"));

        SymmetricKeyWrapper symmetricKeyWrapper = mockSymmetricKeyWrapper(symmetricKeyPropertySpecs, null, null);
        when(pkiService.newSymmetricKeyWrapper(symmetricKeyAccessorType)).thenReturn(symmetricKeyWrapper);
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
    public void setActualAndNoTempOnExistingKeyAccessorWithoutTemp() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("key", "actualKey"));
        securityAccessorInfo.tempProperties = Arrays.asList(createPropertyInfo("key", null));

        SymmetricKeyWrapper symmetricKeyWrapper = mockSymmetricKeyWrapper(symmetricKeyPropertySpecs, null, null);
        when(pkiService.newSymmetricKeyWrapper(symmetricKeyAccessorType)).thenReturn(symmetricKeyWrapper);

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
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("key", null));
        securityAccessorInfo.tempProperties = Arrays.asList(createPropertyInfo("key", "tempKey"));

        SymmetricKeyWrapper symmetricKeyWrapper = mockSymmetricKeyWrapper(symmetricKeyPropertySpecs, null, null);
        when(pkiService.newSymmetricKeyWrapper(symmetricKeyAccessorType)).thenReturn(symmetricKeyWrapper);
        Response response = target("/devices/BVN001/securityaccessors/keys/111").request().put(Entity.json(securityAccessorInfo));

        verify(symmetrickeyAccessor, never()).setActualValue(any(SymmetricKeyWrapper.class));
        verify(actualSymmetricKeyWrapper, never()).setProperties(any(Map.class));
        verify(symmetrickeyAccessor, times(1)).delete();
    }

    @Test
    public void setTempAndNoActualOnExistingCertificateAccessorWithoutTemp() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("alias", null));
        securityAccessorInfo.tempProperties = Arrays.asList(createPropertyInfo("alias", "newcomserver"));

        Response response = target("/devices/BVN001/securityaccessors/certificates/222").request().put(Entity.json(securityAccessorInfo));

        verify(clientCertificateAccessor, times(1)).delete();
    }

    @Test
    public void setActualAndTempOnExistingKeyAccessorWithTemp() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("key", "actualKey"));
        securityAccessorInfo.tempProperties = Arrays.asList(createPropertyInfo("key", "tempKey"));

        SymmetricKeyWrapper tempSymmetricKeyWrapper = mockSymmetricKeyWrapper(symmetricKeyPropertySpecs, "key", "oldtempvalue");
        symmetrickeyAccessor = mockSymmetricKeyAccessor(actualSymmetricKeyWrapper, tempSymmetricKeyWrapper);
        when(device.getKeyAccessor(symmetricKeyAccessorType)).thenReturn(Optional.ofNullable(symmetrickeyAccessor));

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
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("alias", "comserver"));
        securityAccessorInfo.tempProperties = Arrays.asList(createPropertyInfo("alias", "newnewcomserver"));

        when(clientCertificateAccessor.getTempValue()).thenReturn(Optional.of(tempClientCertificateWrapper));
        ClientCertificateWrapper newNewComserver = mockClientCertificateWrapper(certificatePropertySpecs, "alias", "newnewcomserver");
        when(pkiService.findCertificateWrapper("newnewcomserver")).thenReturn(Optional.of(newNewComserver));

        Response response = target("/devices/BVN001/securityaccessors/certificates/222").request().put(Entity.json(securityAccessorInfo));

        verify(clientCertificateAccessor, never()).setActualValue(any(CertificateWrapper.class));
        verify(actualClientCertificateWrapper, never()).setProperties(any(Map.class));

        verify(clientCertificateAccessor, times(1)).setTempValue(newNewComserver);
        verify(tempClientCertificateWrapper, never()).setProperties(any(Map.class));
    }

    @Test
    public void setActualAndTempOnExistingKeyAccessorWithTempWithIdenticalValues() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("key", "b21nLEkgY2FuJ3QgYmVsaWV2ZSB5b3UgZGVjb2RlZCB0aGlz"));
        securityAccessorInfo.tempProperties = Arrays.asList(createPropertyInfo("key", "oldtempvalue"));

        SymmetricKeyWrapper tempSymmetricKeyWrapper = mockSymmetricKeyWrapper(symmetricKeyPropertySpecs, "key", "oldtempvalue");
        symmetrickeyAccessor = mockSymmetricKeyAccessor(actualSymmetricKeyWrapper, tempSymmetricKeyWrapper);
        when(device.getKeyAccessor(symmetricKeyAccessorType)).thenReturn(Optional.ofNullable(symmetrickeyAccessor));

        Response response = target("/devices/BVN001/securityaccessors/keys/111").request().put(Entity.json(securityAccessorInfo));

        verify(symmetrickeyAccessor, never()).setActualValue(any(SymmetricKeyWrapper.class));
        verify(actualSymmetricKeyWrapper, never()).setProperties(any(Map.class));

        verify(symmetrickeyAccessor, never()).setTempValue(any(SymmetricKeyWrapper.class));
        verify(tempSymmetricKeyWrapper, never()).setProperties(any(Map.class));
    }

    @Test
    public void setActualAndTempOnNonExistingKeyAccessor() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("key", "actualKey"));
        securityAccessorInfo.tempProperties = Arrays.asList(createPropertyInfo("key", "tempKey"));

        SymmetricKeyWrapper actualSymmetricKeyWrapper = mockSymmetricKeyWrapper(symmetricKeyPropertySpecs, null, null);
        SymmetricKeyWrapper tempSymmetricKeyWrapper = mockSymmetricKeyWrapper(symmetricKeyPropertySpecs, null, null);
        when(pkiService.newSymmetricKeyWrapper(symmetricKeyAccessorType)).thenReturn(actualSymmetricKeyWrapper, tempSymmetricKeyWrapper);
        when(device.getKeyAccessor(symmetricKeyAccessorType)).thenReturn(Optional.empty());

        SymmetricKeyWrapper symmetricKeyWrapper = mockSymmetricKeyWrapper(symmetricKeyPropertySpecs, null, null);
        KeyAccessor toBeCreatedKeyAccessor = mockSymmetricKeyAccessor(symmetricKeyWrapper, null);

        when(device.newKeyAccessor(symmetricKeyAccessorType)).thenReturn(toBeCreatedKeyAccessor);

        Response response = target("/devices/BVN001/securityaccessors/keys/111").request().put(Entity.json(securityAccessorInfo));

        verify(toBeCreatedKeyAccessor, times(1)).setActualValue(any(SymmetricKeyWrapper.class));
        ArgumentCaptor<Map> actualMapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(actualSymmetricKeyWrapper, times(1)).setProperties(actualMapArgumentCaptor.capture());
        assertThat(actualMapArgumentCaptor.getValue()).contains(MapEntry.entry("key", "actualKey"));

        verify(toBeCreatedKeyAccessor, times(1)).setTempValue(tempSymmetricKeyWrapper);
        ArgumentCaptor<Map> tempMapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(tempSymmetricKeyWrapper, times(1)).setProperties(tempMapArgumentCaptor.capture());
        assertThat(tempMapArgumentCaptor.getValue()).contains(MapEntry.entry("key", "tempKey"));
    }

    @Test
    public void setActualAndTempOnNonExistingCertificateAccessor() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("alias", "comserver"));
        securityAccessorInfo.tempProperties = Arrays.asList(createPropertyInfo("alias", "newcomserver"));

        when(clientCertificateAccessor.getTempValue()).thenReturn(Optional.of(tempClientCertificateWrapper));
        when(device.getKeyAccessor(certificateKeyAccessorType)).thenReturn(Optional.empty());
        when(device.newKeyAccessor(certificateKeyAccessorType)).thenReturn(clientCertificateAccessor);
        Response response = target("/devices/BVN001/securityaccessors/certificates/222").request().put(Entity.json(securityAccessorInfo));

        verify(clientCertificateAccessor, times(1)).setActualValue(actualClientCertificateWrapper);
        verify(actualClientCertificateWrapper, never()).setProperties(any(Map.class));

        verify(clientCertificateAccessor, times(1)).setTempValue(tempClientCertificateWrapper);
        verify(tempClientCertificateWrapper, never()).setProperties(any(Map.class));
    }

    @Test
    public void testDeleteTempOnKeyAccessorWithTempAndActual() throws Exception {
        SecurityAccessorInfo securityAccessorInfo = new SecurityAccessorInfo();
        securityAccessorInfo.currentProperties = Arrays.asList(createPropertyInfo("key", "b21nLEkgY2FuJ3QgYmVsaWV2ZSB5b3UgZGVjb2RlZCB0aGlz"));
        securityAccessorInfo.tempProperties = Collections.emptyList();

        SymmetricKeyWrapper tempSymmetricKeyWrapper = mockSymmetricKeyWrapper(symmetricKeyPropertySpecs, "key", "oldtempvalue");
        symmetrickeyAccessor = mockSymmetricKeyAccessor(actualSymmetricKeyWrapper, tempSymmetricKeyWrapper);
        when(device.getKeyAccessor(symmetricKeyAccessorType)).thenReturn(Optional.ofNullable(symmetrickeyAccessor));

        Response response = target("/devices/BVN001/securityaccessors/keys/111").request().put(Entity.json(securityAccessorInfo));

        verify(symmetrickeyAccessor, never()).setActualValue(any(SymmetricKeyWrapper.class));
        verify(actualSymmetricKeyWrapper, never()).setProperties(any(Map.class));

        verify(symmetrickeyAccessor, times(1)).clearTempValue();
    }

    private PropertyInfo createPropertyInfo(String key, String value) {
        PropertyInfo propertyInfo = new PropertyInfo();
        propertyInfo.key = key;
        propertyInfo.propertyValueInfo = new PropertyValueInfo<String>(value, null);
        return propertyInfo;
    }

    private KeyAccessor mockClientCertificateAccessor(List<PropertySpec> propertySpecs, SecurityValueWrapper clientCertificateWrapper) {
        KeyAccessor keyAccessor1 = mock(KeyAccessor.class);
        when(keyAccessor1.getTempValue()).thenReturn(Optional.empty());
        when(keyAccessor1.getActualValue()).thenReturn(clientCertificateWrapper);
        when(keyAccessor1.getKeyAccessorType()).thenReturn(certificateKeyAccessorType);
        when(keyAccessor1.getPropertySpecs()).thenReturn(propertySpecs);
        return keyAccessor1;
    }

    private ClientCertificateWrapper mockClientCertificateWrapper(List<PropertySpec> propertySpecs, String key, String value) {
        ClientCertificateWrapper clientCertificateWrapper = mock(ClientCertificateWrapper.class);
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        when(clientCertificateWrapper.getProperties()).thenReturn(map);
        when(clientCertificateWrapper.getPropertySpecs()).thenReturn(propertySpecs);
        when(clientCertificateWrapper.getExpirationTime()).thenReturn(Optional.of(Instant.now()));
        return clientCertificateWrapper;
    }

    private PropertySpec mockPropertySpec(String name) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.getValueType()).thenReturn(String.class);
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        when(propertySpec.getName()).thenReturn(name);
        return propertySpec;
    }

    private KeyAccessorType mockCertificateKeyAccessorType(long id, String name) {
        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getId()).thenReturn(id);
        when(keyAccessorType.getName()).thenReturn(name);
        when(keyAccessorType.getDuration()).thenReturn(Optional.of(TimeDuration.years(1)));
        when(keyAccessorType.getDescription()).thenReturn("description");
        KeyType keyType = mock(KeyType.class);
        when(keyType.getCryptographicType()).thenReturn(CryptographicType.ClientCertificate);
        when(keyAccessorType.getKeyType()).thenReturn(keyType);
        return keyAccessorType;
    }

    private KeyAccessor mockSymmetricKeyAccessor(SecurityValueWrapper actual, SecurityValueWrapper temp) {
        KeyAccessor keyAccessor1 = mock(KeyAccessor.class);
        when(keyAccessor1.getPropertySpecs()).thenReturn(symmetricKeyPropertySpecs);
        when(keyAccessor1.getTempValue()).thenReturn(Optional.ofNullable(temp));
        when(keyAccessor1.getActualValue()).thenReturn(actual);
        when(keyAccessor1.getKeyAccessorType()).thenReturn(symmetricKeyAccessorType);
        return keyAccessor1;
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

    private KeyAccessorType mockSymmetricKeyAccessorType(long id, String name) {
        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getId()).thenReturn(id);
        when(keyAccessorType.getName()).thenReturn(name);
        when(keyAccessorType.getDuration()).thenReturn(Optional.of(TimeDuration.years(1)));
        when(keyAccessorType.getDescription()).thenReturn("description");
        KeyType keyType = mock(KeyType.class);
        when(keyType.getCryptographicType()).thenReturn(CryptographicType.SymmetricKey);
        when(keyAccessorType.getKeyType()).thenReturn(keyType);
        return keyAccessorType;
    }
}
