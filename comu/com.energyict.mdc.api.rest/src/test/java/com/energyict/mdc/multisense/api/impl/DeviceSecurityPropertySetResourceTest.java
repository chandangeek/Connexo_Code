/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.common.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.common.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.common.protocol.security.RequestSecurityLevel;
import com.energyict.mdc.common.protocol.security.ResponseSecurityLevel;
import com.energyict.mdc.common.protocol.security.SecuritySuite;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceSecurityPropertySetResourceTest extends MultisensePublicApiJerseyTest {

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

        SecurityAccessor securityAccessor1 = mock(SecurityAccessor.class);
        SecurityAccessor securityAccessor2 = mock(SecurityAccessor.class);
        when(device.getSecurityAccessors()).thenReturn(Arrays.asList(securityAccessor1, securityAccessor2));
        SecurityAccessorType securityAccessorType1 = sps1.getConfigurationSecurityProperties().get(0).getSecurityAccessorType();
        SecurityAccessorType securityAccessorType2 = sps2.getConfigurationSecurityProperties().get(0).getSecurityAccessorType();
        when(securityAccessor1.getSecurityAccessorType()).thenReturn(securityAccessorType1);
        when(securityAccessor2.getSecurityAccessorType()).thenReturn(securityAccessorType2);
    }

    @Test
    public void testAllGetDeviceSecurityPropertySetsPaged() throws Exception {
        Response response = target("/devices/XAS/securitypropertysets").queryParam("start", 0).queryParam("limit", 10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/devices/XAS/securitypropertysets?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(2);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(5);
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/devices/XAS/securitypropertysets/5");
        assertThat(model.<Integer>get("data[1].id")).isEqualTo(6);
        assertThat(model.<String>get("data[1].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("data[1].link.href")).isEqualTo("http://localhost:9998/devices/XAS/securitypropertysets/6");
    }

    @Test
    public void testGetSingleDeviceSecurityPropertySetWithFields() throws Exception {
        Response response = target("/devices/XAS/securitypropertysets/5").queryParam("fields", "id,version").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(5);
        assertThat(model.<Integer>get("$.version")).isEqualTo(1003);
        assertThat(model.<String>get("$.link")).isNull();
        assertTrue(model.get("$.configuredSecurityPropertySet") == null);
    }

    @Test
    public void testGetSingleDeviceSecurityPropertySetAllFields() throws Exception {
        Response response = target("/devices/XAS/securitypropertysets/5").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(5);
        assertThat(model.<Integer>get("$.version")).isEqualTo(1003);
        assertThat(model.<String>get("$.link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("$.link.href")).isEqualTo("http://localhost:9998/devices/XAS/securitypropertysets/5");
        assertThat(model.<String>get("$.device.link.href")).isEqualTo("http://localhost:9998/devices/XAS");
        assertThat(model.<String>get("$.configuredSecurityPropertySet.link.href")).isEqualTo("http://localhost:9998/devicetypes/1/deviceconfigurations/2/securitypropertysets/5");
        assertThat(model.<String>get("$.properties[0].link.href")).isEqualTo("http://localhost:9998/devices/XAS/keyAccessors/Password");
    }

    @Test
    public void testDeviceSecurityPropertySetFields() throws Exception {
        Response response = target("/devices/XAS/securitypropertysets").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(6);
        assertThat(model.<List<String>>get("$")).containsOnly("configuredSecurityPropertySet", "device", "id", "link", "properties", "version");
    }

}
