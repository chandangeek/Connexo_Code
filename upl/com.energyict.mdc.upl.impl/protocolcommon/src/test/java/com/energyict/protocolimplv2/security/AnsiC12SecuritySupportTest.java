package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import com.energyict.protocolimpl.properties.TypedProperties;
import org.fest.assertions.core.Condition;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for the {@link AnsiC12SecuritySupport} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/01/13
 * Time: 14:27
 */
public class AnsiC12SecuritySupportTest {

    @Test
    public void getSecurityPropertiesTest() {
        AnsiC12SecuritySupport ansiC12SecuritySupport = new AnsiC12SecuritySupport();

        // currently only 3 properties are necessary
        assertThat(ansiC12SecuritySupport.getSecurityProperties()).hasSize(4);

        // check for the password propertySpec
        assertThat(ansiC12SecuritySupport.getSecurityProperties()).areExactly(1, new Condition<PropertySpec>() {
            @Override
            public boolean matches(PropertySpec propertySpec) {
                return propertySpec.equals(DeviceSecurityProperty.PASSWORD.getPropertySpec());
            }
        });
        // check for the binaryPassword propertySpec
        assertThat(ansiC12SecuritySupport.getSecurityProperties()).areExactly(1, new Condition<PropertySpec>() {
            @Override
            public boolean matches(PropertySpec propertySpec) {
                return propertySpec.equals(DeviceSecurityProperty.BINARY_PASSWORD.getPropertySpec());
            }
        });
        // check for the ANSI C12 user propertySpec
        assertThat(ansiC12SecuritySupport.getSecurityProperties()).areExactly(1, new Condition<PropertySpec>() {
            @Override
            public boolean matches(PropertySpec propertySpec) {
                return propertySpec.equals(DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec());
            }
        });
        // check for the ANSI C12 userId propertySpec
        assertThat(ansiC12SecuritySupport.getSecurityProperties()).areExactly(1, new Condition<PropertySpec>() {
            @Override
            public boolean matches(PropertySpec propertySpec) {
                return propertySpec.equals(DeviceSecurityProperty.ANSI_C12_USER_ID.getPropertySpec());
            }
        });
    }

    @Test
    public void getAuthenticationAccessLevelsTest() {
        AnsiC12SecuritySupport ansiC12SecuritySupport = new AnsiC12SecuritySupport();

        // currently only 3 levels are supported
        assertThat(ansiC12SecuritySupport.getAuthenticationAccessLevels()).hasSize(3);

        // check for no authentication level
        assertThat(ansiC12SecuritySupport.getAuthenticationAccessLevels()).areExactly(1, new Condition<AuthenticationDeviceAccessLevel>() {
            @Override
            public boolean matches(AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel) {
                return authenticationDeviceAccessLevel.getClass().isAssignableFrom(AnsiC12SecuritySupport.UnRestrictedAuthentication.class);
            }
        });

        // check for level one authentication
        assertThat(ansiC12SecuritySupport.getAuthenticationAccessLevels()).areExactly(1, new Condition<AuthenticationDeviceAccessLevel>() {
            @Override
            public boolean matches(AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel) {
                return authenticationDeviceAccessLevel.getClass().isAssignableFrom(AnsiC12SecuritySupport.RestrictedAuthentication.class);
            }
        });
        // check for level two authentication
        assertThat(ansiC12SecuritySupport.getAuthenticationAccessLevels()).areExactly(1, new Condition<AuthenticationDeviceAccessLevel>() {
            @Override
            public boolean matches(AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel) {
                return authenticationDeviceAccessLevel.getClass().isAssignableFrom(AnsiC12SecuritySupport.ReadOnlyAuthentication.class);
            }
        });

    }

    @Test
    public void getEncryptionAccessLevelsTest() {
        AnsiC12SecuritySupport ansiC12SecuritySupport = new AnsiC12SecuritySupport();

        // currently no encryption levels are supported
        assertThat(ansiC12SecuritySupport.getEncryptionAccessLevels()).isEmpty();
    }

    @Test
    public void convertToTypedPropertiesTest() {
        AnsiC12SecuritySupport ansiC12SecuritySupport = new AnsiC12SecuritySupport();
        final TypedProperties securityProperties = TypedProperties.empty();
        String ansiC12UserIdValue = "MyAnsiC12UserId";
        securityProperties.setProperty(SecurityPropertySpecName.ANSI_C12_USER_ID.toString(), ansiC12UserIdValue);
        String ansiC12UserValue = "MyAnsiC12User";
        securityProperties.setProperty(SecurityPropertySpecName.ANSI_C12_USER.toString(), ansiC12UserValue);
        String passwordValue = "MyPassword";
        securityProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), passwordValue);
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet =
                new DeviceProtocolSecurityPropertySet() {
                    @Override
                    public int getAuthenticationDeviceAccessLevel() {
                        return 1;
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
        com.energyict.mdc.upl.properties.TypedProperties legacyProperties = ansiC12SecuritySupport.convertToTypedProperties(deviceProtocolSecurityPropertySet);

        // asserts
        assertNotNull(legacyProperties);
        assertThat(legacyProperties.getProperty("SecurityLevel")).isEqualTo("1");
        assertThat(legacyProperties.getProperty("C12UserId")).isEqualTo(ansiC12UserIdValue);
        assertThat(legacyProperties.getProperty("C12User")).isEqualTo(ansiC12UserValue);
        assertThat(legacyProperties.getProperty("Password")).isEqualTo(passwordValue);
    }

    @Test
    public void testConvertToSecurityPropertySet() throws Exception {
        AnsiC12SecuritySupport ansiC12SecuritySupport = new AnsiC12SecuritySupport();
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty("SecurityLevel", "6");
        securityProperties.setProperty(DeviceSecurityProperty.PASSWORD.getPropertySpec().getName(), "1pwd2");

        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = ansiC12SecuritySupport.convertFromTypedProperties(securityProperties);
        assertThat(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(6);
        assertThat(deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID);
    }

    @Test
    public void testConvertToSecurityPropertySetMissingSecurityLevel() throws Exception {
        AnsiC12SecuritySupport ansiC12SecuritySupport = new AnsiC12SecuritySupport();
        TypedProperties securityProperties = TypedProperties.empty();

        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = ansiC12SecuritySupport.convertFromTypedProperties(securityProperties);
        assertThat(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(1);
        assertThat(deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID);
    }
}
