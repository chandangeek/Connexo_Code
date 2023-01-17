/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.hsm.model.Message;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.properties.PropertySpec;
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
import com.energyict.mdc.upl.TypedProperties;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author stijn
 * @since 07.06.17 - 09:47
 */
public class SecurityAccessorResourceTest extends MultisensePublicApiJerseyTest {

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

        PropertySpec propertySpec1 = mockStringPropertySpec("key", "123");
        PropertySpec propertySpec2 = mockStringPropertySpec("label", "asd");
        SecurityAccessor securityAccessor1 = mockSecurityAccessor("sa1", propertySpec1, propertySpec2);
        SecurityAccessor securityAccessor2 = mockSecurityAccessor("sa2", propertySpec1);
        when(device.getSecurityAccessors()).thenReturn(Arrays.asList(securityAccessor1, securityAccessor2));
        SecurityAccessorType securityAccessorType1 = sps1.getConfigurationSecurityProperties().get(0).getSecurityAccessorType();
        SecurityAccessorType securityAccessorType2 = sps2.getConfigurationSecurityProperties().get(0).getSecurityAccessorType();
        when(securityAccessor1.getDevice()).thenReturn(device);
        when(securityAccessor2.getDevice()).thenReturn(device);
        when(securityAccessor1.getSecurityAccessorType()).thenReturn(securityAccessorType1);
        when(securityAccessor2.getSecurityAccessorType()).thenReturn(securityAccessorType2);

        Message message = new Message("test");
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty("key", "123");
        typedProperties.setProperty("label", "asd");
        when(securityAccessorInfoFactory.getPropertiesActualValue(Mockito.any(SecurityAccessor.class))).thenReturn(typedProperties);
        when(hsmEnergyService.prepareServiceKey(anyString(), anyString(), anyString())).thenReturn(message);
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
    public void testWrapServiceKeyValue() throws Exception {
        HashMap<String, String> info = new HashMap();
        info.put("value", "ABCDABCDABCDABCDABCDABCDABCDABCD");
        Response response = target("/devices/XAS/keyAccessors/Password/wrapServiceKeyValue").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
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
