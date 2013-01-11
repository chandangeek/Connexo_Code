package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import org.fest.assertions.core.Condition;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for the {@link WavenisSecuritySupport} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/01/13
 * Time: 16:25
 */
public class WavenisSecuritySupportTest {

    @Test
    public void getSecurityPropertiesTest() {
        WavenisSecuritySupport wavenisSecuritySupport = new WavenisSecuritySupport();

        // assert that you only have two property to set
        assertThat(wavenisSecuritySupport.getSecurityProperties()).hasSize(2);


        // check for the password propertySpec
        assertThat(wavenisSecuritySupport.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
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

        // check for the encryptionKey propertySpec
        assertThat(wavenisSecuritySupport.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
            @Override
            public boolean matches(List<PropertySpec> propertySpecs) {
                boolean match = false;
                for (PropertySpec propertySpec : propertySpecs) {
                    if (propertySpec.equals(DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec())) {
                        match |= true;
                    }
                }
                return match;
            }
        });
    }

    @Test
    public void getAuthenticationAccessLevelsTest() {
        WavenisSecuritySupport wavenisSecuritySupport = new WavenisSecuritySupport();

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
        WavenisSecuritySupport wavenisSecuritySupport = new WavenisSecuritySupport();

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

}
