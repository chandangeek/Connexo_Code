package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import org.fest.assertions.core.Condition;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link IEC1107SecuritySupport} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/01/13
 * Time: 11:29
 */
public class IEC1107SecuritySupportTest {

    @Test
    public void getSecurityPropertiesTest() {
        IEC1107SecuritySupport iec1107SecuritySupport = new IEC1107SecuritySupport();

        // currently only 4 properties are necessary
        assertThat(iec1107SecuritySupport.getSecurityProperties()).hasSize(1);

        // check for the password propertySpec
        assertThat(iec1107SecuritySupport.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.equals(DeviceSecurityProperty.PASSWORD.getPropertySpec())) {
                        match |= true;
                    }
                }
                return match;
            }
        });
    }

    @Test
    public void getAuthenticationAccessLevelsTest() {
        IEC1107SecuritySupport iec1107SecuritySupport = new IEC1107SecuritySupport();

        // currently only 4 levels are supported
        assertThat(iec1107SecuritySupport.getAuthenticationAccessLevels()).hasSize(4);

        // check for no authentication level
        assertThat(iec1107SecuritySupport.getAuthenticationAccessLevels()).areExactly(1, new Condition<AuthenticationDeviceAccessLevel>() {
            @Override
            public boolean matches(AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel) {
                return authenticationDeviceAccessLevel.getClass().isAssignableFrom(IEC1107SecuritySupport.NoAuthentication.class);
            }
        });

        // check for level one authentication
        assertThat(iec1107SecuritySupport.getAuthenticationAccessLevels()).areExactly(1, new Condition<AuthenticationDeviceAccessLevel>() {
            @Override
            public boolean matches(AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel) {
                return authenticationDeviceAccessLevel.getClass().isAssignableFrom(IEC1107SecuritySupport.LevelOneAuthentication.class);
            }
        });
        // check for level two authentication
        assertThat(iec1107SecuritySupport.getAuthenticationAccessLevels()).areExactly(1, new Condition<AuthenticationDeviceAccessLevel>() {
            @Override
            public boolean matches(AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel) {
                return authenticationDeviceAccessLevel.getClass().isAssignableFrom(IEC1107SecuritySupport.LevelTwoAuthentication.class);
            }
        });
        // check for level three authentication
        assertThat(iec1107SecuritySupport.getAuthenticationAccessLevels()).areExactly(1, new Condition<AuthenticationDeviceAccessLevel>() {
            @Override
            public boolean matches(AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel) {
                return authenticationDeviceAccessLevel.getClass().isAssignableFrom(IEC1107SecuritySupport.LevelThreeAuthentication.class);
            }
        });

    }

    @Test
    public void getEncryptionAccessLevelsTest() {
        IEC1107SecuritySupport iec1107SecuritySupport = new IEC1107SecuritySupport();

        // currently no encryption levels are supported
        assertThat(iec1107SecuritySupport.getEncryptionAccessLevels()).isEmpty();

    }
}
