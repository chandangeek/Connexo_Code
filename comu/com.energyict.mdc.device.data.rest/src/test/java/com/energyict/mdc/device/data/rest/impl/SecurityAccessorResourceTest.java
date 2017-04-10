/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.impl.PropertyValueInfoServiceImpl;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.KeyAccessor;

import com.jayway.jsonpath.JsonModel;
import net.minidev.json.JSONObject;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 4/4/17.
 */
public class SecurityAccessorResourceTest extends DeviceDataRestApplicationJerseyTest {

    private KeyAccessorType symmetricKeyAccessorType;
    private KeyAccessorType certificateKeyAccessorType;
    private DeviceType deviceType;

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
        Device device = mock(Device.class);
        symmetricKeyAccessorType = mockSymmetricKeyType(1L, "aes");
        certificateKeyAccessorType = mockCertificateKeyAccessorType(1L, "tls1");

        KeyAccessor keyAccessor1 = mockClientCertificateAccessor();
        KeyAccessor keyAccessor2 = mockSymmetricKeyAccessor();

        deviceType = mock(DeviceType.class);
        when(deviceType.getKeyAccessorTypes()).thenReturn(Arrays.asList(symmetricKeyAccessorType, certificateKeyAccessorType));
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getKeyAccessors()).thenReturn(Arrays.asList(keyAccessor1, keyAccessor2));
        when(device.getKeyAccessor(any(KeyAccessorType.class))).thenReturn(Optional.empty());
        when(device.getKeyAccessor(certificateKeyAccessorType)).thenReturn(Optional.ofNullable(keyAccessor1));
        when(device.getKeyAccessor(symmetricKeyAccessorType)).thenReturn(Optional.ofNullable(keyAccessor2));
        when(deviceService.findDeviceByName("BVN001")).thenReturn(Optional.of(device));

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
        assertThat(jsonModel.<String>get("$.keys[0].currentProperties[0].key")).isEqualTo("encryptedKey");
        assertThat(jsonModel.<String>get("$.keys[0].currentProperties[0].propertyValueInfo.value")).isEqualTo("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoK");
        assertThat(jsonModel.<List>get("$.keys[0].tempProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.keys[0].tempProperties[0].key")).isEqualTo("encryptedKey");
        assertThat(jsonModel.<JSONObject>get("$.keys[0].tempProperties[0].propertyValueInfo")).isEmpty();
    }

    private KeyAccessor mockClientCertificateAccessor() {
        KeyAccessor keyAccessor1 = mock(KeyAccessor.class);
        ClientCertificateWrapper clientCertificateWrapper = mock(ClientCertificateWrapper.class);
        when(keyAccessor1.getTempValue()).thenReturn(Optional.empty());
        when(keyAccessor1.getActualValue()).thenReturn(clientCertificateWrapper);
        when(keyAccessor1.getKeyAccessorType()).thenReturn(certificateKeyAccessorType);
        Map<String, Object> map = new HashMap<>();
        map.put("alias", "comserver");
        when(clientCertificateWrapper.getProperties()).thenReturn(map);
        PropertySpec propertySpec = mockPropertySpec("alias");
        List<PropertySpec> propertySpecs = Arrays.asList(propertySpec);
        when(clientCertificateWrapper.getPropertySpecs()).thenReturn(propertySpecs);
        when(keyAccessor1.getPropertySpecs()).thenReturn(propertySpecs);
        when(clientCertificateWrapper.getExpirationTime()).thenReturn(Optional.of(Instant.now()));
        return keyAccessor1;
    }

    private PropertySpec mockPropertySpec(String name) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.getValueType()).thenReturn(String.class);
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
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

    private KeyAccessor mockSymmetricKeyAccessor() {
        KeyAccessor keyAccessor1 = mock(KeyAccessor.class);
        SymmetricKeyWrapper symmetricKeyWrapper = mock(SymmetricKeyWrapper.class);
        when(keyAccessor1.getTempValue()).thenReturn(Optional.empty());
        when(keyAccessor1.getActualValue()).thenReturn(symmetricKeyWrapper);
        when(keyAccessor1.getKeyAccessorType()).thenReturn(symmetricKeyAccessorType);
        Map<String, Object> map = new HashMap<>();
        map.put("encryptedKey", "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoK");
        when(symmetricKeyWrapper.getProperties()).thenReturn(map);
        PropertySpec propertySpec = mockPropertySpec("encryptedKey");
        List<PropertySpec> propertySpecs = Arrays.asList(propertySpec);
        when(symmetricKeyWrapper.getPropertySpecs()).thenReturn(propertySpecs);
        when(keyAccessor1.getPropertySpecs()).thenReturn(propertySpecs);
        when(symmetricKeyWrapper.getExpirationTime()).thenReturn(Optional.of(Instant.now()));
        return keyAccessor1;
    }

    private KeyAccessorType mockSymmetricKeyType(long id, String name) {
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
