package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import org.fest.assertions.core.Condition;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for the {@link PasswordWithLevelSecuritySupport} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/01/13
 * Time: 15:48
 */
public class PasswordWithLevelSecuritySupportTest {

    @Test
    public void getSecurityPropertiesTest() {
        PasswordWithLevelSecuritySupport passwordWithLevelSecuritySupport = new PasswordWithLevelSecuritySupport();

        // assert that you only have two properties to set
        assertThat(passwordWithLevelSecuritySupport.getSecurityProperties()).hasSize(2);

        // check for the password propertySpec
        assertThat(passwordWithLevelSecuritySupport.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
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

        // check for the deviceAccessIdentifier propertySpec
        assertThat(passwordWithLevelSecuritySupport.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.equals(DeviceSecurityProperty.DEVICE_ACCESS_IDENTIFIER.getPropertySpec())) {
                        match |= true;
                    }
                }
                return match;
            }
        });
    }

    @Test
    public void getAuthenticationAccessLevelsTest() {
        PasswordWithLevelSecuritySupport passwordWithLevelSecuritySupport = new PasswordWithLevelSecuritySupport();

        // assert that you only have one authentication level
        assertThat(passwordWithLevelSecuritySupport.getAuthenticationAccessLevels()).hasSize(1);

        // check for the standard authentication level
        assertThat(passwordWithLevelSecuritySupport.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(PasswordWithLevelSecuritySupport.StandardAuthenticationAccessLevel.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
    }

    @Test
    public void getEncryptionAccessLevelsTest(){
        PasswordWithLevelSecuritySupport passwordWithLevelSecuritySupport = new PasswordWithLevelSecuritySupport();

        // assert that you only have one encryption level
        assertThat(passwordWithLevelSecuritySupport.getEncryptionAccessLevels()).hasSize(1);

        // check for the standard encryption level
        assertThat(passwordWithLevelSecuritySupport.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(PasswordWithLevelSecuritySupport.StandardEncryptionAccessLevel.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
    }

}
