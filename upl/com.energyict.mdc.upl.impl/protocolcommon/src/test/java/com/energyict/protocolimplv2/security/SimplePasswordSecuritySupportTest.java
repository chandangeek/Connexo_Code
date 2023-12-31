package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;

import org.fest.assertions.core.Condition;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the {@link SimplePasswordSecuritySupport} component
 * <p>
 * Copyrights EnergyICT
 * Date: 11/01/13
 * Time: 14:56
 */
public class SimplePasswordSecuritySupportTest extends AbstractSecuritySupportTest {

    @Test
    public void getSecurityPropertiesTest() {
        SimplePasswordSecuritySupport simplePasswordSecuritySupport = new SimplePasswordSecuritySupport(propertySpecService);

        // assert that you only have one property to set
        assertThat(simplePasswordSecuritySupport.getSecurityProperties()).hasSize(1);

        // check for the password propertySpec
        Optional<PropertySpec> passwordPropertySpec = simplePasswordSecuritySupport.getSecurityPropertySpec(SecurityPropertySpecTranslationKeys.PASSWORD.getKey());
        assertPropertySpecsEqual(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService), passwordPropertySpec);
    }

    @Test
    public void getAuthenticationAccessLevelsTest() {
        SimplePasswordSecuritySupport simplePasswordSecuritySupport = new SimplePasswordSecuritySupport(propertySpecService);

        // assert that you only have one authentication level
        assertThat(simplePasswordSecuritySupport.getAuthenticationAccessLevels()).hasSize(1);

        // check for the simple authentication level
        assertThat(simplePasswordSecuritySupport.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(SimplePasswordSecuritySupport.SimpleAuthentication.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
    }

    @Test
    public void getEncryptionAccessLevelsTest() {
        SimplePasswordSecuritySupport simplePasswordSecuritySupport = new SimplePasswordSecuritySupport(propertySpecService);

        // assert that we don't have any encryption level
        assertThat(simplePasswordSecuritySupport.getEncryptionAccessLevels()).isEmpty();
    }

    @Test
    public void convertToTypedPropertiesTest() {
        SimplePasswordSecuritySupport simplePasswordSecuritySupport = new SimplePasswordSecuritySupport(propertySpecService);
        final TypedProperties securityProperties = TypedProperties.empty();
        String passwordValue = "MyPassword";
        securityProperties.setProperty(SecurityPropertySpecTranslationKeys.PASSWORD.toString(), passwordValue);

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
                        return DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;
                    }

                    @Override
                    public TypedProperties getSecurityProperties() {
                        return securityProperties;
                    }
                };

        // business method
        TypedProperties legacyProperties = simplePasswordSecuritySupport.convertToTypedProperties(deviceProtocolSecurityPropertySet);

        // asserts
        assertNotNull(legacyProperties);
        assertThat(legacyProperties.getProperty("Password")).isEqualTo(passwordValue);
    }

    @Test
    public void testConvertFromTypedProperties() throws Exception {
        SimplePasswordSecuritySupport simplePasswordSecuritySupport = new SimplePasswordSecuritySupport(propertySpecService);
        TypedProperties securityProperties = TypedProperties.empty();
        String passwordValue = "MyPassword";
        securityProperties.setProperty(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService).getName(), passwordValue);

        DeviceProtocolSecurityPropertySet securityPropertySet = simplePasswordSecuritySupport.convertFromTypedProperties(securityProperties);
        assertThat(securityPropertySet).isNotNull();
        assertThat(securityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(0);
        assertThat(securityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(-1);
        assertThat(securityPropertySet.getSecurityProperties()).isNotNull();
        assertThat(securityPropertySet.getSecurityProperties().size()).isEqualTo(1);
        assertThat(securityPropertySet.getSecurityProperties().getProperty(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService).getName())).isEqualTo("MyPassword");
    }
}
