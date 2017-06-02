/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;

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
import static org.mockito.Matchers.any;
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
        SecurityPropertySet securityPropertySet = mockSecurityPropertySet(13L, deviceConfiguration, "Zorro", encryptionDeviceAccessLevel, authenticationDeviceAccessLevel, 1003L);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet));
        PropertyInfo propertyInfo = new PropertyInfo("string.property", "string.property", new PropertyValueInfo<>("value", null), new PropertyTypeInfo(SimplePropertyType.TEXT, null, null, null), false);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Response response = target("/devicetypes/123/deviceconfigurations/456/securitypropertysets/13").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(13);
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("Zorro");
        assertThat(jsonModel.<String>get("$.link.params.rel")).isEqualTo("self");
        assertThat(jsonModel.<String>get("$.link.href")).isEqualTo("http://localhost:9998/devicetypes/123/deviceconfigurations/456/securitypropertysets/13");
        assertThat(jsonModel.<Integer>get("$.authenticationAccessLevel.id")).isEqualTo(1002);
        assertThat(jsonModel.<String>get("$.authenticationAccessLevel.link.href")).isEqualTo("http://localhost:9998/pluggableclasses/15129/authenticationaccesslevels/1002");
        assertThat(jsonModel.<Integer>get("$.encryptionAccessLevel.id")).isEqualTo(1001);
        assertThat(jsonModel.<String>get("$.encryptionAccessLevel.link.href")).isEqualTo("http://localhost:9998/pluggableclasses/15129/encryptionaccesslevels/1001");
        assertThat(jsonModel.<List>get("$.properties")).isNotEmpty();
        assertThat(jsonModel.<String>get("$.properties[0].key")).isEqualTo("string.property");
        assertThat(jsonModel.<String>get("$.properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");

    }

    @Test
    public void testGetSecurityPropertySetList() throws IOException {
        DeviceType deviceType = mockDeviceType(123, "sampleDeviceType", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(456, "Default", deviceType, 3333L);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));

        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mockEncryptionAccessLevel(1001);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mockAuthenticationAccessLevel(1002);
        PropertySpec stringPropertySpec13 = mockStringPropertySpec();
        SecurityPropertySet securityPropertySet = mockSecurityPropertySet(13L, deviceConfiguration, "Zorro", encryptionDeviceAccessLevel, authenticationDeviceAccessLevel, 103L);
        when(securityPropertySet.getPropertySpecs()).thenReturn(Collections.singleton(stringPropertySpec13));

        PropertySpec stringPropertySpec15 = mockStringPropertySpec();
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel15 = mockEncryptionAccessLevel(1003);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel15 = mockAuthenticationAccessLevel(1004);
        SecurityPropertySet securityPropertySet2 = mockSecurityPropertySet(15L, deviceConfiguration, "Alfa", encryptionDeviceAccessLevel15, authenticationDeviceAccessLevel15, 103L);
        when(securityPropertySet2.getPropertySpecs()).thenReturn(Collections.singleton(stringPropertySpec15));

        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(securityPropertySet, securityPropertySet2));
        PropertyInfo propertyInfo = new PropertyInfo("name", "name", new PropertyValueInfo<>("value", null), new PropertyTypeInfo(), false);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Response response = target("/devicetypes/123/deviceconfigurations/456/securitypropertysets").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.data[0].id")).isEqualTo(15);
        assertThat(jsonModel.<String>get("$.data[0].name")).isEqualTo("Alfa");
        assertThat(jsonModel.<String>get("$.data[0].link.params.rel")).isEqualTo("self");
        assertThat(jsonModel.<String>get("$.data[0].link.href")).isEqualTo("http://localhost:9998/devicetypes/123/deviceconfigurations/456/securitypropertysets/15");
        assertThat(jsonModel.<Integer>get("$.data[0].authenticationAccessLevel.id")).isEqualTo(1004);
        assertThat(jsonModel.<String>get("$.data[0].authenticationAccessLevel.link.href")).isEqualTo("http://localhost:9998/pluggableclasses/15129/authenticationaccesslevels/1004");
        assertThat(jsonModel.<Integer>get("$.data[0].encryptionAccessLevel.id")).isEqualTo(1003);
        assertThat(jsonModel.<String>get("$.data[0].encryptionAccessLevel.link.href")).isEqualTo("http://localhost:9998/pluggableclasses/15129/encryptionaccesslevels/1003");
        assertThat(jsonModel.<Integer>get("$.data[1].id")).isEqualTo(13);
        assertThat(jsonModel.<String>get("$.data[1].name")).isEqualTo("Zorro");
        assertThat(jsonModel.<String>get("$.data[1].link.params.rel")).isEqualTo("self");
        assertThat(jsonModel.<String>get("$.data[1].link.href")).isEqualTo("http://localhost:9998/devicetypes/123/deviceconfigurations/456/securitypropertysets/13");
        assertThat(jsonModel.<Integer>get("$.data[1].authenticationAccessLevel.id")).isEqualTo(1002);
        assertThat(jsonModel.<String>get("$.data[1].authenticationAccessLevel.link.href")).isEqualTo("http://localhost:9998/pluggableclasses/15129/authenticationaccesslevels/1002");
        assertThat(jsonModel.<Integer>get("$.data[1].encryptionAccessLevel.id")).isEqualTo(1001);
        assertThat(jsonModel.<String>get("$.data[1].encryptionAccessLevel.link.href")).isEqualTo("http://localhost:9998/pluggableclasses/15129/encryptionaccesslevels/1001");
    }

    @Test
    public void testFields() throws Exception {
        Response response = target("/devicetypes/x/deviceconfigurations/x/securitypropertysets").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(7);
        assertThat(model.<List<String>>get("$")).containsOnly("id","version", "name", "link", "authenticationAccessLevel", "encryptionAccessLevel", "properties");
    }

}