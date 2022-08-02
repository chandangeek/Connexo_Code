package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;

import org.fest.assertions.core.Condition;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link WavenisSecuritySupport} component
 * <p>
 * Copyrights EnergyICT
 * Date: 11/01/13
 * Time: 16:25
 */
@RunWith(MockitoJUnitRunner.class)
public class WavenisSecuritySupportTest extends AbstractSecuritySupportTest {

    @Test
    public void getSecurityPropertiesTest() {
        WavenisSecuritySupport wavenisSecuritySupport = new WavenisSecuritySupport(propertySpecService);

        // assert that you only have two property to set
        assertThat(wavenisSecuritySupport.getSecurityProperties()).hasSize(2);

        // check for the password propertySpec
        Optional<PropertySpec> passwordPropertySpec = wavenisSecuritySupport.getSecurityPropertySpec(SecurityPropertySpecTranslationKeys.PASSWORD.getKey());
        assertPropertySpecsEqual(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService), passwordPropertySpec);

        // check for the encryptionKey propertySpec
        Optional<PropertySpec> encryptionKeyPropertySpec = wavenisSecuritySupport.getSecurityPropertySpec(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.getKey());
        assertTrue(encryptionKeyPropertySpec.isPresent());
        assertEquals(encryptionKeyPropertySpec.get().getName(), SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.getKey());
    }

    @Test
    public void getAuthenticationAccessLevelsTest() {
        WavenisSecuritySupport wavenisSecuritySupport = new WavenisSecuritySupport(propertySpecService);

        // assert that you only have one authentication level
        assertThat(wavenisSecuritySupport.getAuthenticationAccessLevels()).hasSize(1);


        // check for the simple authentication level
        assertThat(wavenisSecuritySupport.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(WavenisSecuritySupport.StandardAuthenticationAccessLevel.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
    }

    @Test
    public void getEncryptionAccessLevelsTest() {
        WavenisSecuritySupport wavenisSecuritySupport = new WavenisSecuritySupport(propertySpecService);

        // assert that you only have one authentication level
        assertThat(wavenisSecuritySupport.getEncryptionAccessLevels()).hasSize(1);


        // check for the simple authentication level
        assertThat(wavenisSecuritySupport.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(WavenisSecuritySupport.StandardEncryptionAccessLevel.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
    }

    @Test
    public void convertToTypedPropertiesTest() {
        WavenisSecuritySupport wavenisSecuritySupport = new WavenisSecuritySupport(propertySpecService);
        final TypedProperties securityProperties = TypedProperties.empty();

        String passwordValue = "MyPassword";
        String encryptionKey = "MyEncryptionKey";
        securityProperties.setProperty(SecurityPropertySpecTranslationKeys.PASSWORD.toString(), passwordValue);
        securityProperties.setProperty(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString(), encryptionKey);

        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet =
                new DeviceProtocolSecurityPropertySet() {
                    @Override
                    public String getName() {
                        return "security";
                    }

                    @Override
                    public Object getClient() {
                        return null;
                    }

                    @Override
                    public int getAuthenticationDeviceAccessLevel() {
                        return 0;
                    }

                    @Override
                    public int getEncryptionDeviceAccessLevel() {
                        return 0;
                    }

                    @Override
                    public TypedProperties getSecurityProperties() {
                        return securityProperties;
                    }
                };

        // business method
        TypedProperties legacyProperties = TypedProperties.copyOf(wavenisSecuritySupport.convertToTypedProperties(deviceProtocolSecurityPropertySet));

        // asserts
        assertNotNull(legacyProperties);
        assertThat(legacyProperties.getProperty("SecurityLevel")).isEqualTo("0");
        assertThat(legacyProperties.getProperty("Password")).isEqualTo(passwordValue);
        assertThat(legacyProperties.getProperty("WavenisEncryptionKey")).isEqualTo(encryptionKey);
    }

    @Test
    public void testConvertToSecurityPropertySet() throws Exception {
        WavenisSecuritySupport wavenisSecuritySupport = new WavenisSecuritySupport(propertySpecService);
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty("SecurityLevel", "1");
        securityProperties.setProperty("WavenisEncryptionKey", "2");

        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = wavenisSecuritySupport.convertFromTypedProperties(securityProperties);
        assertThat(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(1);
        assertThat(deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(2);
    }

    @Test
    public void testConvertToSecurityPropertySetMissingSecurityLevel() throws Exception {
        WavenisSecuritySupport wavenisSecuritySupport = new WavenisSecuritySupport(propertySpecService);
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty("WavenisEncryptionKey", "2");

        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = wavenisSecuritySupport.convertFromTypedProperties(securityProperties);
        assertThat(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(0);
        assertThat(deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(2);
    }

    @Test
    public void testConvertToSecurityPropertySetMissingEncryptionKey() throws Exception {
        WavenisSecuritySupport wavenisSecuritySupport = new WavenisSecuritySupport(propertySpecService);
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty("SecurityLevel", "1");

        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = wavenisSecuritySupport.convertFromTypedProperties(securityProperties);
        assertThat(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(1);
        assertThat(deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(0);
    }
}
