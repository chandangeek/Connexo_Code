package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import org.fest.assertions.core.Condition;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

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
        AnsiC12SecuritySupport iec1107SecuritySupport = new AnsiC12SecuritySupport();

        // currently only 3 properties are necessary
        assertThat(iec1107SecuritySupport.getSecurityProperties()).hasSize(3);

        // check for the password propertySpec
        assertThat(iec1107SecuritySupport.getSecurityProperties()).areExactly(1, new Condition<PropertySpec>() {
            @Override
            public boolean matches(PropertySpec propertySpec) {
                return propertySpec.equals(DeviceSecurityProperty.PASSWORD.getPropertySpec());
            }
        });
        // check for the ANSI C12 user propertySpec
        assertThat(iec1107SecuritySupport.getSecurityProperties()).areExactly(1, new Condition<PropertySpec>() {
            @Override
            public boolean matches(PropertySpec propertySpec) {
                return propertySpec.equals(DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec());
            }
        });
        // check for the ANSI C12 userId propertySpec
        assertThat(iec1107SecuritySupport.getSecurityProperties()).areExactly(1, new Condition<PropertySpec>() {
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
}
