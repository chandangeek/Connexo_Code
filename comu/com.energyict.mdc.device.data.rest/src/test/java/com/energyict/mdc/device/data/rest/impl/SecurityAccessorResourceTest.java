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
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.KeyAccessor;

import com.jayway.jsonpath.JsonModel;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 4/4/17.
 */
public class SecurityAccessorResourceTest extends DeviceDataRestApplicationJerseyTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Device device = mock(Device.class);
        KeyAccessor keyAccessor1 = mockClientCertificateAccessor();
        KeyAccessor keyAccessor2 = mockSymmetricKeyAccessor();

        when(device.getKeyAccessors()).thenReturn(Arrays.asList(keyAccessor1, keyAccessor2));
        when(deviceService.findDeviceByName("BVN001")).thenReturn(Optional.of(device));
    }

    @Test
    public void testGetCertificates() throws Exception {

        Response response = target("/devices/BVN001/securityaccessors/certificates").request().get();
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<List>get("$.certificates")).hasSize(1);
        assertThat(jsonModel.<List>get("$.certificates[0].currentProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.certificates[0].currentProperties[0].key")).isEqualTo("alias");
        assertThat(jsonModel.<String>get("$.certificates[0].currentProperties[0].propertyValueInfo.value")).isEqualTo("comserver");
    }

    @Test
    public void testGetKeys() throws Exception {

        Response response = target("/devices/BVN001/securityaccessors/keys").request().get();
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<List>get("$.certificates")).hasSize(1);
        assertThat(jsonModel.<List>get("$.certificates[0].currentProperties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.certificates[0].currentProperties[0].key")).isEqualTo("alias");
        assertThat(jsonModel.<String>get("$.certificates[0].currentProperties[0].propertyValueInfo.value")).isEqualTo("comserver");
    }

    private KeyAccessor mockClientCertificateAccessor() {
        KeyAccessor keyAccessor1 = mock(KeyAccessor.class);
        ClientCertificateWrapper clientCertificateWrapper = mock(ClientCertificateWrapper.class);
        when(keyAccessor1.getTempValue()).thenReturn(Optional.empty());
        when(keyAccessor1.getActualValue()).thenReturn(clientCertificateWrapper);
        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getDuration()).thenReturn(Optional.of(TimeDuration.years(1)));
        when(keyAccessorType.getDescription()).thenReturn("description");
        KeyType keyType = mock(KeyType.class);
        when(keyType.getCryptographicType()).thenReturn(CryptographicType.ClientCertificate);
        when(keyAccessorType.getKeyType()).thenReturn(keyType);
        when(keyAccessor1.getKeyAccessorType()).thenReturn(keyAccessorType);
        Map<String, Object> map = new HashMap<>();
        map.put("alias", "comserver");
        when(clientCertificateWrapper.getProperties()).thenReturn(map);
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(clientCertificateWrapper.getPropertySpecs()).thenReturn(Arrays.asList(propertySpec));
        when(clientCertificateWrapper.getExpirationTime()).thenReturn(Optional.of(Instant.now()));
        ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.getValueType()).thenReturn(String.class);
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        when(propertySpec.getName()).thenReturn("alias");
        return keyAccessor1;
    }
    private KeyAccessor mockSymmetricKeyAccessor() {
        KeyAccessor keyAccessor1 = mock(KeyAccessor.class);
        SymmetricKeyWrapper symmetricKeyWrapper = mock(SymmetricKeyWrapper.class);
        when(keyAccessor1.getTempValue()).thenReturn(Optional.empty());
        when(keyAccessor1.getActualValue()).thenReturn(symmetricKeyWrapper);
        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getDuration()).thenReturn(Optional.of(TimeDuration.years(1)));
        when(keyAccessorType.getDescription()).thenReturn("description");
        KeyType keyType = mock(KeyType.class);
        when(keyType.getCryptographicType()).thenReturn(CryptographicType.SymmetricKey);
        when(keyAccessorType.getKeyType()).thenReturn(keyType);
        when(keyAccessor1.getKeyAccessorType()).thenReturn(keyAccessorType);
        Map<String, Object> map = new HashMap<>();
        map.put("encryptedKey", "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoK");
        when(symmetricKeyWrapper.getProperties()).thenReturn(map);
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(symmetricKeyWrapper.getPropertySpecs()).thenReturn(Arrays.asList(propertySpec));
        when(symmetricKeyWrapper.getExpirationTime()).thenReturn(Optional.of(Instant.now()));
        ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.getValueType()).thenReturn(String.class);
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        when(propertySpec.getName()).thenReturn("encryptedKey");
        return keyAccessor1;
    }
}
