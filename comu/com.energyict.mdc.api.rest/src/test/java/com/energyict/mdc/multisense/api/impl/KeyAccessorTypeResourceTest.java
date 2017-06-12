/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.pki.KeyAccessorType;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.KeyAccessor;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.RequestSecurityLevel;
import com.energyict.mdc.protocol.api.security.ResponseSecurityLevel;
import com.energyict.mdc.protocol.api.security.SecuritySuite;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author stijn
 * @since 07.06.17 - 10:47
 */
public class KeyAccessorTypeResourceTest extends MultisensePublicApiJerseyTest {

    private Device device;
    private SecurityPropertySet sps1, sps2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        DeviceType deviceType = mockDeviceType(1L, "device type", 1001L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(2L, "device config", deviceType, 1002L);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mockAuthenticationAccessLevel(3);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mockEncryptionAccessLevel(4);
        SecuritySuite securitySuite = mockSecuritySuite(5);
        RequestSecurityLevel requestSecurityDeviceAccessLevel = mockRequestSecurityDeviceAccessLevel(6);
        ResponseSecurityLevel responseSecurityDeviceAccessLevel = mockResponseSecurityDeviceAccessLevel(7);

        sps1 = mockSecurityPropertySet(5L, deviceConfiguration, "sps1", 1, securitySuite, encryptionDeviceAccessLevel, authenticationDeviceAccessLevel, requestSecurityDeviceAccessLevel, responseSecurityDeviceAccessLevel, "Password", 123L, 1003L);
        sps2 = mockSecurityPropertySet(6L, deviceConfiguration, "sps2", 2, encryptionDeviceAccessLevel, authenticationDeviceAccessLevel, "AK", 321L, 1004L);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1, sps2));
        device = mockDevice("XAS", "10101010101011", deviceConfiguration, 1005L);

        KeyAccessor keyAccessor1 = mock(KeyAccessor.class);
        KeyAccessor keyAccessor2 = mock(KeyAccessor.class);
        when(device.getKeyAccessors()).thenReturn(Arrays.asList(keyAccessor1, keyAccessor2));
        KeyAccessorType keyAccessorType1 = sps1.getConfigurationSecurityProperties().get(0).getKeyAccessorType();
        KeyAccessorType keyAccessorType2 = sps2.getConfigurationSecurityProperties().get(0).getKeyAccessorType();
        when(keyAccessor1.getKeyAccessorType()).thenReturn(keyAccessorType1);
        when(keyAccessor2.getKeyAccessorType()).thenReturn(keyAccessorType2);
        List<KeyAccessorType> keyAccessorTypes = new ArrayList<>();

        sps1.getConfigurationSecurityProperties().stream().forEach(property -> keyAccessorTypes.add(property.getKeyAccessorType()));
        sps2.getConfigurationSecurityProperties().stream().forEach(property -> keyAccessorTypes.add(property.getKeyAccessorType()));
        when(device.getDeviceType().getKeyAccessorTypes()).thenReturn(keyAccessorTypes);
    }

    @Test
    public void testGetSingleKeyAccessorTypeWithFields() throws Exception {
        Response response = target("/devices/XAS/keyAccessorTypes/Password").queryParam("fields", "name").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<String>get("$.link")).isNull();
        assertThat(model.<String>get("$.id")).isNull();
        assertThat(model.<String>get("$.name")).isEqualTo("Password");
    }

    @Test
    public void testGetSingleKeyAccessorTypeAllFields() throws Exception {
        Response response = target("/devices/XAS/keyAccessorTypes/AK").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<String>get("$.link")).isNull();
        assertThat(model.<Integer>get("$.id")).isEqualTo(321);
        assertThat(model.<String>get("$.name")).isEqualTo("AK");
    }

    @Test
    public void testKeyAccessorTypeFields() throws Exception {
        Response response = target("/devices/x/keyAccessorTypes").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(3);
        assertThat(model.<List<String>>get("$")).containsOnly("id", "name", "description");
    }
}