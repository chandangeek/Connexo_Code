package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;

import org.fest.assertions.core.Condition;

import java.util.List;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for the {@link NoOrPasswordSecuritySupport} component
 *
 * Copyrights EnergyICT
 * Date: 31/01/13
 * Time: 14:05
 */
public class NoOrPasswordSecuritySupportTest {

    @Test
    public void getSecurityPropertiesTest() {
        NoOrPasswordSecuritySupport noOrPasswordSecuritySupport = new NoOrPasswordSecuritySupport(propertySpecService);

        // assert that you only have two properties to set
        assertThat(noOrPasswordSecuritySupport.getSecurityProperties()).hasSize(1);

        // check for the password propertySpec
        assertThat(noOrPasswordSecuritySupport.getSecurityProperties()).has(new Condition<List<PropertySpec>>() {
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
        NoOrPasswordSecuritySupport noOrPasswordSecuritySupport = new NoOrPasswordSecuritySupport(propertySpecService);

        // assert that you only have one authentication level
        assertThat(noOrPasswordSecuritySupport.getAuthenticationAccessLevels()).hasSize(2);

        // check for the no authentication level
        assertThat(noOrPasswordSecuritySupport.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(NoOrPasswordSecuritySupport.NoAuthenticationAccessLevel.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });

        // check for the standard authentication level
        assertThat(noOrPasswordSecuritySupport.getAuthenticationAccessLevels()).has(new Condition<List<AuthenticationDeviceAccessLevel>>() {
            @Override
            public boolean matches(List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels) {
                boolean match = false;
                for (AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel : authenticationDeviceAccessLevels) {
                    if (authenticationDeviceAccessLevel.getClass().isAssignableFrom(NoOrPasswordSecuritySupport.StandardAuthenticationAccessLevel.class)) {
                        match |= true;
                    }
                }
                return match;
            }
        });
    }

    @Test
    public void getEncryptionAccessLevelsTest(){
        NoOrPasswordSecuritySupport passwordWithLevelSecuritySupport = new NoOrPasswordSecuritySupport(propertySpecService);

        // assert that you only have one encryption level
        assertThat(passwordWithLevelSecuritySupport.getEncryptionAccessLevels()).hasSize(0);
    }

}
