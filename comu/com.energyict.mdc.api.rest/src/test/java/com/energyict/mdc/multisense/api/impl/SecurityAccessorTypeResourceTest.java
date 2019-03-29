/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.hsm.model.keys.HsmKeyType;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.SecurityAccessor;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.RequestSecurityLevel;
import com.energyict.mdc.protocol.api.security.ResponseSecurityLevel;
import com.energyict.mdc.protocol.api.security.SecuritySuite;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author stijn
 * @since 07.06.17 - 10:47
 */
public class SecurityAccessorTypeResourceTest extends MultisensePublicApiJerseyTest {

    private Device device;
    private DeviceType deviceType;
    private SecurityPropertySet sps1, sps2, sps3;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        deviceType = mockDeviceType(1L, "device type", 1001L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(2L, "device config", deviceType, 1002L);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mockAuthenticationAccessLevel(3);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mockEncryptionAccessLevel(4);
        SecuritySuite securitySuite = mockSecuritySuite(5);
        RequestSecurityLevel requestSecurityDeviceAccessLevel = mockRequestSecurityDeviceAccessLevel(6);
        ResponseSecurityLevel responseSecurityDeviceAccessLevel = mockResponseSecurityDeviceAccessLevel(7);

        sps1 = mockSecurityPropertySet(5L, deviceConfiguration, "sps1", 1, securitySuite, encryptionDeviceAccessLevel, authenticationDeviceAccessLevel, requestSecurityDeviceAccessLevel, responseSecurityDeviceAccessLevel, "Password", 123L, 1003L);
        sps2 = mockSecurityPropertySet(6L, deviceConfiguration, "sps2", 2, encryptionDeviceAccessLevel, authenticationDeviceAccessLevel, "AK", 321L, 1004L);
        sps3 = mockSecurityPropertySet(7L, deviceConfiguration, "sps3", 3, encryptionDeviceAccessLevel, authenticationDeviceAccessLevel, "HSM", 322L, 1005L);

        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1, sps2, sps3));
        device = mockDevice("XAS", "10101010101011", deviceConfiguration, 1005L);

        SecurityAccessor securityAccessor1 = mock(SecurityAccessor.class);
        SecurityAccessor securityAccessor2 = mock(SecurityAccessor.class);
        SecurityAccessor securityAccessor3 = mock(SecurityAccessor.class);
        when(device.getSecurityAccessors()).thenReturn(Arrays.asList(securityAccessor1, securityAccessor2, securityAccessor3));
        SecurityAccessorType securityAccessorType1 = sps1.getConfigurationSecurityProperties().get(0).getSecurityAccessorType();
        SecurityAccessorType securityAccessorType2 = sps2.getConfigurationSecurityProperties().get(0).getSecurityAccessorType();
        SecurityAccessorType securityAccessorType3 = sps3.getConfigurationSecurityProperties().get(0).getSecurityAccessorType();
        when(securityAccessor1.getKeyAccessorType()).thenReturn(securityAccessorType1);
        when(securityAccessor2.getKeyAccessorType()).thenReturn(securityAccessorType2);
        when(securityAccessor3.getKeyAccessorType()).thenReturn(securityAccessorType3);
        List<SecurityAccessorType> securityAccessorTypes = new ArrayList<>();

        sps1.getConfigurationSecurityProperties().stream().forEach(property -> securityAccessorTypes.add(property.getSecurityAccessorType()));
        sps2.getConfigurationSecurityProperties().stream().forEach(property -> securityAccessorTypes.add(property.getSecurityAccessorType()));
        sps3.getConfigurationSecurityProperties().stream().forEach(property -> securityAccessorTypes.add(property.getSecurityAccessorType()));
        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getSecurityAccessorTypes()).thenReturn(securityAccessorTypes);
        when(device.getDeviceType().getSecurityAccessorTypes()).thenReturn(securityAccessorTypes);
        when(device.getSecurityAccessor(Mockito.any(SecurityAccessorType.class))).thenReturn(Optional.of(securityAccessor2));
        when(securityAccessor2.getActualValue()).thenReturn(Optional.of("ABCD"));
    }

    @Test
    public void testGetAllKeyAccessorTypesPaged() throws Exception {
        Response response = target("/devices/XAS/keyAccessorTypes").queryParam("start", 0).queryParam("limit", 10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/devices/XAS/keyAccessorTypes?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(3);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(321);
        assertThat(model.<String>get("data[0].name")).isEqualTo("AK");
        assertThat(model.<Integer>get("data[1].id")).isEqualTo(322);
        assertThat(model.<String>get("data[1].name")).isEqualTo("HSM");
        assertThat(model.<Integer>get("data[2].id")).isEqualTo(123);
        assertThat(model.<String>get("data[2].name")).isEqualTo("Password");
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
    public void testValidateKeyAccessorType() throws Exception {
        Response response = target("/devices/XAS/keyAccessorTypes/AK/validate").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testMarkServiceKey() throws Exception {
        Response response = target("/devices/XAS/keyAccessorTypes/AK/markservicekey").request("application/json").put(Entity
                .json(""));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testUnMarkServiceKey() throws Exception {
        Response response = target("/devices/XAS/keyAccessorTypes/AK/unmarkservicekey").request("application/json").put(Entity
                .json(""));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testStoreTempValue() throws Exception {
        PropertySpec propertySpec1 = mockStringPropertySpec("key", "123");
        PropertySpec propertySpec2 = mockStringPropertySpec("label", "asd");
        SecurityAccessor securityAccessor = mockSecurityAccessor("HSM", propertySpec1, propertySpec2);
        SecurityAccessorType securityAccessorType = mockSecuritySecurityAccessorType("HSM");
        HsmKeyType hsmKeyType = mock(HsmKeyType.class);
        when(securityAccessorType.getHsmKeyType()).thenReturn(hsmKeyType);
        when(hsmKeyType.getLabel()).thenReturn("label");
        when(securityAccessor.getKeyAccessorType()).thenReturn(securityAccessorType);
        SecurityValueWrapper mockSecurityValueWrapper = mock(SecurityValueWrapper.class);
        when(securityAccessor.getTempValue()).thenReturn(Optional.of(mockSecurityValueWrapper));
        when(device.getSecurityAccessor(any(SecurityAccessorType.class))).thenReturn(Optional.of(securityAccessor));
        KeyInfo info = new KeyInfo("FFFFFFFF");
        Response response = target("/devices/XAS/keyAccessorTypes/HSM/storetempvalue").request("application/json").put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testKeyAccessorTypeFields() throws Exception {
        Response response = target("/devices/x/keyAccessorTypes").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(3);
        assertThat(model.<List<String>>get("$")).containsOnly("id", "name", "description");
    }
}