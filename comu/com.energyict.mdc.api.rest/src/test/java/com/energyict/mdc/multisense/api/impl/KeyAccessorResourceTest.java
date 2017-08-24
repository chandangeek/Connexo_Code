/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
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
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author stijn
 * @since 07.06.17 - 09:47
 */
public class KeyAccessorResourceTest extends MultisensePublicApiJerseyTest {

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
        when(keyAccessor1.getDevice()).thenReturn(device);
        when(keyAccessor2.getDevice()).thenReturn(device);
        when(keyAccessor1.getKeyAccessorType()).thenReturn(keyAccessorType1);
        when(keyAccessor2.getKeyAccessorType()).thenReturn(keyAccessorType2);
    }


    @Test
    public void testGetAllKeyAccessorsPaged() throws Exception {
        Response response = target("/devices/XAS/keyAccessors").queryParam("start", 0).queryParam("limit", 10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/devices/XAS/keyAccessors?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(2);
        assertThat(model.<String>get("data[0].name")).isEqualTo("AK");
        assertThat(model.<Integer>get("data[0].keyAccessorType.id")).isEqualTo(321);
        assertThat(model.<String>get("data[0].keyAccessorType.link.params.rel")).isEqualTo(Relation.REF_RELATION.rel());
        assertThat(model.<String>get("data[0].keyAccessorType.link.href")).isEqualTo("http://localhost:9998/devicetypes/1/keyAccessorTypes/AK");
        assertThat(model.<String>get("data[1].name")).isEqualTo("Password");
        assertThat(model.<Integer>get("data[1].keyAccessorType.id")).isEqualTo(123);
        assertThat(model.<String>get("data[1].keyAccessorType.link.params.rel")).isEqualTo(Relation.REF_RELATION.rel());
        assertThat(model.<String>get("data[1].keyAccessorType.link.href")).isEqualTo("http://localhost:9998/devicetypes/1/keyAccessorTypes/Password");
    }

    @Test
    public void testGetSingleKeyAccessorWithFields() throws Exception {
        Response response = target("/devices/XAS/keyAccessors/Password").queryParam("fields", "name").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<String>get("$.link")).isNull();
        assertThat(model.<String>get("$.name")).isEqualTo("Password");
        assertThat(model.<String>get("$.keyAccessorType")).isNull();
    }

    @Test
    public void testKeyAccessorFields() throws Exception {
        Response response = target("/devices/x/keyAccessors").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(2);
        assertThat(model.<List<String>>get("$")).containsOnly("keyAccessorType", "name");
    }
}