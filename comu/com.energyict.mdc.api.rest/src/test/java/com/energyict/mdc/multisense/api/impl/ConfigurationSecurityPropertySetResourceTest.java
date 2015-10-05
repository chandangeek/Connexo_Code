package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.jayway.jsonpath.JsonModel;
import junit.framework.TestCase;
import org.junit.Test;

import javax.ws.rs.core.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
        DeviceType deviceType = mockDeviceType(123, "sampleDeviceType");
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(456, "Default", deviceType);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        PropertySpec stringPropertySpec = mockStringPropertySpec();
        when(securityPropertySet.getId()).thenReturn(13L);
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
        assertThat(jsonModel.<String>get("$.link.params.rel")).isEqualTo("self");
        assertThat(jsonModel.<String>get("$.link.href")).isEqualTo("http://localhost:9998/devicetypes/123/deviceconfigurations/456/securitypropertysets/13");
        assertThat(jsonModel.<Integer>get("$.authenticationAccessLevel.id")).isEqualTo(1002);
        assertThat(jsonModel.<String>get("$.authenticationAccessLevel.link.href")).isEqualTo("http://localhost:9998/pluggableclasses/15129/authenticationaccesslevels/1002");
        assertThat(jsonModel.<Integer>get("$.encryptionAccessLevel.id")).isEqualTo(1001);
        assertThat(jsonModel.<String>get("$.encryptionAccessLevel.link.href")).isEqualTo("http://localhost:9998/pluggableclasses/15129/encryptionaccesslevels/1001");

    }

}