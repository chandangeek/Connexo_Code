package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.jayway.jsonpath.JsonModel;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        PropertySpec stringPropertySpec = mockStringPropertySpec();
        when(securityPropertySet.getId()).thenReturn(13L);
        when(securityPropertySet.getName()).thenReturn("Zorro");
        when(securityPropertySet.getPropertySpecs()).thenReturn(Collections.singleton(stringPropertySpec));
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mockEncryptionAccessLevel(1001);
        when(securityPropertySet.getEncryptionDeviceAccessLevel()).thenReturn(encryptionDeviceAccessLevel);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mockAuthenticationAccessLevel(1002);
        when(securityPropertySet.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationDeviceAccessLevel);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Collections.singletonList(securityPropertySet));
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

        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        PropertySpec stringPropertySpec13 = mockStringPropertySpec();
        when(securityPropertySet.getId()).thenReturn(13L);
        when(securityPropertySet.getName()).thenReturn("Zorro");
        when(securityPropertySet.getPropertySpecs()).thenReturn(Collections.singleton(stringPropertySpec13));
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mockEncryptionAccessLevel(1001);
        when(securityPropertySet.getEncryptionDeviceAccessLevel()).thenReturn(encryptionDeviceAccessLevel);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mockAuthenticationAccessLevel(1002);
        when(securityPropertySet.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationDeviceAccessLevel);

        SecurityPropertySet securityPropertySet2 = mock(SecurityPropertySet.class);
        when(securityPropertySet2.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        PropertySpec stringPropertySpec15 = mockStringPropertySpec();
        when(securityPropertySet2.getId()).thenReturn(15L);
        when(securityPropertySet2.getName()).thenReturn("Alfa");
        when(securityPropertySet2.getPropertySpecs()).thenReturn(Collections.singleton(stringPropertySpec15));
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel15 = mockEncryptionAccessLevel(1003);
        when(securityPropertySet2.getEncryptionDeviceAccessLevel()).thenReturn(encryptionDeviceAccessLevel15);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel15 = mockAuthenticationAccessLevel(1004);
        when(securityPropertySet2.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationDeviceAccessLevel15);

        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(securityPropertySet, securityPropertySet2));

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