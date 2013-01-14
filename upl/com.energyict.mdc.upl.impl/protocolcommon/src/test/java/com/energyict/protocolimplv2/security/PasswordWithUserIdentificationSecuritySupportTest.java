package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import org.fest.assertions.core.Condition;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for the {@link PasswordWithUserIdentificationSecuritySupport} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 14/01/13
 * Time: 9:36
 */
public class PasswordWithUserIdentificationSecuritySupportTest {

    @Test
    public void getSecurityPropertiesTest() {
        PasswordWithUserIdentificationSecuritySupport passwordWithUserIdentificationSecuritySupport = new PasswordWithUserIdentificationSecuritySupport();

        // assert that you only have two properties to set
        assertThat(passwordWithUserIdentificationSecuritySupport.getSecurityProperties()).hasSize(2);

        // check for the password propertySpec
        assertThat(passwordWithUserIdentificationSecuritySupport.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
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
        assertThat(passwordWithUserIdentificationSecuritySupport.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
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
        PasswordWithUserIdentificationSecuritySupport passwordWithUserIdentificationSecuritySupport = new PasswordWithUserIdentificationSecuritySupport();

        // assert that you only have one authentication level
        assertThat(passwordWithUserIdentificationSecuritySupport.getAuthenticationAccessLevels()).hasSize(1);

        // check for the standard authentication level
        assertThat(passwordWithUserIdentificationSecuritySupport.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(PasswordWithUserIdentificationSecuritySupport.StandardAuthenticationAccessLevel.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
    }

    @Test
    public void getEncryptionAccessLevelsTest() {
        PasswordWithUserIdentificationSecuritySupport passwordWithUserIdentificationSecuritySupport = new PasswordWithUserIdentificationSecuritySupport();

        // assert that you only have one encryption level
        assertThat(passwordWithUserIdentificationSecuritySupport.getEncryptionAccessLevels()).hasSize(1);

        // check for the standard encryption level
        assertThat(passwordWithUserIdentificationSecuritySupport.getEncryptionAccessLevels()).has(new Condition<List<EncryptionDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels) {
                boolean match = false;
                for (EncryptionDeviceAccessLevel encryptionDeviceAccessLevel : encryptionDeviceAccessLevels) {
                    if (encryptionDeviceAccessLevel.getClass().isAssignableFrom(PasswordWithUserIdentificationSecuritySupport.StandardEncryptionAccessLevel.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
    }

}
