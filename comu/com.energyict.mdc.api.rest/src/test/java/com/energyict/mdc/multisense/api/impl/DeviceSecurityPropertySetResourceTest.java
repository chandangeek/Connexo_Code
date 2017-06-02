/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.KeyAccessor;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceSecurityPropertySetResourceTest extends MultisensePublicApiJerseyTest {

    Clock clock = Clock.fixed(Instant.ofEpochSecond(1448841600), ZoneId.systemDefault());
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
        sps1 = mockSecurityPropertySet(5L, deviceConfiguration, "sps1", 1, encryptionDeviceAccessLevel, authenticationDeviceAccessLevel, "Password", 123L, 1003L);
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
    }

    @Test
    public void testAllGetDeviceSecurityPropertySetsPaged() throws Exception {
        PropertyInfo propertyInfo = new PropertyInfo("name", "name", new PropertyValueInfo<>("value", null), new PropertyTypeInfo(), false);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Response response = target("/devices/XAS/securitypropertysets").queryParam("start",0).queryParam("limit",10).request().get();
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
        Response response = target("/devices/XAS/securitypropertysets/5").queryParam("fields","id,version").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(5);
        assertThat(model.<Integer>get("$.version")).isEqualTo(1003);
        assertThat(model.<String>get("$.complete")).isNull();
        assertThat(model.<String>get("$.link")).isNull();
        assertTrue(model.get("$.configuredSecurityPropertySet") == null);
    }

    @Test
    public void testGetSingleDeviceSecurityPropertySetAllFields() throws Exception {
        when(propertyValueInfoService.getPropertyInfo(any(PropertySpec.class), any(Function.class))).thenAnswer(invocation -> {
            String propertyName = invocation.getArgumentAt(0, PropertySpec.class).getName();
            String propertyValue =  invocation.getArguments()[1] != null ? String.valueOf(((Function) invocation.getArguments()[1]).apply(propertyName)) : null;
            return new PropertyInfo(propertyName, propertyName, new PropertyValueInfo<>(propertyValue, null), new PropertyTypeInfo(), false);
        });

        Response response = target("/devices/XAS/securitypropertysets/5").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(5);
        assertThat(model.<Integer>get("$.version")).isEqualTo(1003);
        assertThat(model.<String>get("$.link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("$.link.href")).isEqualTo("http://localhost:9998/devices/XAS/securitypropertysets/5");
        assertThat(model.<String>get("$.device.link.href")).isEqualTo("http://localhost:9998/devices/XAS");
        assertThat(model.<String>get("$.client.propertyValueInfo.value")).isEqualTo("1");
        assertThat(model.<String>get("$.configuredSecurityPropertySet.link.href")).isEqualTo("http://localhost:9998/devicetypes/1/deviceconfigurations/2/securitypropertysets/5");
        assertThat(model.<String>get("$.properties[0].link.href")).isEqualTo("http://localhost:9998/devices/XAS/keyAccessors/Password");
    }

    @Test
    public void testDeviceSecurityPropertySetFields() throws Exception {
        Response response = target("/devices/XAS/securitypropertysets").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(7);
        assertThat(model.<List<String>>get("$")).containsOnly("configuredSecurityPropertySet","device","id","link", "client", "properties","version");
    }

}