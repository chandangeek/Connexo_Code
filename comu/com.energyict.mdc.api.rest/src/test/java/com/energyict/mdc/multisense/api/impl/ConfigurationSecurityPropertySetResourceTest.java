/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.RequestSecurityLevel;
import com.energyict.mdc.protocol.api.security.ResponseSecurityLevel;
import com.energyict.mdc.protocol.api.security.SecuritySuite;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 10/5/15.
 */
public class ConfigurationSecurityPropertySetResourceTest extends MultisensePublicApiJerseyTest {

    @Test
    public void testSingleGetSecurityPropertySet() throws IOException {
        DeviceType deviceType = mockDeviceType(123, "sampleDeviceType", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(456, "Default", deviceType, 3333L);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mockEncryptionAccessLevel(1001);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mockAuthenticationAccessLevel(1002);
        SecurityPropertySet securityPropertySet = mockSecurityPropertySet(13L, deviceConfiguration, "Zorro", 1, encryptionDeviceAccessLevel, authenticationDeviceAccessLevel, "Password", 1234L, 1003L);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet));
        Response response = target("/devicetypes/123/deviceconfigurations/456/securitypropertysets/13").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(13);
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("Zorro");
        assertThat(jsonModel.<String>get("$.link.params.rel")).isEqualTo("self");
        assertThat(jsonModel.<String>get("$.link.href")).isEqualTo("http://localhost:9998/devicetypes/123/deviceconfigurations/456/securitypropertysets/13");
        assertThat(jsonModel.<Integer>get("$.client.propertyValueInfo.value")).isEqualTo(1);
        assertThat(jsonModel.<Integer>get("$.authenticationAccessLevel.id")).isEqualTo(1002);
        assertThat(jsonModel.<String>get("$.authenticationAccessLevel.link.href")).isEqualTo("http://localhost:9998/pluggableclasses/15129/authenticationaccesslevels/1002");
        assertThat(jsonModel.<Integer>get("$.encryptionAccessLevel.id")).isEqualTo(1001);
        assertThat(jsonModel.<String>get("$.encryptionAccessLevel.link.href")).isEqualTo("http://localhost:9998/pluggableclasses/15129/encryptionaccesslevels/1001");
        assertThat(jsonModel.<List>get("$.properties")).isNotEmpty();
        assertThat(jsonModel.<Integer>get("$.properties[0].id")).isEqualTo(1234);
        assertThat(jsonModel.<String>get("$.properties[0].link.href")).isEqualTo("http://localhost:9998/devicetypes/123/keyAccessorTypes/Password");
    }

    @Test
    public void testGetSecurityPropertySetList() throws IOException {
        DeviceType deviceType = mockDeviceType(123, "sampleDeviceType", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(456, "Default", deviceType, 3333L);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));

        SecuritySuite securitySuite = mockSecuritySuite(1000);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mockEncryptionAccessLevel(1001);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mockAuthenticationAccessLevel(1002);
        RequestSecurityLevel requestSecurityDeviceAccessLevel = mockRequestSecurityDeviceAccessLevel(1003);
        ResponseSecurityLevel responseSecurityDeviceAccessLevel = mockResponseSecurityDeviceAccessLevel(1004);
        PropertySpec stringPropertySpec13 = mockStringPropertySpec();
        SecurityPropertySet securityPropertySet = mockSecurityPropertySet(13L, deviceConfiguration, "Zorro", 1, securitySuite, encryptionDeviceAccessLevel, authenticationDeviceAccessLevel, requestSecurityDeviceAccessLevel, responseSecurityDeviceAccessLevel, "Password", 123L, 103L);
        when(securityPropertySet.getPropertySpecs()).thenReturn(Collections.singleton(stringPropertySpec13));

        PropertySpec stringPropertySpec15 = mockStringPropertySpec();
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel15 = mockEncryptionAccessLevel(1003);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel15 = mockAuthenticationAccessLevel(1004);
        SecurityPropertySet securityPropertySet2 = mockSecurityPropertySet(15L, deviceConfiguration, "Alfa", 2, encryptionDeviceAccessLevel15, authenticationDeviceAccessLevel15, "Password", 123L, 103L);
        when(securityPropertySet2.getPropertySpecs()).thenReturn(Collections.singleton(stringPropertySpec15));
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(securityPropertySet, securityPropertySet2));

        Response response = target("/devicetypes/123/deviceconfigurations/456/securitypropertysets").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.data[0].id")).isEqualTo(15);
        assertThat(jsonModel.<String>get("$.data[0].name")).isEqualTo("Alfa");
        assertThat(jsonModel.<String>get("$.data[0].link.params.rel")).isEqualTo("self");
        assertThat(jsonModel.<String>get("$.data[0].link.href")).isEqualTo("http://localhost:9998/devicetypes/123/deviceconfigurations/456/securitypropertysets/15");
        assertThat(jsonModel.<Integer>get("$.data[0].client.propertyValueInfo.value")).isEqualTo(2);
        assertThat(jsonModel.<Integer>get("$.data[0].authenticationAccessLevel.id")).isEqualTo(1004);
        assertThat(jsonModel.<String>get("$.data[0].authenticationAccessLevel.link.href")).isEqualTo("http://localhost:9998/pluggableclasses/15129/authenticationaccesslevels/1004");
        assertThat(jsonModel.<Integer>get("$.data[0].encryptionAccessLevel.id")).isEqualTo(1003);
        assertThat(jsonModel.<String>get("$.data[0].encryptionAccessLevel.link.href")).isEqualTo("http://localhost:9998/pluggableclasses/15129/encryptionaccesslevels/1003");
        assertThat(jsonModel.<Integer>get("$.data[1].id")).isEqualTo(13);
        assertThat(jsonModel.<String>get("$.data[1].name")).isEqualTo("Zorro");
        assertThat(jsonModel.<String>get("$.data[1].link.params.rel")).isEqualTo("self");
        assertThat(jsonModel.<String>get("$.data[1].link.href")).isEqualTo("http://localhost:9998/devicetypes/123/deviceconfigurations/456/securitypropertysets/13");
        assertThat(jsonModel.<Integer>get("$.data[1].client.propertyValueInfo.value")).isEqualTo(1);
        assertThat(jsonModel.<Integer>get("$.data[1].securitySuite.id")).isEqualTo(1000);
        assertThat(jsonModel.<String>get("$.data[1].securitySuite.link.href")).isEqualTo("http://localhost:9998/pluggableclasses/15129/securitysuites/1000");
        assertThat(jsonModel.<Integer>get("$.data[1].authenticationAccessLevel.id")).isEqualTo(1002);
        assertThat(jsonModel.<String>get("$.data[1].authenticationAccessLevel.link.href")).isEqualTo("http://localhost:9998/pluggableclasses/15129/authenticationaccesslevels/1002");
        assertThat(jsonModel.<Integer>get("$.data[1].encryptionAccessLevel.id")).isEqualTo(1001);
        assertThat(jsonModel.<String>get("$.data[1].encryptionAccessLevel.link.href")).isEqualTo("http://localhost:9998/pluggableclasses/15129/encryptionaccesslevels/1001");
        assertThat(jsonModel.<Integer>get("$.data[1].requestSecurityLevel.id")).isEqualTo(1003);
        assertThat(jsonModel.<String>get("$.data[1].requestSecurityLevel.link.href")).isEqualTo("http://localhost:9998/pluggableclasses/15129/requestsecurityaccesslevels/1003");
        assertThat(jsonModel.<Integer>get("$.data[1].responseSecurityLevel.id")).isEqualTo(1004);
        assertThat(jsonModel.<String>get("$.data[1].responseSecurityLevel.link.href")).isEqualTo("http://localhost:9998/pluggableclasses/15129/responsesecurityaccesslevels/1004");
    }

    @Test
    public void testFields() throws Exception {
        Response response = target("/devicetypes/x/deviceconfigurations/x/securitypropertysets").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(11);
        assertThat(model.<List<String>>get("$")).containsOnly("id", "version", "name", "link", "client", "authenticationAccessLevel", "encryptionAccessLevel", "securitySuite", "requestSecurityLevel", "responseSecurityLevel", "properties");
    }
}