/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.pki.SecurityAccessorType;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.common.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.common.protocol.security.RequestSecurityLevel;
import com.energyict.mdc.common.protocol.security.ResponseSecurityLevel;
import com.energyict.mdc.common.protocol.security.SecuritySuite;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author stijn
 * @since 07.06.17 - 10:26
 */
public class ConfigurationSecurityAccessorTypeResourceTest extends MultisensePublicApiJerseyTest {

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

        List<SecurityAccessorType> securityAccessorTypes = new ArrayList<>();
        sps1.getConfigurationSecurityProperties().stream().forEach(property -> securityAccessorTypes.add(property.getSecurityAccessorType()));
        sps2.getConfigurationSecurityProperties().stream().forEach(property -> securityAccessorTypes.add(property.getSecurityAccessorType()));
        when(deviceType.getSecurityAccessorTypes()).thenReturn(securityAccessorTypes);
    }


    @Test
    public void testGetAllKeyAccessorTypesPaged() throws Exception {
        Response response = target("/devicetypes/1/keyAccessorTypes").queryParam("start", 0).queryParam("limit", 10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/devicetypes/1/keyAccessorTypes?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(2);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(321);
        assertThat(model.<String>get("data[0].name")).isEqualTo("AK");
        assertThat(model.<Integer>get("data[1].id")).isEqualTo(123);
        assertThat(model.<String>get("data[1].name")).isEqualTo("Password");
    }

    @Test
    public void testGetSingleKeyAccessorTypeWithFields() throws Exception {
        Response response = target("/devicetypes/1/keyAccessorTypes/Password").queryParam("fields", "name").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<String>get("$.link")).isNull();
        assertThat(model.<String>get("$.id")).isNull();
        assertThat(model.<String>get("$.name")).isEqualTo("Password");
    }

    @Test
    public void testKeyAccessorTypeFields() throws Exception {
        Response response = target("/devicetypes/x/keyAccessorTypes").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(4);
        assertThat(model.<List<String>>get("$")).containsOnly("id", "name", "keyPurposeId", "description");
    }
}